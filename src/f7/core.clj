(ns f7.core
  "REPL orchestration. Run probes, intersect results, find leads."
  (:require [f7.gh :as gh]
            [f7.probes :as probes]
            [f7.signals :as sig]
            [clojure.string :as str]
            [clojure.java.io :as io]))

;; ---------------------------------------------------------------------------
;; Running probes
;; ---------------------------------------------------------------------------

(defn run-probes
  "Run a sequence of probes, returning all results."
  [probe-list]
  (mapv probes/run-probe probe-list))

;; ---------------------------------------------------------------------------
;; Intersection — the core payoff
;; ---------------------------------------------------------------------------

(defn intersect
  "Find repos that appear across multiple probe results.
   Returns repos sorted by number of probes they appear in, then by score."
  [probe-results & {:keys [min-probes] :or {min-probes 2}}]
  (let [;; Collect all (probe-name, repo) pairs with scores
        entries (for [{:keys [probe results]} probe-results
                      {:keys [repo signals]} results
                      :when (pos? (:score signals))]
                  {:probe-name (:name probe)
                   :full-name (:full-name repo)
                   :repo repo
                   :score (:score signals)
                   :hard (:hard signals)})
        ;; Group by repo
        by-repo (group-by :full-name entries)]
    (->> by-repo
         (map (fn [[full-name entries]]
                {:full-name full-name
                 :probe-count (count (distinct (map :probe-name entries)))
                 :probes (vec (distinct (map :probe-name entries)))
                 :total-score (reduce + (map :score entries))
                 :max-score (apply max (map :score entries))
                 :repo (:repo (first entries))
                 :all-hard-signals (vec (distinct (mapcat :hard entries)))}))
         (filter #(>= (:probe-count %) min-probes))
         (sort-by (juxt #(- (:probe-count %)) #(- (:total-score %))))
         vec)))

;; ---------------------------------------------------------------------------
;; Category density — which categories have the most commercial activity
;; ---------------------------------------------------------------------------

(defn category-density
  "For each probe, compute the fraction of repos showing commercial signals."
  [probe-results]
  (->> probe-results
       (map (fn [{:keys [probe results]}]
              (let [total (count results)
                    with-signals (count (filter #(pos? (get-in % [:signals :hard-count])) results))
                    scores (map #(get-in % [:signals :score]) results)]
                {:category (:name probe)
                 :repos-checked total
                 :repos-with-hard-signals with-signals
                 :density (if (pos? total) (double (/ with-signals total)) 0.0)
                 :avg-score (if (pos? total)
                              (double (/ (reduce + scores) total))
                              0.0)
                 :top-repos (->> results
                                 (sort-by #(- (get-in % [:signals :score])))
                                 (take 5)
                                 (mapv #(-> {:full-name (get-in % [:repo :full-name])
                                             :score (get-in % [:signals :score])
                                             :hard (mapv :signal (get-in % [:signals :hard]))})))})))
       (sort-by #(- (:density %)))
       vec))

;; ---------------------------------------------------------------------------
;; Gap analysis — high adoption, low commercial extraction
;; ---------------------------------------------------------------------------

(defn gaps
  "Find repos with high adoption (stars, forks) but few commercial signals.
   These are potential opportunities — demand exists but no one is extracting."
  [probe-results & {:keys [min-stars] :or {min-stars 1000}}]
  (->> probe-results
       (mapcat (fn [{:keys [probe results]}]
                 (map #(assoc % :category (:name probe)) results)))
       (filter (fn [{:keys [repo signals]}]
                 (and (>= (or (:stars repo) 0) min-stars)
                      (<= (:hard-count signals) 0)
                      (not (:archived repo))
                      (> (or (:fork-ratio repo) 0) 0.05))))
       (sort-by #(- (get-in % [:repo :stars])))
       (mapv (fn [{:keys [repo signals category]}]
               {:full-name (:full-name repo)
                :category category
                :stars (:stars repo)
                :forks (:forks repo)
                :fork-ratio (:fork-ratio repo)
                :language (:language repo)
                :description (:description repo)
                :note "High adoption, no commercial signals detected"}))))

;; ---------------------------------------------------------------------------
;; Reporting
;; ---------------------------------------------------------------------------

(defn print-density [density-results]
  (println "\n=== Category Signal Density ===\n")
  (doseq [{:keys [category repos-checked repos-with-hard-signals density avg-score top-repos]}
           density-results]
    (println (format "%-20s %d/%d repos (%.0f%%)  avg-score: %.1f"
                     category repos-with-hard-signals repos-checked
                     (* 100 density) avg-score))
    (doseq [r (take 3 top-repos)]
      (println (format "  %-40s score:%.1f  %s"
                       (:full-name r) (:score r)
                       (str/join ", " (map name (:hard r))))))
    (println)))

(defn print-intersection [intersection-results]
  (println "\n=== Fuzzy Intersection (repos across multiple probes) ===\n")
  (doseq [{:keys [full-name probe-count probes total-score all-hard-signals repo]}
           (take 20 intersection-results)]
    (println (format "%-40s probes:%d  score:%.1f  %s"
                     full-name probe-count total-score
                     (str/join "," (map name (map :signal all-hard-signals)))))
    (println (format "  Probes: %s" (str/join ", " probes)))
    (when (:description repo)
      (println (format "  %s" (subs (:description repo) 0 (min 80 (count (:description repo)))))))
    (println)))

(defn print-gaps [gap-results]
  (println "\n=== Opportunity Gaps (high adoption, low commercial signals) ===\n")
  (doseq [{:keys [full-name category stars forks fork-ratio language description]}
           (take 15 gap-results)]
    (println (format "%-40s %-15s stars:%d  forks:%d  f/s:%.2f  %s"
                     full-name category stars forks fork-ratio (or language "?")))
    (when description
      (println (format "  %s" (subs description 0 (min 80 (count description))))))
    (println)))

;; ---------------------------------------------------------------------------
;; Full run
;; ---------------------------------------------------------------------------

(defn run-and-report!
  "Run all probes, print analysis, save results."
  [& {:keys [probes output-file]
      :or {probes probes/all-probes
           output-file "data/results/latest.edn"}}]
  (let [results (run-probes probes)
        density (category-density results)
        inter (intersect results :min-probes 1)
        gap (gaps results)]
    (print-density density)
    (print-intersection inter)
    (print-gaps gap)
    ;; Save raw results as EDN
    (io/make-parents (io/file output-file))
    (spit output-file (pr-str {:timestamp (str (java.time.Instant/now))
                                :probe-results (mapv #(update % :results
                                                              (fn [rs] (mapv (fn [r]
                                                                               (update r :repo dissoc :readme :funding))
                                                                             rs)))
                                                     results)
                                :density density
                                :intersection inter
                                :gaps gap}))
    (println (str "\nResults saved to " output-file))
    (println (str "Rate limit: " (gh/rate-limit)))
    {:density density :intersection inter :gaps gap}))
