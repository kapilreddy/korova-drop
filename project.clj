(defproject korova-drop "0.1.0-SNAPSHOT"
  :description "An audio vizulization project using HTML5"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [org.clojure/clojurescript "0.0-1450"]
                 [core.async "0.1.0-SNAPSHOT"]]
  :source-paths ["src/cljs" "src/clj"]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :ring {:handler core/app}
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :cljsbuild {:builds [{:id "audio"
                        :source-paths ["src/cljs/audio"
                                       "src/cljs/utils"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print true
                                   :static-fns true
                                   :output-to "resources/public/audio/audio.js"}}
                       {:id "graphics"
                        :source-paths ["src/cljs/graphics"
                                       "src/cljs/utils"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print true
                                   :static-fns true
                                   :output-to "resources/public/graphics/graphics.js"}}]})
