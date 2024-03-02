(ns ollama-clj.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [aleph.http :as http]
            [clj-commons.byte-streams :as bs]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [jsonista.core :as json]))

(defprotocol BaseClient
  (request        [this method endpoint opts])
  (stream         [this method endpoint opts])
  (request-stream [this method endpoint opts]))

(defn- resolve-method [method]
  (ns-resolve 'aleph.http (symbol (name method))))

(defrecord Client [^String base-url]
  BaseClient
  (request [_this method endpoint opts]
    (let [url    (str base-url endpoint)
          method (resolve-method method)
          body   (json/write-value-as-string opts)
          opts   (assoc opts :body body)]
      (d/chain (method url opts)
               :body
               bs/to-string)))

  (stream [_this method endpoint {:keys [] :as opts}]
    (let [url    (str base-url endpoint)
          method (resolve-method method)
          body   (json/write-value-as-string opts)
          cp     (http/connection-pool {:connection-options {:raw-stream? true}})
          om     (json/keyword-keys-object-mapper)
          opts   (assoc opts :body body :pool cp)]
      (-> (method url opts)
          (d/chain (fn [{:keys [body] :as _resp}]
                     (s/consume
                      (fn [chunk]
                        (print "_" (-> chunk
                                       bs/to-string
                                       (json/read-value om)
                                       :response)))
                      body))))))

  (request-stream [this method endpoint {:keys [stream?] :as opts}]
    (if stream?
        (stream this method endpoint opts)
        (request this method endpoint opts))))

(defn generate
  ([client model prompt]
   (generate client model prompt {:stream? false
                                  :format ""
                                  :context []}))
  ([client model prompt opts]
   (request-stream client :post "/api/generate" (assoc opts
                                                       :model model
                                                       :prompt prompt))))

(defn chat
  ([client model messages]
   (chat client model messages {:stream? false
                                :format ""
                                :options {}}))
  ([client model messages opts]
   (request-stream client :post "/api/chat" (assoc opts
                                                   :model model
                                                   :messages messages))))

(defn embeddings
  ([client model prompt]
   (embeddings client model prompt {:options {}}))
  ([client model prompt opts]
   (request client :post "/api/embeddings" (assoc opts
                                                  :model model
                                                  :prompt prompt))))

(defn pull
  ([client model]
   (pull client model {:insecure? false
                       :stream? false}))
  ([client model opts]
   (request-stream client :post "/api/pull" (assoc opts :model model))))

(defn push
  ([client model]
   (push client model {:insecure? false
                       :stream? false}))
  ([client model opts]
   (request-stream client :post "/api/push" (assoc opts :model model))))

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

(defn show [client model]
  (request client :post "/api/show" {:name model}))
