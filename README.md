# ollama-clj

Use [`ollama`][https://ollama.com] from the Clojure project.

## Usage

```clojure
(require '[ollama-clj.core :as o])

(def client (o/->Client "http://localhost:11434"))

(def messages
  [{:role "user"
    :content "WHy is the sky blie?"}])

(-> (o/chat client {:model "mistral"
                    :messages messages})
    :message
    :content)
```

or with streaming option:

```clojure
(require '[ollama-clj.core :as o])

(def client (o/->Client "http://localhost:11434"))

(def messages
  [{:role "user"
    :content "WHy is the sky blie?"}])

(doseq [part (o/chat client {:model "mistral"
                             :messages messages
                             :stream? true})]  ;; <-----
  (print (-> part :message :content)))
```

For more usages reach out to [`examples/`][examples/] directory.

## References

- Ollama: https://ollama.com
- Python library: https://github.com/ollama/ollama-python
