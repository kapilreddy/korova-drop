(ns core
  (:use compojure.core
        stencil.loader)
  (:gen-class)
  (:require [compojure.handler :as handler]
            [stencil.core :as sc]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.route :as route]))

(defn audio-page
  []
  (sc/render-file "public/audio/index.html" {:repl-script "test"}))


(defroutes app-routes
  (route/resources "/")
  (GET "/" req (audio-page)))

(def app
  (handler/site app-routes))

(stencil.loader/set-cache (clojure.core.cache/ttl-cache-factory {} :ttl 0))


(defn run
  []
  (run-jetty app
             {:port 3000
              :join? false}))
