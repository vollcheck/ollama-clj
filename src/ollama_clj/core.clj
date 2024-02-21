(ns ollama-clj.core
  ;; (:exclude [list]) ;; for list
  (:require [clojure.string :as str]
            [org.httpkit.client :as http]
            [jsonista.core :as json]
            [ollama-clj.schema :as s]
            [malli.core :as m]))

(def ollama-clj-version "0.1.0")

(def user-agent-string
  (format "ollama-clj/%s (%s system with Java %s) with Clojure version %s"
          ollama-clj-version
          (System/getProperty "os.name")
          (System/getProperty "java.version")
          (clojure-version)))

(def base-config
  {:follow-redirects? true
   :timeout nil
   :headers {"Content-Type" "application/json"
             "Accept"       "application/json"
             "User-Agent"   user-agent-string}})

(defprotocol BaseClient
  (request        [this method endpoint opts])
  (stream         [this method endpoint opts])
  (request-stream [this method endpoint opts]))

(defn- resolve-method [method]
  (ns-resolve 'org.httpkit.client (symbol (name method))))

;; https://github.com/clj-commons/aleph/blob/master/src/aleph/http.clj#L469
(defrecord Client [^String base-url]
  BaseClient
  (request [_this method endpoint opts]
    (let [url (str base-url endpoint)
          method (resolve-method method)
          body (json/write-value-as-string opts)
          {:keys [status] :as response} @(method url (assoc opts :body body))]
      (if (>= status 200)
        response
        (throw (Exception. (str "Request failed with status " status))))))

  ;; I've heard that streaming is not supported with http-kit
  ;; though what about websockets?
  ;; or {:as stream} option?
  ;; Get the body as a byte stream
  ;; (hk-client/get "http://site.com/favicon.ico" {:as :stream}
  ;;   (fn [{:keys [status headers body error opts]}]
  ;;     ;; body is a `java.io.InputStream`
  ;;     ))
  (stream [_this method endpoint {:keys [] :as opts}]
    (throw (UnsupportedOperationException. "Streaming not supported with http-kit")))

  (request-stream [this method endpoint {:keys [stream?] :as opts}]
    (if stream?
      (throw (UnsupportedOperationException. "Streaming not supported with http-kit"))
      (request this method endpoint opts))))

(comment
  ;; What do you think about instatiating base client for the user?
  (def base-client (->Client "http://localhost:11434"))
  )

(defn chat
  ([client model messages]
   (chat client model messages {}))
  ([client model messages opts]
   (request-stream client :post "/api/chat" (assoc opts
                                                   :model model
                                                   :messages messages))))

(defn generate
  ([client model prompt]
   (generate client model prompt {:stream? false
                                  ;; raw is not supported
                                  #_#_:raw? false}))
  ([client model prompt opts]
   (request-stream client :post "/api/generate" (assoc opts
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

(defn list-tags [client]
  (request client :get "/api/tags" {}))

(defn copy
  [client source destination]
  (let [{:keys [status] :as _response} (request client :post "/api/copy" {:source source
                                                                          :destination destination})]
    (if (= 200 status)
      {:status "success"}
      {:status "failure"})))

(defn show
  [client model]
  (request client :post "/api/show" {:name model}))
