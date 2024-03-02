(ns user
  (:require
   [clojure.string :as str]
   [jsonista.core :as json]
   [ollama-clj.core :as o]
   [manifold.deferred :as d]
   [clj-commons.byte-streams :as bs]
   [manifold.stream :as s]))

(def kkom json/keyword-keys-object-mapper)

(comment
  (def client (o/->Client "http://localhost:11434"))

  (def messages
    [{:role "user"
      :content "Why is the sky blue? Please keep the answer short."}])

  ;; sync
  (->> (d/chain (o/chat client #_"stablelm-zephyr" "mistral" messages)
                :body
                bs/to-line-seq
                #(s/map (fn [x] (-> x
                                    bs/to-string
                                    (json/read-value kkom)
                                    #_(get-in [:message :content])))
                        %)
                #(s/reduce conj [] %))
       deref
       (map #(get-in % [:message :content]))
       (reduce str ""))

  ;; streaming, async
  (->> (d/chain (o/chat client "stablelm-zephyr" messages {:stream? true})
                :body
                #(s/map (fn [x] (-> x
                                    bs/to-string
                                    (json/read-value kkom)
                                    #_(get-in [:message :content])))
                        %)
                #(s/reduce (fn [acc x]
                             (conj acc x))
                           [] %)
                )
       deref)


(comment
  "embeddings test"
  (def client (o/->Client "http://localhost:11434"))
  (-> (o/embeddings client "stablelm-zephyr" "Why is the sky blue?")
      deref
      (json/read-value kkom) ;; TODO test further JSON parsing
      type
      )
  )
