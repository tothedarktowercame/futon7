(ns f7.probe-gen
  "Generate landscape probes from the futon3a concept graph.

   Instead of hand-crafting probes (as in probes.clj), this module reads
   entities and arrows from meme.db and generates probe definitions
   automatically. High-degree concepts become search terms; arrow clusters
   become probe categories.

   Requires MEME_DB_PATH to point to a populated meme.db.

   Run: cd futon7 && MEME_DB_PATH=../futon3a/meme.db bb probe-gen"
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

;; We read meme.db via SQLite JDBC when available (clj -M),
;; or fall back to reading the bootstrap filter report (bb).

(def ^:private alignment-areas
  "Map concept keywords to landscape probe areas.
   Checked as exact match against concept name."
  {;; knowledge store / graph
   "knowledge"   :knowledge-graph
   "graph"       :knowledge-graph
   "entity"      :knowledge-graph
   "store"       :knowledge-store
   "memory"      :knowledge-store
   "schema"      :knowledge-store
   "datascript"  :knowledge-store
   "datalog"     :knowledge-store
   "identity"    :knowledge-graph
   ;; pattern reasoning
   "pattern"     :pattern-reasoning
   "patterns"    :pattern-reasoning
   "rule"        :pattern-reasoning
   "coherence"   :pattern-reasoning
   "discipline"  :pattern-reasoning
   "validation"  :pattern-reasoning
   ;; multi-agent
   "agent"       :multi-agent
   "agents"      :multi-agent
   "dispatch"    :multi-agent
   "coordination" :multi-agent
   "session"     :multi-agent
   "peripheral"  :multi-agent
   ;; formal math
   "proof"       :formal-math
   "theorem"     :formal-math
   "formal"      :formal-math
   "construct"   :formal-math
   "lemma"       :formal-math
   ;; transparency / provenance
   "evidence"    :transparency
   "audit"       :transparency
   "provenance"  :transparency
   "transparency" :transparency
   "trust"       :transparency
   "traceability" :transparency
   ;; dev workflow
   "workflow"    :dev-workflow
   "code"        :dev-workflow
   "test"        :dev-workflow
   "deploy"      :dev-workflow
   "commit"      :dev-workflow})

(defn- area-for-concept [concept-name]
  (or (get alignment-areas concept-name)
      :general))

(defn- load-concepts-from-report
  "Load kept concepts from bootstrap filter report (works in bb)."
  [report-path]
  (let [report (edn/read-string (slurp report-path))]
    (:kept-concepts report)))

(defn- concept->search-term
  "Convert a concept name to a GitHub search term.
   Multi-word concepts become quoted phrases; single words stay as-is."
  [concept-name]
  (if (str/includes? concept-name " ")
    (str "\"" concept-name "\"")
    concept-name))

(defn- generate-probes-from-concepts
  "Given a seq of {:concept name :freq N}, group by area and generate probes."
  [concepts]
  (let [;; Filter to concepts with reasonable search potential
        searchable (filter (fn [{:keys [concept freq]}]
                            (and (>= freq 8)   ;; frequent enough to be meaningful
                                 (<= freq 200) ;; not so common it's noise
                                 (> (count concept) 3))) ;; long enough for search
                          concepts)
        ;; Group by area
        by-area (group-by #(area-for-concept (:concept %)) searchable)
        ;; For each area, pick top concepts by frequency and build a probe
        probes
        (for [[area area-concepts] by-area
              :when (not= area :general)
              :let [top (take 5 (sort-by #(- (:freq %)) area-concepts))
                    search-terms (map #(concept->search-term (:concept %)) top)
                    query (str (str/join " OR " search-terms)
                               " stars:>50 pushed:>2024-01-01")]]
          {:name (str "graph-" (name area))
           :query query
           :per-page 30
           :proximity (case area
                        (:knowledge-store :knowledge-graph :pattern-reasoning) :domain
                        (:multi-agent :dev-workflow) :domain
                        (:formal-math :transparency) :adjacent
                        :adjacent)
           :source-entities (mapv :concept top)
           :generated-from "meme.db concept graph"})]
    (vec probes)))

(defn generate-probes
  "Generate probes from the concept graph.
   Returns a vec of probe maps compatible with f7.probes/run-probe."
  ([] (generate-probes "../futon3a/data/bootstrap-filter-report.edn"))
  ([report-path]
   (let [concepts (load-concepts-from-report report-path)
         probes (generate-probes-from-concepts concepts)]
     probes)))

(defn -main [& _args]
  (let [report-path (or (System/getenv "FILTER_REPORT")
                        "../futon3a/data/bootstrap-filter-report.edn")]
    (println "=== Probe Generation from Concept Graph ===")
    (println (str "Source: " report-path))
    (let [probes (generate-probes report-path)]
      (println (str "Generated " (count probes) " probes:"))
      (println)
      (doseq [p probes]
        (println (str "  " (:name p)))
        (println (str "    Query: " (:query p)))
        (println (str "    Source entities: " (str/join ", " (:source-entities p))))
        (println))
      ;; Write to file
      (let [out-path "data/results/generated-probes.edn"]
        (io/make-parents out-path)
        (spit out-path (pr-str probes))
        (println (str "Written to " out-path))))))
