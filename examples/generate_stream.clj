(ns generate-stream
  (:require [ollama-clj.core :as o]))

(def client (o/->Client "http://localhost:3000"))

(doseq [part (o/generate client "mistral" "Why is the sky blue?" {:stream true})]
  (print part))
