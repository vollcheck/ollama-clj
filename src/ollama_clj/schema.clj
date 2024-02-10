(ns ollama-clj.schema
  (:require [clojure.string :as str]
            [malli.core :as m]))

(defn non-blank-string? [s]
  (and (string? s)
       (not (str/blank? s))))

(def nb-string
  [:pred #(and (string? %)
              (not (str/blank? %)))])

(def Message
  [:map
   [:role [:enum "user" "system"]]
   [:content nb-string]])

(def ChatOptions
  [:map
   [:model nb-string]
   [:messages :vector Message]
   [:stream :boolean]
   [:format :string]
   [:options {:optional true} :any]
   [:keep-alive {:optional true} :boolean]])

(comment
  (require '[malli.generator :as mg])
  (mg/generate Message)
  )

(def Options
  [:map
   [:model nb-string]
   [:messages :vector Message]
   [:stream :boolean]
   [:format :string]
   [:options :any]
   [:keep-alive :boolean]])

(def Embeddings
  [:map
   [:model nb-string]
   [:prompt nb-string]
   [:?options :vector :string] ;; TODO
   [:?keep-alive :boolean]])

(comment
  (require '[malli.generator :as mg])
  (mg/generate Message)

  (mg/generate Options)

  (m/validate Message {:role "user" :content "Why is the sky blue?"})
  )
