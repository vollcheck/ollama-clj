(ns ollama-clj.schema
  (:require [clojure.string :as str]
            [malli.core :as m]))

(def nb-string
  [:pred #(and (string? %)
               (not (str/blank? %)))])

(def Generate
  [:map
   [:model :string]
   [:prompt :string]
   [:system {:optional true} :string]
   [:template {:optional true} :string]
   [:context {:optional true} :string]
   [:stream {:optional true} :boolean]
   [:raw {:optional true} :boolean]
   [:format {:optional true} :string]
   [:images {:optional true} :any] ;; TODO
   [:options {:optional true} :any]
   [:keep-alive {:optional true} :boolean]])

(def Message
  [:map
   [:role [:enum "user" "system"]]
   [:content nb-string]
   [:images {:optional true} :any]])

(def Messages
  [:vector Message])

(def Chat
  [:map
   [:model nb-string]
   [:messages :vector Message]
   [:stream :boolean]
   [:format :string] ;; enum json or ""
   [:options {:optional true} :any]
   [:keep-alive {:optional true} :boolean]])

(def Embeddings
  [:map
   [:model nb-string]
   [:prompt nb-string]
   [:options {:optional true} :any]
   [:keep-alive {:optional true} :any]])

(def Pull
  [:map
   [:model nb-string]
   [:insecure :boolean] ;; false
   [:stream :boolean]]) ;; false

(def Push
  [:map
   [:model nb-string]
   [:insecure? :boolean] ;; false
   [:stream? :boolean]]) ;; false

(def Create
  [:map
   [:model nb-string]
   [:path {:optional true} :string]
   [:modelfile {:optional true} :string]
   [:stream? :boolean]]) ;; false

(def Delete
  [:map
   [:model nb-string]])

(def ListTags
  [:map
   [:model nb-string]])

(def Copy
  [:map
   [:model nb-string]
   [:source :string]
   [:destination :string]])

(def Show
  [:map
   [:model nb-string]])

(comment
  (require '[malli.generator :as mg])
  (mg/generate Message)
  (m/validate Message {:role "user" :content "Why is the sky blue?"})
  )
