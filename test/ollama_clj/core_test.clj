(ns ollama-clj.core-test
  "External tests for the core namespace.

  Note that in order to make them pass, you need to have a runnig ollama server."
  (:require [clojure.test :refer :all]
            [ollama-clj.core :as o]))

(def url "http://localhost:11434")
(def model "stablelm-zephyr")

(def client (o/make-client url model))

(deftest list-running-test
  (let [client (o/make-client url model)
        response (o/list-running client)]
    (is (= (-> response :models count)
           1))

    (is (= (-> response :models first :model)
           model))))

(deftest embeddings-test
  (let [f (partial o/embeddings client)
        prompt "here is something about llms"
        with-model-provided    (f model prompt)
        with-model-from-client (f prompt)
        with-unavailable-model (f "mistral" prompt)]
    (is (-> with-model-provided
            :embedding
            count
            (= 2560)))

    (is (-> with-model-from-client
            :embedding
            count
            (= 2560)))

    (is (-> with-unavailable-model
            :error
            (= "model 'mistral' not found, try pulling it first")))))

(deftest pull-test
  (throw (Exception. "not implemented yet")))

(deftest list-tags-test
  (is (-> (o/list-tags client)
          :models
          first
          :model
          (= (str model ":latest")))))

(deftest delete-test
  (let [copied-model (str model "-backup")]
    (o/copy client model copied-model)

    (is (= (o/delete client "mistral")
           404))

    (is (= (o/delete client copied-model)
           200))))

(deftest copy-test
  (is (= (o/copy client "mistral" "mistral-backup")
         404))

  (is (= (o/copy client model (str model "-backup"))
         200)))


(deftest show-test
  (let [client (o/make-client "http://localhost:11434" "stablelm-zephyr")]
    (is (-> (o/show client "mistral")
            :error
            (= "model 'mistral' not found")))

    (is (o/show client))))
