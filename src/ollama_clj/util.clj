(ns ollama-clj.util
  (:require [jsonista.core :as json]
            [manifold.deferred :as d]
            [clj-commons.byte-streams :as bs]))

(def kkom json/keyword-keys-object-mapper)

(defn default-process-part [^String part]
  (json/read-value part kkom))

(defn process
  ([command-fn]
   (process command-fn default-process-part))
  ([command-fn process-part-fn]
   (d/chain command-fn
            :body
            bs/to-line-seq
            #(map process-part-fn %))))

(defn read-body
  "Simply reads the body of a response."
  [response]
  (-> response
      deref
      :body
      (json/read-value kkom)))

(defn read-status
  "Simply reads the status of a response."
  [response]
  (-> response
      deref
      :status))
