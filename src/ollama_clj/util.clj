(ns ollama-clj.util
  (:require [jsonista.core :as json]
            [ollama-clj.core :as o]
            [manifold.deferred :as d]
            [clj-commons.byte-streams :as bs]
            [manifold.stream :as s]))

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

(defn read-body [response]
  (-> response
      deref
      :body
      bs/to-string
      (json/read-value kkom)))
