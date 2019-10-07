(ns demo.middleware.exception-handler
  (:require [ring.util.response :as response]
            [integrant.core :as ig]))

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

(defmethod ig/init-key :demo.middleware/exception-handler [_ _]
  wrap-exception-handler)
