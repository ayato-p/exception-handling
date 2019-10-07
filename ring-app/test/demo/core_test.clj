(ns demo.core-test
  (:require [clojure.test :as t]
            [demo.core :as sut]
            [ring.mock.request :as mock]
            [clojure.string :as str]))

(t/deftest throw-exception-handler-test
  (t/testing "throw-exception-handlerが例外を投げる"
    (let [req (mock/request :get "/err")]
      (t/is
       (thrown-with-msg? clojure.lang.ExceptionInfo #"err"
                         (sut/throw-exception-handler req)))

      (let [data (try
                   (sut/throw-exception-handler req)
                   (catch clojure.lang.ExceptionInfo e
                     (ex-data e)))]
        (t/is (= {:exception/type :server-error}
                 data))))))

(t/deftest wrap-exception-handler-test
  (t/testing
      "例外を投げないハンドラーが実行されたら何もせずに元の結果を返すこと"
    (let [req (mock/request :get "/err")
          handler (constantly {:status 200
                               :body "Hello, world"})
          app (sut/wrap-exception-handler handler)]
      (t/is (= {:status 200
                :body "Hello, world"}
               (app req)))))
  (t/testing
      "例外を投げるハンドラーが実行されたら適切なエラーコードを返すこと"

    (t/testing "IllegalArgumentExceptionのとき500を返す"
      (let [req (mock/request :get "/err")
            handler (fn [_] (throw (IllegalArgumentException. "err")))
            app (sut/wrap-exception-handler handler)]
        (t/is (= {:status 500
                  :body "Internal Server Error!!"
                  :headers {}}
                 (app req)))))

    (t/testing "対応できていない例外は500を返す"
      (let [req (mock/request :get "/err")
            handler (fn [_] (throw (NullPointerException. "err")))
            app (sut/wrap-exception-handler handler)]
        (t/is (= {:status 500
                  :body "Internal Server Error!!"
                  :headers {}}
                 (app req)))
        (t/is (str/starts-with?
               (with-out-str (app req))
               "Unhandled Exception: java.lang.NullPointerException"))))

    (t/testing "ExceptionInfo"
      (t/testing ":exception/typeが:server-errorのとき500を返す"
        (let [req (mock/request :get "/err")
              handler (fn [_] (throw (ex-info "err" {:exception/type :server-error})))
              app (sut/wrap-exception-handler handler)]
          (t/is (= {:status 500
                    :body "Internal Server Error!!"
                    :headers {}}
                   (app req)))))

      (t/testing ":exception/typeが:not-foundのとき400を返す"
        (let [req (mock/request :get "/err")
              handler (fn [_] (throw (ex-info "err" {:exception/type :not-found})))
              app (sut/wrap-exception-handler handler)]
          (t/is (= {:status 404
                    :body "Not Found!!"
                    :headers {}}
                   (app req))))))))
