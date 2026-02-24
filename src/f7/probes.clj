(ns f7.probes
  "Probe definitions and runner. A probe is a question about the OSS landscape
   expressed as a GitHub search query + signal extraction.

   Probes are organized by proximity to the futon stack's actual capabilities:
   - Neighborhood: projects built on the same foundations (Datascript, XTDB, etc.)
   - Domain: areas where the stack has working code (knowledge graphs, agents, etc.)
   - Adjacent: commercially active spaces reachable from existing skills"
  (:require [f7.gh :as gh]
            [f7.signals :as sig]))

;; ---------------------------------------------------------------------------
;; Probe runner
;; ---------------------------------------------------------------------------

(defn run-probe
  "Execute a probe: search → enrich → extract signals → score.
   Returns {:probe probe :results [{:repo {...} :signals {...}} ...]}."
  [{:keys [name query per-page filter-fn]
    :or {per-page 30}
    :as probe}]
  (println (str ">>> Probe: " name))
  (println (str "    Query: " query))
  (let [search-result (gh/search-repos query :per-page per-page)]
    (if-not (:ok search-result)
      (do (println (str "    FAILED: " (:error search-result)))
          {:probe probe :results [] :error (:error search-result)})
      (let [repos (:data search-result)
            _ (println (str "    Found: " (count repos) " repos"))
            enriched (mapv (fn [repo]
                             (print ".")
                             (flush)
                             (let [enriched (gh/enrich-repo repo)
                                   signals (sig/extract-signals enriched)]
                               {:repo (dissoc enriched :readme :funding)
                                :signals signals}))
                           repos)
            _ (println)
            filtered (if filter-fn
                       (filterv #(filter-fn %) enriched)
                       enriched)
            sorted (sort-by #(- (get-in % [:signals :score])) filtered)]
        (println (str "    Scored: " (count (filter #(pos? (get-in % [:signals :score])) sorted))
                      " repos with positive signals"))
        {:probe (dissoc probe :filter-fn)
         :results (vec sorted)}))))

;; ---------------------------------------------------------------------------
;; NEIGHBORHOOD — projects built on the same foundations
;; "Who else is building on what we build on, and are they making money?"
;; ---------------------------------------------------------------------------

(def datascript-ecosystem
  {:name "datascript-ecosystem"
   :query "datascript OR datalog stars:>50 pushed:>2024-01-01"
   :per-page 30
   :proximity :neighborhood})

(def xtdb-ecosystem
  {:name "xtdb-ecosystem"
   :query "xtdb OR crux-db OR \"temporal database\" OR \"bitemporality\" stars:>30 pushed:>2024-01-01"
   :per-page 30
   :proximity :neighborhood})

(def clojure-commercial
  {:name "clojure-commercial"
   :query "language:Clojure stars:>200 pushed:>2024-01-01"
   :per-page 50
   :proximity :neighborhood})

;; ---------------------------------------------------------------------------
;; DOMAIN — areas where the stack has working code
;; "In the spaces we already operate, who is extracting revenue?"
;; ---------------------------------------------------------------------------

(def knowledge-graphs
  {:name "knowledge-graphs"
   :query "knowledge-graph OR entity-resolution OR \"graph database\" stars:>200 pushed:>2024-01-01"
   :per-page 30
   :proximity :domain})

(def multi-agent-coordination
  {:name "multi-agent-coordination"
   :query "multi-agent OR agent-framework OR \"agent orchestration\" stars:>200 pushed:>2024-01-01"
   :per-page 30
   :proximity :domain})

(def pattern-rule-systems
  {:name "pattern-rule-systems"
   :query "rule-engine OR \"pattern matching\" OR \"decision engine\" OR \"business rules\" stars:>200 pushed:>2024-01-01"
   :per-page 30
   :proximity :domain})

(def nlp-entity-extraction
  {:name "nlp-entity-extraction"
   :query "NER OR \"entity extraction\" OR \"information extraction\" OR \"named entity\" stars:>200 pushed:>2024-01-01"
   :per-page 30
   :proximity :domain})

;; ---------------------------------------------------------------------------
;; ADJACENT — commercially active spaces reachable from existing skills
;; "Where could futon-style thinking (patterns, graphs, agents) plug in?"
;; ---------------------------------------------------------------------------

(def math-formal-methods
  {:name "math-formal-methods"
   :query "theorem-prover OR \"formal verification\" OR \"proof assistant\" OR \"math knowledge\" stars:>100 pushed:>2024-01-01"
   :per-page 30
   :proximity :adjacent})

(def dev-workflow-automation
  {:name "dev-workflow-automation"
   :query "\"developer workflow\" OR \"code review\" OR \"development automation\" OR devops stars:>500 pushed:>2024-01-01"
   :per-page 30
   :proximity :adjacent})

(def annotation-transparency
  {:name "annotation-transparency"
   :query "\"web annotation\" OR \"supply chain transparency\" OR \"provenance\" OR \"audit trail\" stars:>50 pushed:>2024-01-01"
   :per-page 30
   :proximity :adjacent})

;; ---------------------------------------------------------------------------
;; Probe sets
;; ---------------------------------------------------------------------------

(def neighborhood-probes
  [datascript-ecosystem xtdb-ecosystem clojure-commercial])

(def domain-probes
  [knowledge-graphs multi-agent-coordination pattern-rule-systems nlp-entity-extraction])

(def adjacent-probes
  [math-formal-methods dev-workflow-automation annotation-transparency])

(def all-probes
  (concat neighborhood-probes domain-probes adjacent-probes))
