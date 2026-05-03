(ns f7.probe-expansion
  "Probe expansion: generate new probes from scan results and workup insights.

   The static probes in probes.clj are the seed set. This module adds
   probes derived from:
   1. Workup insights (manually identified niches)
   2. Dependency crawling (repos that use our deps)
   3. Contributor networks (what else do interesting people build?)

   New probes are saved to data/probes/ as EDN files and loaded
   alongside the static set on subsequent scans.

   Pattern: depositing/scan-talk-frame — the scan evolves with use."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def ^:private custom-probes-dir "data/probes")

(defn axis-dir
  "Resolve custom-probes directory for AXIS.
   nil or \"main\" → data/probes (existing consulting probes).
   Any other axis → data/probes/<axis> (subseries probes)."
  [axis]
  (if (or (nil? axis) (= axis "main"))
    custom-probes-dir
    (str custom-probes-dir "/" axis)))

(defn load-custom-probes
  "Load custom probe definitions for AXIS.
   nil/\"main\" loads data/probes/*.edn (immediate children only —
   subseries subdirectories are skipped because directory names
   don't end in .edn).  Other axes load data/probes/<axis>/*.edn."
  ([] (load-custom-probes nil))
  ([axis]
   (let [dir (io/file (axis-dir axis))]
     (when (.exists dir)
       (->> (.listFiles dir)
            (filter #(.isFile %))
            (filter #(str/ends-with? (.getName %) ".edn"))
            (mapv #(try (read-string (slurp %))
                        (catch Exception _ nil)))
            (filterv some?))))))

(defn save-custom-probe!
  "Save a custom probe definition to data/probes[/axis]/<name>.edn."
  ([probe] (save-custom-probe! probe nil))
  ([{:keys [name] :as probe} axis]
   (let [path (str (axis-dir axis) "/" name ".edn")]
     (io/make-parents path)
     (spit path (pr-str probe))
     (println (str "Saved custom probe: " path))
     probe)))

;; ---------------------------------------------------------------------------
;; Workup-derived probes (Day 1 results)
;; ---------------------------------------------------------------------------

(def day-1-expansion
  "Probes generated from Day 1 workup analysis."
  [{:name "xtdb-users"
    :query "xtdb in:file filename:deps.edn stars:>5 pushed:>2024-01-01"
    :per-page 30
    :proximity :neighborhood
    :source "Day 1 workup: 'XTDB without Juxt' — who uses XTDB?"}

   {:name "variational-inference"
    :query "\"variational inference\" OR \"amortized inference\" OR \"structured beliefs\" stars:>50 pushed:>2024-01-01"
    :per-page 30
    :proximity :adjacent
    :source "Day 1 workup: Datahike principal's PhD thesis"}

   {:name "xanadu-hypertext"
    :query "xanadu OR \"transclusion\" OR \"bidirectional link\" OR \"knowledge graph editor\" stars:>20 pushed:>2024-01-01"
    :per-page 30
    :proximity :domain
    :source "Day 1 workup: WebArxana as Xanadu clone, not Roam clone"}

   {:name "opencollective-funded"
    :query "open_collective in:file filename:FUNDING.yml language:Clojure pushed:>2024-01-01"
    :per-page 30
    :proximity :neighborhood
    :source "Day 1 workup: Logseq's OpenCollective balance"}

   {:name "evaluation-frameworks"
    :query "\"logic model\" OR \"theory of change\" OR \"evaluation framework\" OR \"programme evaluation\" stars:>20 pushed:>2024-01-01"
    :per-page 30
    :proximity :adjacent
    :source "Day 1 workup: UKRN-S delivery, Bayesian evaluation consulting"}])

(defn install-day-1-probes!
  "Save all Day 1 expansion probes to data/probes/."
  []
  (doseq [probe day-1-expansion]
    (save-custom-probe! probe))
  (println (str "Installed " (count day-1-expansion) " custom probes.")))
