(ns user
  (:require [ollama-clj.core :as o]))

(comment
  (def port 11434)

  (def client (o/->Client "http://localhost:11434"))

  (def messages
    [{:role "user"
      :content "Why is the sky blue?"}])

  (o/chat client {:model "llama2"
                  :messages messages})

  (-> (o/chat client {:model "llama2"
                      :messages messages})
      :message
      #_#_:content
      println
      )



  )
