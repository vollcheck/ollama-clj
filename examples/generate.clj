(ns generate
  (:require [ollama-clj.core :as o]))

(def client (o/->Client "http://localhost:3000"))

(-> (o/generate client "mistral" "Why is the sky blue?")
    (get "response")
    println)
