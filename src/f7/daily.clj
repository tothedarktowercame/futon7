(ns f7.daily
  "Daily scan + brief generation.

   Runs probes, caches results, computes delta against previous scan,
   and produces a markdown brief. The brief is the daily deliverable
   for M-daily-scan.

   Usage:
     bb daily-scan                   ; run main (consulting) scan
     bb daily-scan --axis lean       ; run :lean subseries scan
     bb daily-scan-lean              ; shorthand task for :lean

   Subseries axes:
     main      — data/probes/*.edn       + static consulting probes
     <axis>    — data/probes/<axis>/*.edn (subseries custom probes only;
                 static probes skipped)

   Data locations:
     main:    data/daily/<date>.edn, data/briefs/<date>.md
     <axis>:  data/daily/<axis>/<date>.edn, data/briefs/<axis>/<date>.md"
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

(defn- axis-suffix [axis]
  (if (or (nil? axis) (= axis "main")) "" (str "/" axis)))

(defn- today-str []
  (.format (LocalDate/now) (DateTimeFormatter/ofPattern "yyyy-MM-dd")))

(defn- scan-path
  ([date-str] (scan-path date-str nil))
  ([date-str axis]
   (str data-dir (axis-suffix axis) "/" date-str ".edn")))

(defn- brief-path
  ([date-str] (brief-path date-str nil))
  ([date-str axis]
   (str briefs-dir (axis-suffix axis) "/" date-str ".md")))

(defn- latest-scan-before
  ([date-str] (latest-scan-before date-str nil))
  ([date-str axis]
   (let [dir (io/file (str data-dir (axis-suffix axis)))]
     (when (.exists dir)
       (->> (.listFiles dir)
            (filter #(.isFile %))
            (filter #(str/ends-with? (.getName %) ".edn"))
            (map #(.getName %))
            (map #(str/replace % ".edn" ""))
            (filter #(neg? (compare % date-str)))
            sort
            last)))))

;; ---------------------------------------------------------------------------
;; Run scan
;; ---------------------------------------------------------------------------

(defn run-scan!
  "Run all probes for AXIS and save results to
   data/daily[/axis]/<date>.edn.  For the main (consulting) axis
   the static probe set from f7.probes is included.  Subseries axes
   use only their own custom probes."
  ([] (run-scan! nil))
  ([axis]
   (let [date (today-str)
         path (scan-path date axis)
         main? (or (nil? axis) (= axis "main"))]
     (io/make-parents path)
     (println (str "=== Daily scan: " date
                   (when-not main? (str " [" axis " subseries]"))
                   " ==="))
     (let [custom-probes (or (expansion/load-custom-probes axis) [])
           _ (when (seq custom-probes)
               (println (str "Custom probes: " (count custom-probes))))
           probe-list (if main?
                        (into (vec probes/all-probes) custom-probes)
                        (vec custom-probes))
           _ (when-not main?
               (println (str "Static probes skipped for subseries: "
                             axis)))
           results (core/run-probes probe-list)
           intersection (core/intersect results)
           density (core/category-density results)
           gaps (core/gaps results)
           scan {:date date
                 :axis (or axis "main")
                 :probe-count (count probe-list)
                 :total-repos (reduce + (map #(count (:results %)) results))
                 :results results
                 :intersection intersection
                 :density density
                 :gaps gaps}]
       (spit path (pr-str scan))
       (println (str "Saved: " path " (" (:total-repos scan) " repos from "
                     (:probe-count scan) " probes)"))
       scan))))

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

(defn- probe-is-custom?
  ([probe-name] (probe-is-custom? probe-name nil))
  ([probe-name axis]
   (.exists (io/file (str (expansion/axis-dir axis) "/"
                          probe-name ".edn")))))

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
  ([scan delta date] (render-brief scan delta date nil))
  ([scan delta date axis]
   (let [results (:results scan)
        ;; Separate new/custom probes from static probes
        new-probe-results (filter #(probe-is-custom? (get-in % [:probe :name]) axis) results)
        static-probe-results (remove #(probe-is-custom? (get-in % [:probe :name]) axis) results)
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
     "*Generated by `bb daily-scan"
     (when (and axis (not= axis "main")) (str "-" axis))
     "` for M-daily-scan"
     (when (and axis (not= axis "main")) (str " [:" axis " subseries]"))
     ". Day "
     (if-let [prev (latest-scan-before date axis)] "2+" "1")
     ".*\n"))))

;; ---------------------------------------------------------------------------
;; Main
;; ---------------------------------------------------------------------------

(defn- parse-args [args]
  (loop [remaining args
         result {:axis nil}]
    (cond
      (empty? remaining) result
      (= "--axis" (first remaining))
        (recur (drop 2 remaining)
               (assoc result :axis (second remaining)))
      :else (recur (rest remaining) result))))

(defn -main [& args]
  (let [{:keys [axis]} (parse-args args)
        date (today-str)
        scan (run-scan! axis)
        prev-date (latest-scan-before date axis)
        prev-scan (when prev-date (load-scan (scan-path prev-date axis)))
        _ (when prev-date (println (str "Previous scan: " prev-date)))
        delta (compute-delta scan prev-scan)
        brief (render-brief scan delta date axis)
        bpath (brief-path date axis)]
    (io/make-parents bpath)
    (spit bpath brief)
    (println (str "\nBrief saved: " bpath))
    (println (str "\n" brief))))
