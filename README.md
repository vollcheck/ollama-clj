# ollama-clj

Use [`ollama`][https://ollama.com] from the Clojure project.

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

For more usages reach out to [`examples/`][examples/] directory.

### Async client

TODO

### Create your own client

If you want to gain control over the way of executing `ollama` calls, you can implement your own client like so:

```clojure
TODO
```

## References

- Ollama: https://ollama.com
- Python library: https://github.com/ollama/ollama-python
- https://github.com/s-kostyaev/ellama/blob/main/ellama.el
- streaming server used for testing `ollama-clj`: https://github.com/vollcheck/streaming-server-clj

## TODO

- juxt/aero for reading the configuration (in practice URL only - do not overengineer it...)
- make name of the model a part of the client? it can persist through calls to ollama
