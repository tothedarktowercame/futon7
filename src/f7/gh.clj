(ns f7.gh
  "GitHub CLI wrapper. Calls `gh api` via shell, parses JSON, caches to EDN.
   All public functions return {:ok true :data ...} or {:ok false :error ...}."
  (:require [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

;; ---------------------------------------------------------------------------
;; Cache
;; ---------------------------------------------------------------------------

(def ^:private cache-dir "data/cache")

(defn- cache-path [key]
  (str cache-dir "/" (str/replace key #"[/:]" "__") ".edn"))

(defn- cache-read [key]
  (let [f (io/file (cache-path key))]
    (when (.exists f)
      (edn/read-string (slurp f)))))

(defn- cache-write! [key data]
  (let [f (io/file (cache-path key))]
    (io/make-parents f)
    (spit f (pr-str data))
    data))

;; ---------------------------------------------------------------------------
;; gh CLI helpers
;; ---------------------------------------------------------------------------

(defn- gh-api
  "Call `gh api <endpoint>` with optional query params.
   Returns parsed JSON or {:ok false :error ...}."
  [endpoint & {:keys [params method] :or {method "GET"}}]
  (let [args (into ["gh" "api" endpoint "-X" method
                     "--header" "Accept: application/vnd.github+json"]
                   (mapcat (fn [[k v]] ["-f" (str (name k) "=" v)]) params))
        {:keys [exit out err]} (apply shell/sh args)]
    (if (zero? exit)
      {:ok true :data (json/parse-string (str/trim out) true)}
      {:ok false :error (str/trim err) :endpoint endpoint})))

(defn- gh-api-raw
  "Like gh-api but returns raw string body (for base64-encoded content)."
  [endpoint]
  (let [{:keys [exit out err]} (shell/sh "gh" "api" endpoint
                                         "-X" "GET"
                                         "--header" "Accept: application/vnd.github+json")]
    (if (zero? exit)
      {:ok true :data (json/parse-string (str/trim out) true)}
      {:ok false :error (str/trim err) :endpoint endpoint})))

;; ---------------------------------------------------------------------------
;; Public API
;; ---------------------------------------------------------------------------

(defn search-repos
  "Search GitHub repos. Returns {:ok true :data [{:full_name :stars ...}]}.
   Query uses GitHub search syntax, e.g. 'database stars:>1000'."
  [query & {:keys [sort per-page] :or {sort "stars" per-page 30}}]
  (let [cache-key (str "search/" (str/replace query #"\s+" "_") "__" sort "__" per-page)]
    (or (when-let [cached (cache-read cache-key)]
          {:ok true :data cached :cached true})
        (let [result (gh-api "search/repositories"
                             :params {:q query :sort sort :per_page (str per-page)})]
          (if (:ok result)
            (let [items (mapv (fn [item]
                                {:full-name (:full_name item)
                                 :owner (get-in item [:owner :login])
                                 :name (:name item)
                                 :description (:description item)
                                 :stars (:stargazers_count item)
                                 :forks (:forks_count item)
                                 :license (get-in item [:license :spdx_id] )
                                 :homepage (:homepage item)
                                 :topics (:topics item)
                                 :archived (:archived item)
                                 :fork-ratio (when (and (:stargazers_count item)
                                                        (pos? (:stargazers_count item)))
                                               (double (/ (:forks_count item)
                                                          (:stargazers_count item))))})
                              (:items (:data result)))]
              (cache-write! cache-key items)
              {:ok true :data items})
            result)))))

(defn repo-metadata
  "Fetch full metadata for a single repo."
  [owner repo]
  (let [cache-key (str "meta/" owner "/" repo)]
    (or (when-let [cached (cache-read cache-key)]
          {:ok true :data cached :cached true})
        (let [result (gh-api (str "repos/" owner "/" repo))]
          (if (:ok result)
            (let [d (:data result)
                  m {:full-name (:full_name d)
                     :owner (get-in d [:owner :login])
                     :name (:name d)
                     :description (:description d)
                     :stars (:stargazers_count d)
                     :forks (:forks_count d)
                     :license (get-in d [:license :spdx_id])
                     :license-name (get-in d [:license :name])
                     :homepage (:homepage d)
                     :topics (:topics d)
                     :archived (:archived d)
                     :language (:language d)
                     :created-at (:created_at d)
                     :updated-at (:updated_at d)
                     :open-issues (:open_issues_count d)
                     :fork-ratio (when (and (:stargazers_count d)
                                            (pos? (:stargazers_count d)))
                                   (double (/ (:forks_count d)
                                              (:stargazers_count d))))}]
              (cache-write! cache-key m)
              {:ok true :data m})
            result)))))

(defn repo-readme-text
  "Fetch decoded README text for a repo. Returns nil-safe."
  [owner repo]
  (let [cache-key (str "readme/" owner "/" repo)]
    (or (when-let [cached (cache-read cache-key)]
          {:ok true :data cached :cached true})
        (let [result (gh-api-raw (str "repos/" owner "/" repo "/readme"))]
          (if (:ok result)
            (let [content (get-in result [:data :content])
                  ;; GitHub returns base64-encoded content with newlines
                  decoded (when content
                            (try
                              (String. (.decode (java.util.Base64/getMimeDecoder)
                                                content))
                              (catch Exception _ nil)))]
              (cache-write! cache-key decoded)
              {:ok true :data decoded})
            ;; No README is not an error, just nil
            (do (cache-write! cache-key nil)
                {:ok true :data nil}))))))

(defn repo-funding
  "Fetch .github/FUNDING.yml contents. Returns parsed string or nil."
  [owner repo]
  (let [cache-key (str "funding/" owner "/" repo)]
    (or (when-let [cached (cache-read cache-key)]
          {:ok true :data cached :cached true})
        (let [result (gh-api-raw (str "repos/" owner "/" repo
                                      "/contents/.github/FUNDING.yml"))]
          (if (:ok result)
            (let [content (get-in result [:data :content])
                  decoded (when content
                            (try
                              (String. (.decode (java.util.Base64/getMimeDecoder)
                                                content))
                              (catch Exception _ nil)))]
              (cache-write! cache-key decoded)
              {:ok true :data decoded})
            (do (cache-write! cache-key nil)
                {:ok true :data nil}))))))

(defn enrich-repo
  "Given a repo map (from search-repos), fetch readme + funding + full metadata.
   Returns the repo map with :readme, :funding, and extra metadata merged."
  [{:keys [owner name] :as repo}]
  (let [meta-result (repo-metadata owner name)
        readme-result (repo-readme-text owner name)
        funding-result (repo-funding owner name)]
    (merge repo
           (when (:ok meta-result) (:data meta-result))
           {:readme (:data readme-result)
            :funding (:data funding-result)})))

(defn rate-limit
  "Check current GitHub API rate limit status."
  []
  (let [{:keys [exit out]} (shell/sh "gh" "api" "rate_limit")]
    (when (zero? exit)
      (let [parsed (json/parse-string (str/trim out) true)]
        {:core (get-in parsed [:resources :core])
         :search (get-in parsed [:resources :search])}))))
