(ns f7.report
  "Generate a human-readable lead report from probe results.
   Scores repos by alignment with futon stack capabilities."
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

;; ---------------------------------------------------------------------------
;; Stack alignment scoring
;; ---------------------------------------------------------------------------

(def stack-alignment-keywords
  "Keywords that indicate a repo touches areas where the futon stack has
   working capabilities. Maps keyword -> {:area ... :weight ...}."
  {"datalog"            {:area "knowledge-store" :weight 2}
   "datascript"         {:area "knowledge-store" :weight 2}
   "xtdb"               {:area "knowledge-store" :weight 2}
   "bitemporal"         {:area "knowledge-store" :weight 2}
   "knowledge graph"    {:area "knowledge-graph" :weight 2}
   "entity resolution"  {:area "knowledge-graph" :weight 2}
   "graph database"     {:area "knowledge-graph" :weight 1}
   "pattern"            {:area "pattern-reasoning" :weight 1}
   "rule engine"        {:area "pattern-reasoning" :weight 2}
   "decision engine"    {:area "pattern-reasoning" :weight 2}
   "business rules"     {:area "pattern-reasoning" :weight 2}
   "agent"              {:area "multi-agent" :weight 1}
   "multi-agent"        {:area "multi-agent" :weight 2}
   "orchestration"      {:area "multi-agent" :weight 1}
   "theorem"            {:area "formal-math" :weight 2}
   "proof"              {:area "formal-math" :weight 1}
   "formal verification" {:area "formal-math" :weight 2}
   "annotation"         {:area "transparency" :weight 2}
   "provenance"         {:area "transparency" :weight 2}
   "transparency"       {:area "transparency" :weight 2}
   "audit"              {:area "transparency" :weight 1}
   "traceability"       {:area "transparency" :weight 2}
   "code review"        {:area "dev-workflow" :weight 1}
   "code intelligence"  {:area "dev-workflow" :weight 2}
   "developer tool"     {:area "dev-workflow" :weight 1}
   "clojure"            {:area "native-stack" :weight 2}
   "babashka"           {:area "native-stack" :weight 3}})

(defn- compute-alignment
  "Score how well a repo aligns with futon stack capabilities.
   Returns {:score N :areas #{...} :matches [...]}."
  [{:keys [description full-name topics language] :as repo}]
  (let [text (str/lower-case
              (str (or description "") " "
                   (or full-name "") " "
                   (str/join " " (or topics [])) " "
                   (or language "")))
        matches (for [[kw {:keys [area weight]}] stack-alignment-keywords
                      :when (str/includes? text (str/lower-case kw))]
                  {:keyword kw :area area :weight weight})]
    {:score (reduce + 0 (map :weight matches))
     :areas (set (map :area matches))
     :matches (vec matches)}))

;; ---------------------------------------------------------------------------
;; Lead classification
;; ---------------------------------------------------------------------------

(defn- classify-lead
  "Classify a repo as a lead type based on signals + alignment."
  [commercial-score alignment-score]
  (cond
    (and (>= commercial-score 3) (>= alignment-score 3))
    :hot-lead

    (and (>= commercial-score 2) (>= alignment-score 2))
    :warm-lead

    (and (>= commercial-score 3) (< alignment-score 2))
    :commercial-but-distant

    (and (< commercial-score 2) (>= alignment-score 3))
    :aligned-but-uncommercial

    :else :cold))

;; ---------------------------------------------------------------------------
;; Report generation
;; ---------------------------------------------------------------------------

(defn- format-lead [{:keys [repo signals alignment lead-type category]}]
  (let [{:keys [full-name description stars forks language]} repo
        hard-signals (mapv :signal (:hard signals))]
    (str "### " full-name "\n\n"
         "- **Category**: " category "\n"
         "- **Lead type**: " (name lead-type) "\n"
         "- **Stars**: " (or stars "?")
         "  |  **Forks**: " (or forks "?")
         "  |  **Language**: " (or language "?") "\n"
         "- **Commercial signals**: "
         (if (seq hard-signals)
           (str/join ", " (map name hard-signals))
           "none") "\n"
         "- **Stack alignment**: score " (:score alignment)
         " — areas: "
         (if (seq (:areas alignment))
           (str/join ", " (sort (map name (:areas alignment))))
           "none")
         "\n"
         "- **Description**: "
         (let [d (or description "(none)")]
           (if (> (count d) 200)
             (str (subs d 0 200) "...")
             d))
         "\n"
         "\n")))

(defn- format-gap [{:keys [full-name category stars forks language description]}]
  (str "- **" full-name "** (" category ") — "
       stars " stars, " forks " forks, " (or language "?")
       (when description (str " — " (subs description 0 (min 80 (count description)))))
       "\n"))

(defn generate-report
  "Generate a markdown lead report from probe results EDN."
  [input-file output-file]
  (println (str "Reading " input-file "..."))
  (let [data (edn/read-string (slurp input-file))
        {:keys [probe-results density gaps]} data

        ;; Flatten all repos with their probe category
        all-repos (for [{:keys [probe results]} probe-results
                        {:keys [repo signals]} results]
                    (let [alignment (compute-alignment repo)
                          lead-type (classify-lead (:score signals) (:score alignment))]
                      {:repo repo
                       :signals signals
                       :alignment alignment
                       :lead-type lead-type
                       :category (:name probe)
                       :combined-score (+ (:score signals) (* 0.5 (:score alignment)))}))

        ;; Deduplicate by full-name, keeping highest combined score
        by-name (group-by #(get-in % [:repo :full-name]) all-repos)
        deduped (mapv (fn [[_ entries]] (apply max-key :combined-score entries))
                      by-name)

        hot (filter #(= :hot-lead (:lead-type %)) deduped)
        warm (filter #(= :warm-lead (:lead-type %)) deduped)
        aligned (filter #(= :aligned-but-uncommercial (:lead-type %)) deduped)

        report (str "# Futon7 Landscape Intelligence Report\n\n"
                    "Generated: " (str (java.time.Instant/now)) "\n"
                    "Source: " input-file "\n\n"
                    "## Summary\n\n"
                    "- Probes run: " (count probe-results) "\n"
                    "- Total repos analyzed: " (count deduped) "\n"
                    "- Hot leads (commercial + aligned): " (count hot) "\n"
                    "- Warm leads: " (count warm) "\n"
                    "- Aligned but uncommercial: " (count aligned) "\n"
                    "- Gap opportunities: " (count gaps) "\n\n"

                    "## Category Signal Density\n\n"
                    "| Category | Repos with signals | Density | Avg Score |\n"
                    "|----------|-------------------|---------|----------|\n"
                    (str/join (for [{:keys [category repos-checked repos-with-hard-signals
                                           density avg-score]} density]
                               (format "| %-24s | %d/%d | %.0f%% | %.1f |\n"
                                       category repos-with-hard-signals repos-checked
                                       (* 100 density) avg-score)))
                    "\n"

                    "## Hot Leads\n\n"
                    "These repos show strong commercial signals AND align with futon stack capabilities.\n\n"
                    (if (seq hot)
                      (str/join (map format-lead (sort-by #(- (:combined-score %)) hot)))
                      "_No hot leads found._\n\n")

                    "## Warm Leads\n\n"
                    "Moderate commercial signals with some stack alignment.\n\n"
                    (if (seq warm)
                      (str/join (map format-lead
                                     (take 15 (sort-by #(- (:combined-score %)) warm))))
                      "_No warm leads found._\n\n")

                    "## Aligned but Uncommercial\n\n"
                    "Strong stack alignment but limited commercial signals. Potential open-source\n"
                    "collaboration or pro-bono consulting that builds portfolio.\n\n"
                    (if (seq aligned)
                      (str/join (map format-lead
                                     (take 10 (sort-by #(- (:combined-score %)) aligned))))
                      "_None found._\n\n")

                    "## Gap Opportunities\n\n"
                    "High-adoption repos with no commercial extraction — potential demand.\n\n"
                    (str/join (map format-gap (take 15 gaps)))
                    "\n"

                    "---\n\n"
                    "*Report generated by futon7 landscape intelligence. "
                    "Lead classification uses commercial signal extraction "
                    "(license, README keywords, FUNDING.yml, homepage) crossed with "
                    "stack alignment scoring (keyword matching against futon capabilities).*\n")]

    (io/make-parents (io/file output-file))
    (spit output-file report)
    (println (str "Report written to " output-file))
    (println (str "  Hot: " (count hot)
                  "  Warm: " (count warm)
                  "  Aligned: " (count aligned)
                  "  Gaps: " (count gaps)))
    {:hot (count hot) :warm (count warm)
     :aligned (count aligned) :gaps (count gaps)}))

;; ---------------------------------------------------------------------------
;; CLI entry point
;; ---------------------------------------------------------------------------

(defn -main [& args]
  (let [input (or (first args) "data/results/stack-grounded-run.edn")
        output (or (second args) "data/results/lead-report.md")]
    (generate-report input output)))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
