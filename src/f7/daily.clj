(ns f7.daily
  "Daily scan + brief generation.

   Runs probes, caches results, computes delta against previous scan,
   and produces a markdown brief. The brief is the daily deliverable
   for M-daily-scan.

   Usage:
     bb daily-scan              ; run scan + produce brief
     bb daily-scan --dry-run    ; show what would be scanned without API calls"
  (:require [f7.core :as core]
            [f7.probes :as probes]
            [f7.probe-expansion :as expansion]
            [f7.report :as report]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :as json])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)))

;; ---------------------------------------------------------------------------
;; Paths
;; ---------------------------------------------------------------------------

(def ^:private data-dir "data/daily")
(def ^:private briefs-dir "data/briefs")

(defn- today-str []
  (.format (LocalDate/now) (DateTimeFormatter/ofPattern "yyyy-MM-dd")))

(defn- scan-path [date-str]
  (str data-dir "/" date-str ".edn"))

(defn- brief-path [date-str]
  (str briefs-dir "/" date-str ".md"))

(defn- latest-scan-before [date-str]
  (let [dir (io/file data-dir)]
    (when (.exists dir)
      (->> (.listFiles dir)
           (filter #(str/ends-with? (.getName %) ".edn"))
           (map #(.getName %))
           (map #(str/replace % ".edn" ""))
           (filter #(neg? (compare % date-str)))
           sort
           last))))

;; ---------------------------------------------------------------------------
;; Run scan
;; ---------------------------------------------------------------------------

(defn run-scan!
  "Run all probes and save results to data/daily/<date>.edn"
  []
  (let [date (today-str)
        path (scan-path date)]
    (io/make-parents path)
    (println (str "=== Daily scan: " date " ==="))
    (let [custom-probes (or (expansion/load-custom-probes) [])
          _ (when (seq custom-probes)
              (println (str "Custom probes: " (count custom-probes))))
          probe-list (into (vec probes/all-probes) custom-probes)
          results (core/run-probes probe-list)
          intersection (core/intersect results)
          density (core/category-density results)
          gaps (core/gaps results)
          scan {:date date
                :probe-count (count probe-list)
                :total-repos (reduce + (map #(count (:results %)) results))
                :results results
                :intersection intersection
                :density density
                :gaps gaps}]
      (spit path (pr-str scan))
      (println (str "Saved: " path " (" (:total-repos scan) " repos from "
                    (:probe-count scan) " probes)"))
      scan)))

;; ---------------------------------------------------------------------------
;; Delta detection
;; ---------------------------------------------------------------------------

(defn- load-scan [path]
  (when (.exists (io/file path))
    (read-string (slurp path))))

(defn compute-delta
  "Compare today's scan against the previous one.
   Returns {:new-repos [...] :changed-repos [...] :lost-repos [...]}."
  [today-scan prev-scan]
  (if-not prev-scan
    {:new-repos (mapv :full-name (:intersection today-scan))
     :changed-repos []
     :lost-repos []
     :first-scan? true}
    (let [today-set (set (map :full-name (:intersection today-scan)))
          prev-set (set (map :full-name (:intersection prev-scan)))
          today-by-name (into {} (map (juxt :full-name identity) (:intersection today-scan)))
          prev-by-name (into {} (map (juxt :full-name identity) (:intersection prev-scan)))
          new-repos (vec (clojure.set/difference today-set prev-set))
          lost-repos (vec (clojure.set/difference prev-set today-set))
          ;; Changed: score changed or probe count changed
          changed-repos (->> (clojure.set/intersection today-set prev-set)
                             (filter (fn [name]
                                       (let [t (get today-by-name name)
                                             p (get prev-by-name name)]
                                         (or (not= (:total-score t) (:total-score p))
                                             (not= (:probe-count t) (:probe-count p))))))
                             vec)]
      {:new-repos new-repos
       :changed-repos changed-repos
       :lost-repos lost-repos
       :first-scan? false})))

;; ---------------------------------------------------------------------------
;; Brief generation
;; ---------------------------------------------------------------------------

(defn- probe-is-custom? [probe-name]
  (.exists (io/file (str "data/probes/" probe-name ".edn"))))

(defn- top-from-probe
  "Best scoring items from a specific probe."
  [probe-result n]
  (->> (:results probe-result)
       (filter #(pos? (get-in % [:signals :score])))
       (sort-by #(- (get-in % [:signals :score])))
       (take n)))

(defn- render-item [repo signals]
  (let [name (:full-name repo)
        score (:score signals)
        hard (->> (:hard signals)
                  (filter :present?)
                  (map #(str (clojure.core/name (:signal %))
                             (when (:detail %) (str " (" (:detail %) ")")))))]
    (str "- **" name "** (score:" score
         " stars:" (:stars repo) ")"
         (when (seq hard) (str " — " (str/join ", " hard)))
         "\n")))

(defn render-brief
  "Produce a markdown brief from scan + delta.
   Leads with new probe results. Suppresses stale repeats."
  [scan delta date]
  (let [results (:results scan)
        ;; Separate new/custom probes from static probes
        new-probe-results (filter #(probe-is-custom? (get-in % [:probe :name])) results)
        static-probe-results (remove #(probe-is-custom? (get-in % [:probe :name])) results)
        ;; Items from yesterday (to suppress)
        prev-intersection-names (set (or (:prev-intersection-names delta) []))
        ;; New intersection items only
        new-intersection (->> (:intersection scan)
                              (filter #(contains? (set (:new-repos delta)) (:full-name %))))]
    (str
     "# Daily Scan Brief — " date "\n\n"
     "**Probes:** " (:probe-count scan)
     " (" (count new-probe-results) " custom, "
     (count static-probe-results) " static)"
     " | **Repos scanned:** " (:total-repos scan) "\n\n"

     ;; === Section 1: New probe highlights ===
     (when (seq new-probe-results)
       (str "## Today's Focus\n\n"
            (str/join "\n"
                      (for [pr new-probe-results
                            :let [probe (:probe pr)
                                  source (:source probe)
                                  top (top-from-probe pr 3)]
                            :when (seq top)]
                        (str "### " (:name probe) "\n"
                             (when source (str "*" source "*\n"))
                             "\n"
                             (str/join "" (map #(render-item (:repo %) (:signals %)) top))
                             "\n")))
            "\n"))

     ;; === Section 2: New intersection items (if any) ===
     (when (seq new-intersection)
       (str "## New Cross-Probe Hits\n\n"
            (str/join ""
                      (map (fn [item]
                             (str "- **" (:full-name item) "** — in "
                                  (str/join " ∩ " (:probes item))
                                  " (score:" (:total-score item) ")\n"))
                           new-intersection))
            "\n"))

     ;; === Section 3: Static probe notable items (only if new/changed) ===
     (let [static-new (for [pr static-probe-results
                            :let [top (top-from-probe pr 1)
                                  item (first top)]
                            :when (and item
                                       (not (contains? prev-intersection-names
                                                       (get-in item [:repo :full-name]))))]
                        {:probe-name (get-in pr [:probe :name])
                         :item item})]
       (when (seq static-new)
         (str "## Static Probe Updates\n\n"
              "_Showing top item per probe (suppressing yesterday's repeats)_\n\n"
              (str/join ""
                        (for [{:keys [probe-name item]} static-new]
                          (str "**" probe-name ":** "
                               (get-in item [:repo :full-name])
                               " (score:" (get-in item [:signals :score])
                               " stars:" (get-in item [:repo :stars]) ")\n")))
              "\n")))

     "## Workup\n\n"
     "_Focus on the Today's Focus section. What connections do you see?_\n"
     "_What could you offer to the most interesting items above?_\n\n"
     "---\n"
     "*Generated by `bb daily-scan` for M-daily-scan. Day "
     (if-let [prev (latest-scan-before date)] "2+" "1")
     ".*\n")))

;; ---------------------------------------------------------------------------
;; Main
;; ---------------------------------------------------------------------------

(defn -main [& args]
  (let [date (today-str)
        ;; Run scan
        scan (run-scan!)
        ;; Load previous
        prev-date (latest-scan-before date)
        prev-scan (when prev-date (load-scan (scan-path prev-date)))
        _ (when prev-date (println (str "Previous scan: " prev-date)))
        ;; Compute delta
        delta (compute-delta scan prev-scan)
        ;; Render brief
        brief (render-brief scan delta date)
        bpath (brief-path date)]
    ;; Save brief
    (io/make-parents bpath)
    (spit bpath brief)
    (println (str "\nBrief saved: " bpath))
    (println (str "\n" brief))))
