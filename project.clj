(defproject korova-drop "0.1.0-SNAPSHOT"
  :description "An audio vizulization project using HTML5"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [stencil "0.3.2"]
                 [ring/ring-jetty-adapter "1.1.7"]
                 [com.cemerick/piggieback "0.0.5"]
                 [org.clojure/clojurescript "0.0-1844"]
                 [core.async "0.1.0-SNAPSHOT"]]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :source-paths ["src/cljs" "src/clj"]
  :plugins [[lein-cljsbuild "0.3.2"]
            [lein-ring "0.8.6"]
            [com.cemerick/austin "0.1.0"]]
  :profiles
  {:browser-repl
   {:repl-options
    {:init (cemerick.piggieback/cljs-repl)}}}
  :ring {:handler core/app}
  :jvm-opts ["-Xmx768M"]
  :aliases
  { "browser" ["with-profile" "browser-repl" "repl"]}
  :cljsbuild {:builds [{:id "audio"
                        :notify-command ["growlnotify" "-m"]
                        :source-paths ["src/cljs/audio"
                                       "src/cljs/utils"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print true
                                   :output-to "resources/public/audio/audio.js"}}
                       {:id "graphics"
                        :source-paths ["src/cljs/graphics"
                                       "src/cljs/utils"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print true
                                   :libs ["resources/public/three"]
                                   :output-to "resources/public/graphics/graphics.js"}}]})
