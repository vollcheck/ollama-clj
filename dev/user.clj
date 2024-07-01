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

  (doseq [part (deref (u/process (o/generate client model prompt)))]
    (println part))


  ;;;;;; SHOW TESTS
  ;; NOTE: this should raise an error says the model is not found
  (o/show (o/make-client "http://localhost:11434" nil))

  ;; NOTE: good, uses model defined in client
  (o/show (o/make-client "http://localhost:11434" "stablelm-zephyr"))

  ;; NOTE: good, but raw output (manifold deferred)
  (o/show (o/make-client "http://localhost:11434" "stablelm-zephyr")
          {:raw? true})

  ;; NOTE: good, same as above but with raw output
  (o/show client {:raw? true})

  ;; TODO: it raises 404 error
  (o/show client {:model "mistral"})


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

  )

(comment
  "embeddings test"
  (def client (o/->Client "http://localhost:11434"))
  (-> (o/embeddings client "stablelm-zephyr" "Why is the sky blue?")
      deref
      (json/read-value kkom) ;; TODO test further JSON parsing
      type
      )
  )
