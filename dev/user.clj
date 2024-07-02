(ns user
  (:require [jsonista.core :as json]

            [manifold.deferred :as d]
            [clj-commons.byte-streams :as bs]
            [manifold.stream :as s]

            [ollama-clj.core :as o]
            [ollama-clj.util :as u]))


(comment
  (def kkom json/keyword-keys-object-mapper)

  (def client (o/make-client "http://localhost:11434"
                             "stablelm-zephyr"))

  (def messages
    [{:role "user"
      :content "Why is the sky blue? Please keep the answer short."}])

  (def prompt "Why is the sky blue? Please keep the answer short.")

  (def model "stablelm-zephyr")

  (doseq [part (deref (u/process (o/chat client messages)))]
    (println part))

  (doseq [part (deref (u/process (o/generate client {:model model
                                                     :prompt prompt})))]
    (println part))

  ;; sync
  (->> (d/chain (o/chat client "stablelm-zephyr" messages)
                :body
                bs/to-line-seq
                #(s/map (fn [x] (-> x
                                    bs/to-string
                                    (json/read-value kkom)
                                    #_(get-in [:message :content])))
                        %)
                #(s/reduce conj [] %))
       deref
       (map #(get-in % [:message :content]))
       (reduce str ""))

  ;; streaming, async
  (->> (d/chain (o/chat client {:model model
                                :messages messages
                                :stream? true})
                :body
                #(s/map (fn [x] (-> x
                                    bs/to-string
                                    (json/read-value kkom)
                                    #_(get-in [:message :content])))
                        %)
                #(s/reduce (fn [acc x]
                             (conj acc x))
                           [] %)
                )
       deref)

  )


(comment
  "`LIST-TAGS` TESTS"
  ;; TODO: turn this into actual tests
  (let [client (o/make-client "http://localhost:11434")]
    (o/list-tags client))

     ;; => {:models
  ;;     [{:modified_at "2024-07-02T22:08:47.624196778+02:00",
  ;;       :name "stablelm-zephyr:latest",
  ;;       :digest
  ;;       "0a108dbd846e2b0ee264a71a28e50ac18e7f1601eeb2d677217602d32644bf24",
  ;;       :size 1608579394,
  ;;       :details
  ;;       {:format "gguf",
  ;;        :family "stablelm",
  ;;        :parent_model "",
  ;;        :parameter_size "3B",
  ;;        :quantization_level "Q4_0",
  ;;        :families ["stablelm"]},
  ;;       :model "stablelm-zephyr:latest"}
  ;;      {:modified_at "2024-07-02T22:02:00.944796898+02:00",
  ;;       :name "stablelm-zephyr-backup:latest",
  ;;       :digest
  ;;       "0a108dbd846e2b0ee264a71a28e50ac18e7f1601eeb2d677217602d32644bf24",
  ;;       :size 1608579394,
  ;;       :details
  ;;       {:format "gguf",
  ;;        :family "stablelm",
  ;;        :parent_model "",
  ;;        :parameter_size "3B",
  ;;        :quantization_level "Q4_0",
  ;;        :families ["stablelm"]},
  ;;       :model "stablelm-zephyr-backup:latest"}]}
  )

(comment
  "`DELETE` TESTS"
  ;; TODO: turn this into actual tests
  (let [client (o/make-client "http://localhost:11434")]
    ;; (assert (= 404)
    (o/delete client "mistral")

    ;; assert 200
    (o/delete client "stablelm-zephyr")
    )
  )

(comment
  "`COPY` TESTS"
  ;; TODO: turn this into actual tests
  (let [client (o/make-client "http://localhost:11434" "stablelm-zephyr")]
    ;; (assert (= 404)
    (o/copy client "mistral" "mistral-backup")

    ;; assert 200
    (o/copy client "stablelm-zephyr" "stablelm-zephyr-backup")
    )
  )

(comment
  "`SHOW` TESTS"
  ;; TODO: turn this into actual tests


  ;; NOTE: this should raise an error says the model is not found
  (o/show (o/make-client "http://localhost:11434" nil))

  (let [client (o/make-client "http://localhost:11434" "stablelm-zephyr")]
    (o/show client {:model "mistral"}))

  (let [client (o/make-client "http://localhost:11434" "stablelm-zephyr")]

    ;; NOTE: good, uses model defined in client
    (o/show client)

    ;; NOTE: good, but raw output
    (o/show client {:raw? false})

    ;; NOTE: good, same as above but with raw output (manifold deferred)
    (o/show client {:raw? true})

    ;; TODO: it raises 404 error
    (o/show client {:model "mistral"})
    )
  )
