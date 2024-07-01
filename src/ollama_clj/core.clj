(ns ollama-clj.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [aleph.http :as http]
            [clj-commons.byte-streams :as bs]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [jsonista.core :as json]

            [ollama-clj.util :as u]))

(defprotocol BaseClient
  (request        [this method endpoint opts])
  (stream         [this method endpoint opts])
  (request-stream [this method endpoint opts]))

(defn- resolve-method [method]
  (ns-resolve 'aleph.http (symbol (name method))))

(defrecord Client [^String base-url ^String model]
  BaseClient
  (request [_this method endpoint opts]
    (let [url    (str base-url endpoint)
          method (resolve-method method)
          opts   (cond-> opts
                   (contains? opts :model) (assoc :model model))
          body   {:body (json/write-value-as-string opts)}]
      (method url body)))

  (stream [_this method endpoint opts]
    (let [url    (str base-url endpoint)
          method (resolve-method method)
          opts   (cond-> opts
                   (nil? (:model opts)) (assoc :model model))
          opts   {:body (json/write-value-as-string opts)
                  :pool (http/connection-pool {:connection-options
                                               {:raw-stream? true}})}]
      (method url opts)))

  (request-stream [this method endpoint {:keys [stream?] :as opts}]
    (if stream?
        (stream this method endpoint opts)
        (request this method endpoint opts))))

(defn make-client
  ([base-url]
   (make-client base-url nil))
  ([base-url model]
   (->Client base-url model)))

(defn generate
  "
  Expects opts to contain :model, :messages, and :stream? keys.
  "
  [client opts]
  (request-stream client :post "/api/generate" opts))

(defn chat
  "
  Expects opts to contain :model, :messages, and :stream? keys.
  "
  [client opts]
  (request-stream client :post "/api/chat" opts))

(defn embeddings
  "
  Expects opts to have :model, :prompt, and :stream? keys.
  "
  [client opts]
  (request client :post "/api/embeddings" opts))

(defn pull
  ""
  [client opts]
  (request-stream client :post "/api/pull" opts))

(defn push
  [client opts]
  (request-stream client :post "/api/push" (merge {:insecure? false
                                                   :stream? false}
                                                  opts)))

(defn- cwd []
  (System/getProperty "user.dir"))

(defn- parse-modelfile
  ([modelfile] (parse-modelfile modelfile nil))
  ([modelfile base]
   (let [base (or base (cwd))])))

(defn create
  ([client model]
   (create client model {:stream? false
                         :path nil
                         :modelfile nil}))
  ([client model {:keys [path modelfile] :as opts}]
   (let [file (io/file path)
         parsed  (cond
                   (and (.exists file) (.isFile file))
                   (parse-modelfile modelfile (.getParent file))

                   (and (string? modelfile)
                        (not (str/blank? modelfile)))
                   (parse-modelfile modelfile)

                   :else
                   (throw (Exception. "Invalid path or modelfile")))]
     (request-stream client :post "/api/create" (assoc opts
                                                       :name model
                                                       :modelfile parsed)))))



(comment
  (import [java.security MessageDigest])
  (defn sha256 [string]
    (let [digest (.digest (MessageDigest/getInstance "SHA-256") (.getBytes string "UTF-8"))]
      (apply str (map (partial format "%02x") digest))))
  )

(defn- create-blob [path]
  ;; TODO
  (println "Creating blob from" path)
  (throw (Exception. "Not implemented")))

(defn delete [client model]
  (let [response (request client :delete "/api/delete" {:name model})]
    (if (= 200 (:status response))
      {:status :success}
      {:status :failure})))

(defn list-tags [client]
  (request client :get "/api/tags" {}))

(defn copy [client source destination]
  (let [response (request client :post "/api/copy" {:source source
                                           :destination destination})]
    (if (= 200 (:status response))
      {:status "success"}
      {:status "failure"})))

(defn show
  ([client]
   (show client {}))
  ([^Client client {:keys [model raw?]}]
   ;; TODO: handle case when the model is not found - 404 error message
   (if-let [actual-model (or model (.model client))]
     (cond-> (request client :post "/api/show" {:name actual-model})
       (not (true? raw?))
       (u/read-body))
     (throw (Exception. "Provide a model name either by passing it to the function or by setting it in the client.")))))
