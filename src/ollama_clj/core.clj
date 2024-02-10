(ns ollama-clj.core
  (:require [clojure.string :as str]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [manifold.stream :as stream]
            [jsonista.core :as json]
            [ollama-clj.schema :as s]
            [malli.core :as m]))

(defn- resolve-method [method]
  (ns-resolve 'aleph.http (symbol (name method))))

(defprotocol BaseClient
  (request        [this method endpoint opts])
  (stream         [this method endpoint opts])
  (request-stream [this method endpoint opts]))

(defrecord Client [^String base-url]
  BaseClient
  (request [this method endpoint {:keys [] :as opts}]
    (let [method (resolve-method method)]
      (-> (str base-url endpoint)
          (method {:headers {"Content-Type" "application/json"}
                   :body (json/write-value-as-string opts)})
          (d/chain #(println "Response:" @(d/deferred @%))))))

  (stream [this method endpoint {:keys [] :as opts}]
    (let [method (resolve-method method)]
      (-> (str base-url endpoint)
          (method {:headers {"Content-Type" "application/json"}
                   :body (json/write-value-as-string opts)
                   :stream? true})
          (d/chain (fn [{:keys [body] :as resp}]
                     (stream/consume
                      (fn [chunk]
                        (println "Received chunk:" chunk))
                      body))))))

  (request-stream [this method endpoint {:keys [stream?] :as opts}]
    (if stream?
      (stream this method endpoint opts)
      (request this method endpoint opts))))

(defn chat
  [client opts]
  #_{:pre [(m/validate s/ChatOptions opts)]}
  (request-stream client :post "/api/chat" opts))

(defn generate
  ;; ollama.generate(model='llama2', prompt='Why is the sky blue?')
  [client {:keys [model prompt system template context stream? raw? format images options keep-alive?]
           :or {model ""
                prompt ""
                system ""
                template ""
                context nil
                stream? false
                raw? false
                format ""
                images nil
                options nil
                keep-alive? nil}
           :as opts}]
  {:pre [(not (str/blank? model))]}
  (request-stream client :post "/api/generate" opts))

(defn parse-modelfile []
  (assert false "Not implemented")
  )

(defn create-blob []
  (assert false "Not implemented")
  )

(defn delete [client]
  (let [response (request client :delete "/api/delete" {})]
    (if (= 200 (:status response))
      {:status "success"}
      {:status "failure"})))

(defn lst [client]
  (request client :get "/api/tags" {}))

(defn show
  [client
   {:keys [model]
    :or {model ""}
    :as opts}]
  {:pre [(not (str/blank? model))]}
  (request client :post "/api/show" opts))

;; modelfile='''
;; FROM llama2
;; SYSTEM You are mario from super mario bros.
;; '''

;; ollama.create(model='example', modelfile=modelfile)
(defn create
  [client
   {:keys [model path modelfile stream?]
    :or {model ""
         modelfile ""}
    :as opts}]
  {:pre [(not (str/blank? model))]}
  ;; if (realpath := _as_path(path)) and realpath.exists():
  ;;     modelfile = self._parse_modelfile(realpath.read_text(), base=realpath.parent)
  ;;   elif modelfile:
  ;;     modelfile = self._parse_modelfile(modelfile)
  ;;   else:
  ;;     raise RequestError('must provide either path or modelfile')
  (request-stream client :post "/api/create" opts))

(defn copy
  [client
   {:keys [model source destination]
    :or {model ""
         source ""
         destination ""}
    :as opts}]
  {:pre [(not (str/blank? model))]}
  (let [response (request client :post "/api/copy" opts)]
    (if (= 200 (:status response))
      {:status "success"}
      {:status "failure"})))

(defn push
  [client
   {:keys [model insecure? stream?]
    :or {model ""
         insecure? false
         stream? false}
    :as opts}]
  {:pre [(not (str/blank? model))]}
  (request-stream client "/api/push" opts))

(defn pull
  [client
   {:keys [model insecure? stream?]
    :or {model ""
         insecure? false
         stream? false}
    :as opts}]
  {:pre [(not (str/blank? model))]}
  (request-stream client "/api/pull" opts))

(defn embeddings
  [client
   {:keys [model prompt options keep-alive]
    :or {model ""
         prompt ""
         options nil
         keep-alive nil}
    :as opts}]
  {:pre []}
  (request client "/api/embeddings" opts))
