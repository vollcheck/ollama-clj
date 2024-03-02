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

                                               ;; note the streaming flag!
(doseq [part (o/chat client "mistral" messages {:stream true})]
  (print (-> part :message :content)))
```

For more usages reach out to `examples/` directory.

### Internals

### Implement your own client

If you want to gain control over the way of executing `ollama` calls, you can implement your own client simply by using record and protocol like so:

```clojure
(defrecord MyClient [url]
  o/BaseClient
  (request [_this method endpoint opts]
    :perform-request)

  (stream [_this method endpoint opts]
    :perform-streaming)

  (request-stream [this method endpoint {:keys [stream?] :as opts}]
    (if stream?
      (.stream this method endpoint opts)
      (.request this method endpoint opts))))
```

## References

- https://ollama.com
- https://github.com/ollama/ollama-python - Python library for ollama (big inspiration for this project)
- https://github.com/clj-commons/manifold/blob/master/doc/stream.md - aleph/manifold streaming docs
