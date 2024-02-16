(ns multimodal
  (:require [aleph.http :as http]
            [clj-commons.byte-streams :as bs]
            [jsonista.core :as json]
            #_[ollama.core :as o]))

(def kw-om json/keyword-keys-object-mapper)

(defn- a-request
  ([url]
   (a-request url false))
  ([url image?]
   (let [response @(http/get url)]
     (if (= 200 (:status response))
       (if image?
         (-> response
             :body)
         (-> response
             :body
             bs/to-string
             (json/read-value kw-om)))
       (throw (ex-info (str "Failed to fetch " url)
                       {:response response}))))))

(def latest
  (a-request "https://xkcd.com/info.0.json"))

(def rand-num
  (rand-int (:num latest)))

(def comic
  (a-request (format "https://xkcd.com/%d/info.0.json" rand-num)))

(println (format "xkcd #%s: %s" (:num comic) (:alt comic)))
(println (format "link to comic: https://xkcd.com/%s" rand-num))
(println "---")

(def raw
  (a-request (:img comic) true) ;; returns java.io.ByteArrayInputStream

(def client (o/Client. "http://localhost:3000"))

(doseq [part (o/generate client "llava" "explain this comic:"
                       {:images (-> raw #_bs/to-string)
                        :stream true})]
  (prn (get part "response")))
