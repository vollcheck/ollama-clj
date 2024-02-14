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

(def Chat
  [:map
   [:model nb-string]
   [:messages :vector Message]
   [:stream :boolean]
   [:format :string]
   [:options {:optional true} :any]
   [:keep-alive {:optional true} :boolean]])

(def Generate
  [:map
   [:model nb-string]
   [:prompt nb-string]
   [:system {:optional true} :string]
   [:template {:optional true} :string]
   [:context {:optional true} :string]
   [:stream {:optional true} :boolean]
   [:raw {:optional true} :boolean]
   [:format {:optional true} :string]
   [:images {:optional true} :any] ;; TODO
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

(def Copy
  [:map
   [:model nb-string]
   [:source :string]
   [:destination :string]])

(def Push
  [:map
   [:model nb-string]
   [:insecure? :boolean]
   [:stream? :boolean]])

(comment
  (require '[malli.generator :as mg])
  (mg/generate Message)

  (mg/generate Options)

  (m/validate Message {:role "user" :content "Why is the sky blue?"})
  )
