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
    (let [probe-list probes/all-probes
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

(defn- top-items
  "Pick the top N items from intersection, preferring new/changed."
  [intersection delta n]
  (let [new-set (set (:new-repos delta))
        changed-set (set (:changed-repos delta))
        prioritized (sort-by
                     (fn [item]
                       [(cond
                          (contains? new-set (:full-name item)) 0
                          (contains? changed-set (:full-name item)) 1
                          :else 2)
                        (- (:total-score item))])
                     intersection)]
    (take n prioritized)))

(defn render-brief
  "Produce a markdown brief from scan + delta."
  [scan delta date]
  (let [top (top-items (:intersection scan) delta 5)
        new-count (count (:new-repos delta))
        changed-count (count (:changed-repos delta))]
    (str
     "# Daily Scan Brief — " date "\n\n"
     "**Probes:** " (:probe-count scan) " | "
     "**Repos scanned:** " (:total-repos scan) " | "
     "**In intersection:** " (count (:intersection scan)) "\n\n"
     (if (:first-scan? delta)
       "*First scan — no delta available.*\n\n"
       (str "**Delta:** " new-count " new, "
            changed-count " changed, "
            (count (:lost-repos delta)) " dropped\n\n"))
     "## Top Items\n\n"
     (str/join "\n"
               (map-indexed
                (fn [i item]
                  (let [tag (cond
                              (contains? (set (:new-repos delta)) (:full-name item)) "NEW"
                              (contains? (set (:changed-repos delta)) (:full-name item)) "CHANGED"
                              :else "")]
                    (str "### " (inc i) ". " (:full-name item)
                         (when (seq tag) (str " [" tag "]"))
                         "\n"
                         "- **Probes:** " (:probe-count item)
                         " | **Score:** " (:total-score item)
                         " | **In:** " (str/join ", " (:probes item)) "\n"
                         "- **Signals:** "
                         (str/join ", " (->> (:all-hard-signals item)
                                             (filter :present?)
                                             (map #(str (name (:signal %))
                                                        (when (:detail %) (str " (" (:detail %) ")"))))))
                         "\n"
                         "- **Capacity match:** _TODO — what could Joe offer here?_\n")))
                top))
     "\n\n## Workup\n\n"
     "_Spend the remaining time on the most promising item above._\n"
     "_What targeted response could you prepare? (Bayesian model, flexiarg, capability demo)_\n\n"
     "---\n"
     "*Generated by `bb daily-scan` for M-daily-scan.*\n")))

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
