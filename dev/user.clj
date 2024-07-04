(ns user
  (:require [jsonista.core :as json]
            [manifold.deferred :as d]
            [clj-commons.byte-streams :as bs]
            [manifold.stream :as s]
            [ollama-clj.core :as o]
            [ollama-clj.util :as u]))


(comment
  (def kkom json/keyword-keys-object-mapper)

  (def client (o/make-client "http://localhost:11434"
                             "stablelm-zephyr"))

  (def messages
    [{:role "user"
      :content "Why is the sky blue? Please keep the answer short."}])

  (def prompt "Why is the sky blue? Please keep the answer short.")

  (def model "stablelm-zephyr")

  (doseq [part (deref (u/process (o/chat client messages)))]
    (println part))

  (doseq [part (deref (u/process (o/generate client {:model model
                                                     :prompt prompt})))]
    (println part))

  ;; sync
  (->> (d/chain (o/chat client "stablelm-zephyr" messages)
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
  (->> (d/chain (o/chat client {:model model
                                :messages messages
                                :stream? true})
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

  )

(comment
  "`PULL` TESTS"
  ;; TODO: turn this into actual tests
  (let [client (o/make-client "http://localhost:11434")]
    (def r (o/pull client {:name "stablelm-zephyr"
                           :stream? true})))

  (type r)
  (-> r deref)
  )
