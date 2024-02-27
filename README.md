# ollama-clj

## NOTE: Work in progress!

Use [`ollama`](https://ollama.com) within Clojure project.

## Usage

```clojure
(require '[ollama-clj.core :as o])

(def client (o/->Client "http://localhost:11434"))

(def messages
  [{:role "user"
    :content "Why is the sky blue?"}])

(-> (o/chat client "mistral" messages)
    :message
    :content)
```

or with streaming option:

```clojure
(require '[ollama-clj.core :as o])

(def client (o/->Client "http://localhost:11434"))

(def messages
  [{:role "user"
    :content "Why is the sky blue?"}])

(doseq [part (o/chat client "mistral" messages
                     {:stream true})]  ;; <----- note the streaming flag
  (print (-> part :message :content)))
```

For more usages reach out to `examples/` directory.

### Async client

TODO

### Implement your own client

If you want to gain control over the way of executing `ollama` calls, you can implement your own client like so:

```clojure
TODO
```

## References

- ollama: https://ollama.com
- Python library for ollama (big inspiration for this project): https://github.com/ollama/ollama-python
- aleph/manifold docs: https://github.com/clj-commons/manifold/blob/master/doc/stream.md
