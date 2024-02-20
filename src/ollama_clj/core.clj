(ns ollama-clj.core
  ;; (:exclude [list]) ;; for list
  (:require [clojure.string :as str]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [manifold.stream :as stream]
            [jsonista.core :as json]
            [ollama-clj.schema :as s]
            [malli.core :as m]))

(def user-agent-string ;; TODO
  (format "ollama-clj/%s (%s %s) with Clojure version %s" ollama-version machine os-version (clojure-version)))

(def base-config
  {:base-url (or (System/getenv "OLLAMA_BASE_URL")
                 "http://localhost:11434")
   :follow-redirects? true
   :timeout nil
   :headers {"Content-Type" "application/json"
             "Accept"       "application/json"
             "User-Agent"   "ollama-clj"}})

(defprotocol BaseClient
  (request        [this method endpoint opts])
  (stream         [this method endpoint opts])
  (request-stream [this method endpoint opts]))

(defn- resolve-method [method]
  (ns-resolve 'aleph.http (symbol (name method))))

(defrecord Client [^String base-url]
  BaseClient
  (request [_this method endpoint {:keys [] :as opts}]
    (let [method (resolve-method method)]
      (-> (str base-url endpoint)
          (method {:headers {"Content-Type" "application/json"}
                   :body (json/write-value-as-string opts)})
          (d/chain #(println "Response:" @(d/deferred @%))))))

  (stream [_this method endpoint {:keys [] :as opts}]
    (let [method (resolve-method method)]
      (-> (str base-url endpoint)
          (method {:headers {"Content-Type" "application/json"}
                   :body (json/write-value-as-string opts)
                   :stream? true})
          (d/chain (fn [{:keys [body] :as _resp}]
                     (stream/consume
                      (fn [chunk]
                        (println "Received chunk:" chunk))
                      body))))))

  (request-stream [this method endpoint {:keys [stream?] :as opts}]
    (if stream?
      (stream this method endpoint opts)
      (request this method endpoint opts))))

(comment
  ;; What do you think about instatiating base client for the user?
  (def base-client (->Client "http://localhost:11434"))
  )

(defn chat
  ([client model messages]
   (chat client model messages {}))
  ([client model messages opts]
   (request-stream client
                   :post
                   "/api/chat"
                   (assoc opts
                          :model model
                          :messages messages))))

(defn generate
  ([client model prompt]
   (generate client model prompt {:stream? false
                                  :raw? false}))
  ([client model prompt opts]
   (request-stream client
                   :post
                   "/api/generate"
                   (assoc opts
                          :model model
                          :prompt prompt))))

(defn embeddings
  ([client model prompt]
   (embeddings client model prompt {}))
  ([client model prompt opts]
   (request client :post "/api/embeddings" (assoc opts :model model :prompt prompt))))

(defn pull
  ([client model]
   (pull client model {:insecure? false :stream? false}))
  ([client model opts]
   (request-stream client :post "/api/pull" (assoc opts :model model))))

(defn push
  ([client model]
   (push client model {:insecure? false :stream? false}))
  ([client model opts]
   (request-stream client :post "/api/push" (assoc opts :model model))))

(defn create
  ([client model]
   (create client model {:stream? false
                         :path nil
                         :modelfile nil}))
  ([client model opts]
   (request-stream client :post "/api/create" (assoc opts :model model))))

(defn- parse-modelfile
  ([modelfile] (parse-modelfile modelfile nil))
  ([modelfile base]
   (throw (Exception. "Not implemented"))))

(defn- create-blob [path]
  (throw (Exception. "Not implemented")))

(defn delete [client]
  (let [response (request client :delete "/api/delete" {})]
    (if (= 200 (:status response))
      {:status "success"}
      {:status "failure"})))

(defn list [client]
  (request client :get "/api/tags" {}))

(defn show
  [client
   {:keys [model]
    :or {model ""}
    :as opts}]
  {:pre [(not (str/blank? model))]}
  (request client :post "/api/show" opts))

(defn copy
  [client opts]
  {:pre [(m/validate s/Copy opts)]}
  (let [response (request client :post "/api/copy" opts)]
    (if (= 200 (:status response))
      {:status "success"}
      {:status "failure"})))
