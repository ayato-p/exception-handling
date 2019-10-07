(ns demo.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]))

(defonce server (atom nil))

(defn- do-something []
  (throw (ex-info "err" {:exception/type :server-error})))

(defn throw-exception-handler [req]
  (let [res (do-something)]
    (response/response res)))

(defn- internal-server-error-response []
  (-> (response/response "Internal Server Error!!")
      (response/status 500)))

(defn- not-found-response []
  (-> (response/response "Not Found!!")
      (response/status 404)))

(defprotocol ExceptionToResponse
  (->response [e]))

(extend-protocol ExceptionToResponse
  Exception
  (->response [e]
    (println "Unhandled Exception:" (type e))
    (clojure.stacktrace/print-stack-trace e)
    (internal-server-error-response))

  IllegalArgumentException
  (->response [e]
    (internal-server-error-response))

  clojure.lang.ExceptionInfo
  (->response [e]
    (let [{t :exception/type} (ex-data e)]
      (case t
        :server-error
        (internal-server-error-response)
        :not-found
        (not-found-response)))))

(defn wrap-exception-handler [handler]
  (fn exception-handler [req]
    (try
      (handler req)
      (catch Exception e
        (->response e)))))

(def app
  (-> throw-exception-handler
      wrap-exception-handler))

(defn start-server []
  (when-not @server
    (let [options {:port 3000 :join? false}]
      (->> (jetty/run-jetty app options)
           (reset! server)))))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn -main [& args]
  (start-server))
