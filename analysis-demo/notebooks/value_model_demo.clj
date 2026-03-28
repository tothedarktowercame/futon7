(ns value-model-demo
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [scicloj.kindly.v4.kind :as kind]
            [fastmath.random :as fr]))

;; # Hyperreal Enterprises Value-Flow Model
;;
;; This notebook reads the value network from `assumptions.edn` and
;; invoice observations from `invoices.edn`, then:
;;
;; 1. Renders the prior predictive model (forward sampling from priors)
;; 2. Summarises invoice evidence per edge
;; 3. Generates a unified comparison figure and tradeoff scatter
;;
;; The structure mirrors the VSAT wiki's `analysis-demo/` pipeline.

(def assumptions (-> "assumptions.edn" slurp edn/read-string))
(def invoice-data (-> "invoices.edn" slurp edn/read-string))

(def scenarios (:scenarios assumptions))
(def network-nodes (-> assumptions :value-network :nodes))
(def node-by-id (into {} (map (juxt :id identity) network-nodes)))

(def group-order (or (:group-order assumptions)
                     [:central :capability :network :public]))
(def group-label (or (:group-label assumptions)
                     {:central "Central capture"
                      :capability "Partner capability formation"
                      :network "Cross-project transfer"
                      :public "Public / ecosystem benefit"}))

;; =====================================================================
;; Utility functions (ported from VSAT notebook)
;; =====================================================================

(defn pounds [n]
  (format "GBP %,d" (long (Math/round (double n)))))

(defn md-table [headers rows]
  (let [header-row (str "| " (str/join " | " headers) " |")
        rule-row   (str "| " (str/join " | " (repeat (count headers) "---")) " |")
        body-rows  (map (fn [row] (str "| " (str/join " | " row) " |")) rows)]
    (str/join "\n" (concat [header-row rule-row] body-rows))))

(defn svg-escape [s]
  (-> (str s)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn slugify [s]
  (-> s str/lower-case (str/replace #"[^a-z0-9]+" "-") (str/replace #"(^-+|-+$)" "")))

;; =====================================================================
;; Financial computation
;; =====================================================================

(defn scenario->financials [{:keys [billable-hours hourly-rate other-revenue fixed-cost]
                             :as scenario}]
  (let [billable-revenue (* billable-hours hourly-rate)
        total (+ billable-revenue other-revenue)
        surplus (- total fixed-cost)]
    (assoc scenario
           :billable-revenue billable-revenue
           :total-revenue total
           :operating-surplus surplus)))

(defn scenario->value-dimensions [{:keys [value-flows] :as scenario}]
  (let [totals (reduce
                 (fn [acc {:keys [to weight]}]
                   (let [grp (-> to node-by-id :group)]
                     (if (contains? (set group-order) grp)
                       (update acc grp (fnil + 0) weight)
                       acc)))
                 (zipmap group-order (repeat 0))
                 value-flows)]
    (merge scenario totals)))

(def results
  (mapv (comp scenario->value-dimensions scenario->financials) scenarios))

;; ## Scenario overview

^:kindly/hide-code
(kind/md
 (md-table
  ["Scenario" "Period" "Engagements" "Billable hours" "Other revenue" "Total revenue" "Surplus"]
  (map (fn [{:keys [model period engagements billable-hours other-revenue total-revenue operating-surplus]}]
         [model period (str engagements) (str billable-hours)
          (pounds other-revenue) (pounds total-revenue) (pounds operating-surplus)])
       results)))

;; ## Value dimensions (point estimates)

^:kindly/hide-code
(kind/md
 (md-table
  (into ["Scenario"] (map group-label group-order))
  (map (fn [r]
         (into [(:model r)] (map #(str (get r %)) group-order)))
       results)))

;; =====================================================================
;; Invoice evidence summary
;; =====================================================================

;; ## Invoice evidence

^:kindly/hide-code
(let [all-lines (mapcat :lines (:invoices invoice-data))
      total-hours (reduce + 0 (map :hours all-lines))
      total-revenue (reduce + 0.0 (map :subtotal all-lines))
      by-type (group-by :service-type all-lines)
      type-summary (map (fn [[stype lines]]
                          [(name stype)
                           (str (count lines))
                           (format "%.1f" (reduce + 0.0 (map :hours lines)))
                           (pounds (reduce + 0.0 (map :subtotal lines)))])
                        (sort-by key by-type))]
  (kind/md
   (str "**Total invoiced:** " (pounds total-revenue)
        " over " (format "%.1f" total-hours) " hours\n\n"
        (md-table ["Service type" "Line items" "Hours" "Revenue"] type-summary))))

;; ## Edge activation evidence
;;
;; Each invoice line activates edges in the value network.
;; This table aggregates the total observed weight per edge.

^:kindly/hide-code
(let [all-lines (mapcat :lines (:invoices invoice-data))
      all-activations (mapcat :edge-activations all-lines)
      by-edge (group-by :edge all-activations)
      edge-totals (sort-by (comp - second)
                    (map (fn [[edge acts]]
                           [edge (reduce + 0 (map :observed-weight acts))])
                         by-edge))]
  (kind/md
   (md-table ["Edge" "Total observed weight" "Observations"]
     (map (fn [[[from to] total]]
            [(str (name from) " → " (name to))
             (str total)
             (str (count (get by-edge [from to])))])
          edge-totals))))

;; =====================================================================
;; Bayesian prior predictive model
;; =====================================================================

;; ## Prior predictive model
;;
;; Same approach as the VSAT model: Poisson priors on engagement volume,
;; Gamma priors on value-flow weights. Forward-sample to see the
;; distribution of outcomes under each scenario.

(def n-mc-samples 5000)

(defn poisson-prior [lambda]
  (when (pos? lambda)
    (fr/distribution :poisson {:p lambda})))

(defn gamma-prior [mean cv]
  (let [shape (/ 1.0 (* cv cv))
        scale (* mean cv cv)]
    (fr/distribution :gamma {:shape shape :scale scale})))

(defn draw-volume [prior point-estimate]
  (if prior
    (max 0 (long (Math/round (double (fr/sample prior)))))
    (long point-estimate)))

(defn forward-sample-scenario
  [{:keys [engagements billable-hours hourly-rate other-revenue fixed-cost
           value-flows] :as scenario}]
  (let [n-eng (draw-volume (poisson-prior engagements) engagements)
        ;; Scale billable hours proportionally to engagement count
        hours-per-eng (if (pos? engagements) (/ (double billable-hours) engagements) 0.0)
        sampled-hours (* n-eng hours-per-eng)
        billable-rev (* sampled-hours hourly-rate)
        ;; Other revenue also uncertain (log-normal around point estimate)
        sampled-other (if (pos? other-revenue)
                        (fr/sample (fr/distribution :log-normal
                                    {:scale (Math/log (max 1 other-revenue))
                                     :shape 0.3}))
                        0.0)
        total (+ billable-rev sampled-other)
        surplus (- total fixed-cost)
        ;; Value-flow weights with gamma priors
        sampled-flows (mapv (fn [{:keys [weight] :as flow}]
                              (assoc flow :weight
                                     (fr/sample (gamma-prior (max 0.5 weight) 0.3))))
                            value-flows)
        dims (reduce
               (fn [acc {:keys [to weight]}]
                 (let [grp (-> to node-by-id :group)]
                   (if (contains? (set group-order) grp)
                     (update acc grp (fnil + 0.0) weight)
                     acc)))
               (zipmap group-order (repeat 0.0))
               sampled-flows)]
    (merge {:total-revenue (double total)
            :operating-surplus (double surplus)}
           dims)))

(def mc-results
  (mapv (fn [scenario]
          {:model (:model scenario)
           :samples (vec (repeatedly n-mc-samples
                                     #(forward-sample-scenario scenario)))})
        scenarios))

(defn pctl [sorted-values p]
  (let [idx (min (int (* (count sorted-values) (/ p 100.0)))
                 (dec (count sorted-values)))]
    (nth sorted-values idx)))

(defn summarize-key [samples k]
  (let [values (vec (sort (mapv k samples)))]
    {:mean   (/ (reduce + 0.0 values) (count values))
     :median (pctl values 50)
     :p10    (pctl values 10)
     :p90    (pctl values 90)}))

;; ## Prior predictive summary

^:kindly/hide-code
(kind/md
 (md-table
  ["Scenario" "Revenue (median)" "Revenue (80% CI)" "Surplus (median)" "Surplus (80% CI)"]
  (map (fn [{:keys [model samples]}]
         (let [rev (summarize-key samples :total-revenue)
               sur (summarize-key samples :operating-surplus)]
           [model
            (pounds (:median rev))
            (str (pounds (:p10 rev)) " – " (pounds (:p90 rev)))
            (pounds (:median sur))
            (str (pounds (:p10 sur)) " – " (pounds (:p90 sur)))]))
       mc-results)))

;; =====================================================================
;; Unified value-network figure
;; =====================================================================

(def scenario-colors ["#4C78A8" "#F58518" "#E45756"])

(def compact-nodes
  [{:id :futon-stack          :label "FUTON stack"  :x 35  :y 45   :group :upstream}
   {:id :method-validation    :label "Validation"   :x 35  :y 135  :group :upstream}
   {:id :optionality-bank     :label "Optionality"  :x 35  :y 225  :group :upstream}
   {:id :vsat-work            :label "VSAT"         :x 145 :y 40   :group :alignment}
   {:id :ukrns-work           :label "UKRNS"        :x 145 :y 120  :group :alignment}
   {:id :other-engagements    :label "Other"        :x 145 :y 210  :group :alignment}
   {:id :revenue              :label "Revenue"      :x 255 :y 30   :group :catalyst}
   {:id :client-capability    :label "Client cap."  :x 255 :y 100  :group :downstream}
   {:id :published-knowledge  :label "Published"    :x 255 :y 180  :group :downstream}
   {:id :ecosystem-benefit    :label "Ecosystem"    :x 255 :y 260  :group :downstream}])

(def compact-by-id (into {} (map (juxt :id identity) compact-nodes)))

(defn unified-edge-color [to-id]
  (get {:upstream "#4A8C6F" :alignment "#4A6F8C" :catalyst "#D4A843"
        :downstream "#7B5EA7"}
       (:group (compact-by-id to-id)) "#888888"))

(defn unified-node-fill [grp]
  (get {:upstream "#D0E8D6" :alignment "#D0DDE8" :catalyst "#F2E5C4"
        :downstream "#E0D6E8"} grp "#E8E8E8"))

(defn write-unified-value-network! [scenarios source-path]
  (let [file (io/file source-path)
        _ (.mkdirs (.getParentFile file))
        panel-w 290 panel-h 280
        gap 15 top-pad 16 bottom-pad 48 right-pad 30
        width (+ (* 3 panel-w) (* 2 gap) right-pad)
        height (+ top-pad panel-h bottom-pad)
        nr 13
        all-flows (mapcat :value-flows scenarios)
        max-weight (apply max 1 (map :weight all-flows))
        edge-w (fn [w] (+ 0.8 (* 4.0 (/ (double w) (double max-weight)))))
        edge-opacity (fn [w] (+ 0.25 (* 0.45 (/ (double w) (double max-weight)))))
        all-edges (distinct (mapcat (fn [s] (map (fn [{:keys [from to]}] [from to]) (:value-flows s))) scenarios))
        panels
        (apply str
          (map-indexed
            (fn [pidx scenario]
              (let [ox (* pidx (+ panel-w gap))
                    oy top-pad
                    flow-map (into {} (map (fn [{:keys [from to weight]}] [[from to] weight]) (:value-flows scenario)))
                    edges-svg
                    (apply str
                      (for [[from to] all-edges
                            :let [w (get flow-map [from to] 0)
                                  {x1 :x y1 :y} (compact-by-id from)
                                  {x2 :x y2 :y} (compact-by-id to)]]
                        (if (pos? w)
                          (str "<line x1=\"" (+ ox x1) "\" y1=\"" (+ oy y1)
                               "\" x2=\"" (+ ox x2) "\" y2=\"" (+ oy y2)
                               "\" stroke=\"" (unified-edge-color to)
                               "\" stroke-width=\"" (edge-w w)
                               "\" stroke-opacity=\"" (edge-opacity w) "\" />")
                          (str "<line x1=\"" (+ ox x1) "\" y1=\"" (+ oy y1)
                               "\" x2=\"" (+ ox x2) "\" y2=\"" (+ oy y2)
                               "\" stroke=\"#CCCCCC\" stroke-width=\"0.5\""
                               " stroke-opacity=\"0.3\" stroke-dasharray=\"3,4\" />"))))
                    nodes-svg
                    (apply str
                      (for [{:keys [label x y group]} compact-nodes
                            :let [cx (+ ox x) cy (+ oy y)
                                  fill (unified-node-fill group)
                                  stroke (get {:upstream "#4A8C6F" :alignment "#4A6F8C"
                                               :catalyst "#D4A843" :downstream "#7B5EA7"} group "#999")]]
                        (str "<circle cx=\"" cx "\" cy=\"" cy "\" r=\"" nr
                             "\" fill=\"" fill "\" stroke=\"" stroke "\" stroke-width=\"1.2\" />"
                             "<text x=\"" cx "\" y=\"" (+ cy nr 11)
                             "\" text-anchor=\"middle\" font-family=\"sans-serif\" font-size=\"9\" fill=\"#222\">"
                             (svg-escape label) "</text>")))
                    scolor (nth scenario-colors pidx)
                    title-cx (+ ox (/ panel-w 2))
                    title-y (+ oy panel-h 18)
                    title-svg
                    (str "<circle cx=\"" (- title-cx (+ 6 (/ (* 6.0 (count (:model scenario))) 2)))
                         "\" cy=\"" (- title-y 4) "\" r=\"5\" fill=\"" scolor "\" stroke=\"#222\" stroke-width=\"0.8\" />"
                         "<text x=\"" title-cx "\" y=\"" title-y
                         "\" text-anchor=\"middle\" font-family=\"sans-serif\" font-size=\"11\" fill=\"#222\">"
                         (svg-escape (:model scenario)) "</text>")]
                (str edges-svg nodes-svg title-svg)))
            scenarios))
        legend-y (- height 12)
        legend-items [[:upstream "Capability growth" "#4A8C6F"]
                      [:alignment "Alignment zone" "#4A6F8C"]
                      [:catalyst "Revenue (catalyst)" "#D4A843"]
                      [:downstream "Downstream benefit" "#7B5EA7"]]
        legend-svg
        (apply str
          (map-indexed
            (fn [i [_ lbl col]]
              (let [lx (+ 20 (* i 220))]
                (str "<rect x=\"" lx "\" y=\"" (- legend-y 9) "\" width=\"12\" height=\"12\""
                     " fill=\"" col "\" fill-opacity=\"0.6\" stroke=\"" col "\" />"
                     "<text x=\"" (+ lx 18) "\" y=\"" legend-y
                     "\" font-family=\"sans-serif\" font-size=\"10\" fill=\"#444\">"
                     (svg-escape lbl) "</text>")))
            legend-items))
        svg (str "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" width "\" height=\"" height
                 "\" viewBox=\"0 0 " width " " height "\">"
                 "<rect width=\"100%\" height=\"100%\" fill=\"#FAFAFA\" />"
                 panels legend-svg
                 "</svg>")]
    (spit file svg)))

;; ## Unified value-flow network

^:kindly/hide-code
(write-unified-value-network! scenarios "images/value-network-unified.svg")

^:kindly/hide-code
(kind/md "![Unified value-flow network](images/value-network-unified.svg)")

;; ## Capability growth versus revenue (prior predictive)

^:kindly/hide-code
(let [file (io/file "images/bayesian-tradeoff.svg")
      _ (.mkdirs (.getParentFile file))
      width 860 height 400
      ml 88 mr 48 mt 60 mb 56
      pw (- width ml mr) ph (- height mt mb)
      all-rev (mapcat (fn [{:keys [samples]}] (map :total-revenue samples)) mc-results)
      all-cap (mapcat (fn [{:keys [samples]}] (map :upstream samples)) mc-results)
      x-max (double (apply max 1 all-rev))
      y-max (double (apply max 1 all-cap))
      sx (fn [v] (+ ml (* pw (/ (double v) x-max))))
      sy (fn [v] (- height mb (* ph (/ (double v) y-max))))
      n-render 400
      points (apply str
               (map-indexed
                 (fn [idx {:keys [samples]}]
                   (let [subset (take n-render (shuffle samples))
                         color (nth scenario-colors idx)]
                     (apply str
                       (map (fn [s]
                              (str "<circle cx=\"" (sx (:total-revenue s))
                                   "\" cy=\"" (sy (:upstream s))
                                   "\" r=\"2\" fill=\"" color "\" fill-opacity=\"0.12\" />"))
                            subset))))
                 mc-results))
      medians (apply str
                (map-indexed
                  (fn [idx {:keys [model samples]}]
                    (let [rm (:median (summarize-key samples :total-revenue))
                          cm (:median (summarize-key samples :upstream))
                          color (nth scenario-colors idx)
                          x (sx rm) y (sy cm)
                          pl? (> (+ x 160) (- width mr))
                          lx (if pl? (- x 10) (+ x 10))
                          anchor (if pl? "end" "start")]
                      (str "<circle cx=\"" x "\" cy=\"" y
                           "\" r=\"6\" fill=\"" color "\" stroke=\"#222\" stroke-width=\"1.5\" />"
                           "<text x=\"" lx "\" y=\"" (- y 4)
                           "\" text-anchor=\"" anchor "\" font-size=\"11\" fill=\"#222\">"
                           (svg-escape model) "</text>")))
                  mc-results))
      x-step (/ x-max 4.0) y-step (max 1.0 (* y-max 0.2))
      grid-x (apply str
               (for [i (range 5) :let [v (* i x-step) x (sx v)]]
                 (str "<line x1=\"" x "\" y1=\"" mt "\" x2=\"" x "\" y2=\"" (- height mb) "\" stroke=\"#e5e7eb\" />"
                      "<text x=\"" x "\" y=\"" (- height 20) "\" text-anchor=\"middle\" font-size=\"12\" fill=\"#555\">"
                      (svg-escape (pounds v)) "</text>")))
      grid-y (apply str
               (for [i (range 6) :let [v (* i y-step) y (sy v)]]
                 (str "<line x1=\"" ml "\" y1=\"" y "\" x2=\"" (- width mr) "\" y2=\"" y "\" stroke=\"#e5e7eb\" />"
                      "<text x=\"68\" y=\"" (+ y 4) "\" text-anchor=\"end\" font-size=\"12\" fill=\"#555\">"
                      (format "%.0f" v) "</text>")))
      svg (str "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" width "\" height=\"" height
               "\" viewBox=\"0 0 " width " " height "\">"
               "<rect width=\"100%\" height=\"100%\" fill=\"white\" />"
               "<text x=\"12\" y=\"28\" font-size=\"18\" fill=\"#111\">Capability growth vs revenue (prior predictive)</text>"
               "<text x=\"12\" y=\"46\" font-size=\"12\" fill=\"#555\">Each dot is one forward sample; filled circle is the median</text>"
               grid-x grid-y
               "<line x1=\"" ml "\" y1=\"" (- height mb) "\" x2=\"" (- width mr) "\" y2=\"" (- height mb) "\" stroke=\"#111\" />"
               "<line x1=\"" ml "\" y1=\"" mt "\" x2=\"" ml "\" y2=\"" (- height mb) "\" stroke=\"#111\" />"
               "<text x=\"" (/ width 2) "\" y=\"" (- height 8) "\" text-anchor=\"middle\" font-size=\"13\" fill=\"#111\">Revenue (per period)</text>"
               "<text x=\"22\" y=\"" (/ height 2) "\" transform=\"rotate(-90 22 " (/ height 2)
               ")\" text-anchor=\"middle\" font-size=\"13\" fill=\"#111\">Capability growth (upstream)</text>"
               points medians
               "</svg>")]
  (spit file svg))

^:kindly/hide-code
(kind/md "![Revenue vs partner capability](images/bayesian-tradeoff.svg)")

;; ## Reading the output
;;
;; The three scenarios trace a growth trajectory rather than competing
;; alternatives (unlike the VSAT model, where services-first vs software-first
;; were genuinely different strategies). Here the question is: does the
;; trajectory from current-state through year-1 to year-2 produce
;; increasing partner capability alongside increasing revenue, or does
;; one crowd out the other?
;;
;; The invoice evidence provides a first anchor: Q1 2026 shows which
;; edges actually activated and at what strength. As more invoices
;; accumulate, these can be used as likelihood observations for posterior
;; inference, sharpening the priors on edge weights and engagement volumes.
