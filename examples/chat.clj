(ns chat
  (:require [ollama-clj.core :as o]))

(def client (o/Client. "http://localhost:3000"))

(def messages
  [{:role "user"
    :content "Why is the sky blue?"}])

(-> (o/chat client "mistral" messages})
    :message
    :content
    println)
