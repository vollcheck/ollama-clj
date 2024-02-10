(ns chat-stream
  (:require [ollama-clj.core :as o]))

(def client (o/->Client "http://localhost:3000"))

(def messages
  [{:role "user"
    :content "Why is the sky blue?"}])

(doseq [part (o/chat client {:model "mistral"
                             :messages messages
                             :stream? true})]
  (print (-> part :message :content)))
