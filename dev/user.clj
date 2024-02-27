(ns user
  (:require
   [clojure.string :as str]
   [jsonista.core :as json]
   [ollama-clj.core :as o]
   [ollama-clj.schema :as s]))

(comment
  (def port 11434)

  (def client (o/->Client "http://localhost:11434"))

  (def messages
    [{:role "user"
      :content "Why is the sky blue?"}])

  (def kkom json/keyword-keys-object-mapper)

  (-> (o/chat client "stablelm-zephyr" messages {:stream? false})
      deref
      (json/read-value kkom)
      (spit "chat.edn")
      )
  )

(comment
  (require '[clojure.string :as str]
           '[jsonista.core :as json])

  (def client (o/->Client "http://localhost:11434"))

  (def response
    (o/generate client "stablelm-zephyr" "Why is the sky blue?"))

  ;; Response is chunked into separate lines, we need to collect them
  ;; {:content-type "application/x-ndjson",
  ;;  :transfer-encoding "chunked"}
  (def body-lines (-> response :body (str/split-lines)))

  (def kkom json/keyword-keys-object-mapper)

  (->> body-lines
       (map #(-> % (json/read-value kkom) :response))
       #_(apply str)
       )

  )

(comment
  ;; testing the possibiltites of http-kit {:as :stream} option with `/api/generate` endpoitn
  (require '[jsonista.core :as json]
           ;; '[org.httpkit.client :as http]
           '[clojure.java.io :as io]
           '[clojure.string :as str]
           '[aleph.http :as http]
           '[manifold.stream :as s]
           '[manifold.deferred :as d]
           '[clj-commons.byte-streams :as bs])

  (def kkom json/keyword-keys-object-mapper)

  (def body
    (-> {:model "stablelm-zephyr"
         :prompt "Why is the sky blue?"}
        (json/write-value-as-string)))

  (def raw-stream-connection-pool (http/connection-pool {:connection-options {:raw-stream? true}}))
  ;; apparently, this is the right way to do the streaming, what form should I return the response in for people
  ;; to be able to use it in the same streaming manner?
  (defn using-aleph-manifold-streaming []
    (-> (http/post "http://localhost:11434/api/generate"
                   {:body body
                    :pool raw-stream-connection-pool})
        (d/chain (fn [{:keys [body] :as _resp}]
                   (s/consume
                    (fn [chunk]
                      (print "_" (-> chunk bs/to-string (json/read-value kkom) :response)))
                    body)))))

  (using-aleph-manifold-streaming)


  ;; using input coercion `{:as :stream}`
  (defn using-coercion []
    (with-open [rdr (-> (http/post "http://localhost:11434/api/generate"
                                   {:body body
                                    :as :stream}) ;; => org.httpkit.BytesInputStream
                        deref
                        :body
                        (io/reader #_#_:encoding "UTF-8"))]
      (doseq [line (line-seq rdr)]
        (print (-> line (json/read-value kkom) :response)))))

  (using-coercion)


  ;; using callback, async?
  (defn using-callback []
    (http/post "http://localhost:11434/api/generate"
               {:body body
                #_#_:as :stream}
               (fn [{:keys [error body] :as _resp}]
                 (if error
                   (println "Error: " error)
                   (doseq [chunk body]
                     (print "chunk")
                     (print (String. chunk)))))))

  (using-callback)
  )
