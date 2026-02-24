(ns f7.signals
  "Pure signal extraction. Takes an enriched repo map, returns signal vectors.
   No side effects — all GitHub fetching happens in f7.gh."
  (:require [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; License signals
;; ---------------------------------------------------------------------------

(def ^:private commercial-licenses
  "Licenses that indicate monetisation intent (non-OSI-approved or dual-license)."
  #{"NOASSERTION"  ;; often BSL, SSPL, RSALv2 — GitHub can't classify
    "BSL-1.1"      ;; Business Source License (MariaDB, CockroachDB, Sentry)
    "SSPL-1.0"     ;; Server Side Public License (MongoDB, Elastic briefly)
    "ELv2"         ;; Elastic License v2
    "Elastic-2.0"  ;; alternate SPDX
    nil})          ;; no license at all can signal proprietary/commercial

(def ^:private copyleft-licenses
  "Strong copyleft — not commercial signal per se, but affects business model."
  #{"AGPL-3.0" "AGPL-3.0-only" "AGPL-3.0-or-later" "GPL-3.0" "GPL-3.0-only"})

(defn- license-signal [{:keys [license license-name]}]
  (cond
    (contains? commercial-licenses license)
    {:signal :commercial-license :present? true
     :detail (str "License: " (or license "none") " (" (or license-name "unknown") ")")}

    (contains? copyleft-licenses license)
    {:signal :copyleft-license :present? true
     :detail (str "Copyleft: " license)}

    :else
    {:signal :commercial-license :present? false :detail (str "License: " license)}))

;; ---------------------------------------------------------------------------
;; README keyword signals
;; ---------------------------------------------------------------------------

(def ^:private enterprise-keywords
  "Keywords in README that suggest commercial/enterprise offering."
  #{"enterprise" "pricing" "commercial license" "commercial support"
    "pro version" "pro plan" "premium" "saas" "self-hosted"
    "on-premise" "on-prem" "support contract" "business plan"
    "team plan" "cloud-hosted" "managed service"})

(def ^:private enterprise-pattern
  (re-pattern
   (str "(?i)\\b("
        (str/join "|" (map #(str/replace % " " "\\\\s+") enterprise-keywords))
        ")\\b")))

(defn- readme-enterprise-signal [{:keys [readme]}]
  (if (str/blank? readme)
    {:signal :enterprise-readme :present? false :detail "No README"}
    (let [matches (re-seq enterprise-pattern readme)
          found (set (map (comp str/lower-case first) matches))]
      {:signal :enterprise-readme
       :present? (pos? (count found))
       :detail (if (seq found)
                 (str "Keywords: " (str/join ", " (sort found)))
                 "No enterprise keywords")
       :match-count (count found)
       :matches found})))

;; ---------------------------------------------------------------------------
;; Funding signals
;; ---------------------------------------------------------------------------

(defn- funding-signal [{:keys [funding]}]
  (if (str/blank? funding)
    {:signal :funding-yml :present? false :detail "No FUNDING.yml"}
    (let [lines (str/split-lines funding)
          active (->> lines
                      (remove #(str/starts-with? (str/trim %) "#"))
                      (filter #(let [parts (str/split % #":" 2)]
                                 (and (= 2 (count parts))
                                      (not (str/blank? (second parts))))))
                      (map #(str/trim (first (str/split % #":" 2)))))]
      {:signal :funding-yml
       :present? true
       :detail (str "Funding platforms: " (str/join ", " active))
       :platforms (set active)})))

;; ---------------------------------------------------------------------------
;; Homepage signal
;; ---------------------------------------------------------------------------

(defn- homepage-signal [{:keys [homepage full-name]}]
  (let [has-homepage (and (not (str/blank? homepage))
                          (not (str/includes? (str/lower-case (or homepage ""))
                                              "github.com"))
                          (not (str/includes? (str/lower-case (or homepage ""))
                                              "github.io")))]
    {:signal :commercial-homepage
     :present? has-homepage
     :detail (if has-homepage
               (str "Homepage: " homepage)
               "No commercial homepage")}))

;; ---------------------------------------------------------------------------
;; Soft signals
;; ---------------------------------------------------------------------------

(defn- fork-ratio-signal [{:keys [fork-ratio stars forks]}]
  (cond
    (nil? fork-ratio)
    {:signal :fork-ratio :present? false :detail "No data"}

    (> fork-ratio 0.1)
    {:signal :high-fork-ratio :present? true
     :detail (str "Fork:star " (format "%.2f" fork-ratio)
                  " (" forks "/" stars ") — suggests production use")}

    (< fork-ratio 0.02)
    {:signal :low-fork-ratio :present? true
     :detail (str "Fork:star " (format "%.2f" fork-ratio)
                  " — hobbyist interest pattern")}

    :else
    {:signal :fork-ratio :present? false
     :detail (str "Fork:star " (format "%.2f" fork-ratio) " — neutral")}))

;; ---------------------------------------------------------------------------
;; Anti-signals
;; ---------------------------------------------------------------------------

(defn- archived-signal [{:keys [archived]}]
  {:signal :archived :present? (boolean archived)
   :detail (if archived "Archived — abandoned" "Active")})

;; ---------------------------------------------------------------------------
;; Public API
;; ---------------------------------------------------------------------------

(def all-signal-fns
  "All signal extraction functions."
  [license-signal
   readme-enterprise-signal
   funding-signal
   homepage-signal
   fork-ratio-signal
   archived-signal])

(defn extract-signals
  "Run all signal extractors against an enriched repo map.
   Returns {:hard [...] :soft [...] :anti [...] :all [...]}."
  [repo]
  (let [results (mapv #(% repo) all-signal-fns)
        present (filter :present? results)
        hard (filter #(#{:commercial-license :enterprise-readme
                         :funding-yml :commercial-homepage} (:signal %))
                     present)
        soft (filter #(#{:high-fork-ratio :copyleft-license} (:signal %))
                     present)
        anti (filter #(#{:archived :low-fork-ratio} (:signal %))
                     present)]
    {:hard (vec hard)
     :soft (vec soft)
     :anti (vec anti)
     :all (vec results)
     :hard-count (count hard)
     :soft-count (count soft)
     :anti-count (count anti)
     :score (- (+ (count hard) (* 0.5 (count soft)))
               (* 2 (count anti)))}))
