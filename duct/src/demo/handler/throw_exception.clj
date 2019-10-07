(ns demo.handler.throw-exception
  (:require [integrant.core :as ig]))

(defmethod ig/init-key ::not-found [_ _]
  (fn [req]
    (throw (ex-info "Not Found" {:exception/type :not-found}))))

(defmethod ig/init-key ::server-error [_ _]
  (fn [req]
    (throw (ex-info "ERRR" {:exception/type :server-error}))))
