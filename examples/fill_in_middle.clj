(ns fill-in-middle)

(def prefix
  "def remove_non_ascii(s: str) -> str:
    ")

(def suffix
  "
    return result
  ")

(def client (o/Client. "http://localhost:3000"))

(def prompt
  (format "<PRE> %s <SUF>%s <MID>" prefix suffix))

(def options
  {"num_predict" 128
   "temperature" 0
   "top_p" 0.9
   "stop" ["<EOT>"]})

(-> (o/generate client
                "codellama:7b-code"
                prompt
                options)
    (get "response")
    (println))
