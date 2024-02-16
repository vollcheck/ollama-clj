(ns create
  (:require [ollama.core :as o]))

(def client (o/Client. "http://localhost:3000"))

;; TODO:
(def model #_(first *command-line-args*))
(def path #_(second *command-line-args*))
(def modelfile #_TOOD)

(doseq [part (o/create client model {:modelfile modelfile
                                     :stream true})]
  (-> part
      (get "status")
      println))
