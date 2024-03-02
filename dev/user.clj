(ns user
  (:require
   [clojure.string :as str]
   [jsonista.core :as json]
   [ollama-clj.core :as o]
   [manifold.deferred :as d]
   [clj-commons.byte-streams :as bs]))

(def kkom json/keyword-keys-object-mapper)

(comment
  (def client (o/->Client "http://localhost:11434"))

  (def messages
    [{:role "user"
      :content "Why is the sky blue?"}])

  ;; (def response
  ;;   (o/chat client "stablelm-zephyr" messages {:stream? false}))

  ;; simple chain
  (d/chain (o/chat client "stablelm-zephyr" messages {:stream? false})
           :body
           bs/to-string
           type)

  ;; simple chain with println at the end
  (d/chain (o/chat client "stablelm-zephyr" messages {:stream? false})
           :body
           bs/to-string
           println)

  ;; simple "sync" print
  (->> @(o/chat client "stablelm-zephyr" messages {:stream? false})
       :body
       bs/to-string
       (spit "chat-response.json")
       ;; (json/read-value kkom)
       ;; println
       )

  ;; using the `chat` function with a single message + save to a file
  (->> @(o/chat client "stablelm-zephyr" "why is the sky blue?" {:stream? false})
       :body
       bs/to-string
       ;; (spit "chat-response.json")
       ;; (json/read-value kkom)
       ;; println
       )

  (->> @(o/generate client "stablelm-zephyr" "why is the sky blue?" {:stream? false})
       :body
       ;;bs/to-string
       ;; (spit "chat-response.json")
       ;; (json/read-value kkom)
       ;; println
       )

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
