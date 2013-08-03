(ns audio.core
  (:require [cljs.core.async :refer [chan sliding-buffer put! timeout]]
            [clojure.string :as string]
            [clojure.browser.repl :as repl]
            [audio.sphere-plot :as sph]
            [audio.sphere-plot-2 :as sph2]
            [audio.sphere-plot-3 :as sph3]
            [audio.spectrum-plot :as sp]
            [utils.helpers
             :refer [event-chan set-html by-id add-class remove-class]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [utils.macros :refer [go-loop]]))


(repl/connect "http://localhost:9000/repl")

(def viz-list [:spectrum :sphere :sphere2 :sphere3])

(defn viz-run
  [keys & args]
  (let [viz-data {:spectrum {:setup sp/scene-setup
                             :sound-render sp/sound->display
                             :destroy sp/scene-destroy}
                  :sphere {:setup sph/scene-setup
                           :sound-render sph/sound->display
                           :destroy sph/scene-destroy}
                  :sphere2 {:setup sph2/scene-setup
                            :sound-render sph2/sound->display
                            :destroy sph2/scene-destroy}
                  :sphere3 {:setup sph3/scene-setup
                            :sound-render sph3/sound->display
                            :destroy sph3/scene-destroy}}]

    (apply (get-in viz-data keys) args)))

(def audio-context (if window/webkitAudioContext
                     (new window/webkitAudioContext)
                     window/AudioContext))
(def a-size 2048)

(defn local-file->chan
  [file]
  (let [reader (new window/FileReader)
        resp-c (chan)
        c (chan)]
    (set! (.-onload reader) (fn []
                              (put! resp-c (.-result reader))))
    (.readAsArrayBuffer reader file)
    (go-loop (let [[resp] (alts! [resp-c])]
               (.decodeAudioData audio-context
                                 resp
                                 #(put! c %))))
    c))


(defn init-file-handling
  []
  (let [drop-zone (by-id "drop_zone")
        files-chan (chan)]
    (.addEventListener drop-zone
                       "dragover"
                       (fn [e]
                         (.stopPropagation e)
                         (.preventDefault e)
                         (set! (.-dropEffect (.-dataTransfer e))
                               "copy"))
                       false)
    (.addEventListener drop-zone
                       "drop"
                       (fn [e]
                         (.stopPropagation e)
                         (.preventDefault e)
                         (put! files-chan
                               (.-files (.-dataTransfer e))))
                       false)
    files-chan))


(defn play-sound-buff
  [buffer]
  (let [source (.createBufferSource audio-context)]
    (set! (.-buffer source) buffer)
    (.connect source (.-destination audio-context))
    (.start source 0)
    source))


(defn sound-+>display
  [source-node]
  (let [analyzer (.createAnalyser audio-context)]
    (set! (.-fftSize analyzer) 1024)
    (set! (.-smoothingTimeConstant analyzer) 0.7)
    (.connect source-node analyzer)
    (.connect analyzer (.-destination audio-context))
    analyzer))

(defn animloop [ui-chan ts]
  (.requestAnimationFrame js/window (partial animloop ui-chan))
  (put! ui-chan ts))

(def active-viz (atom :sphere))

(defn change-renderer
  [renderer]
  (let [old-renderer @active-viz]
    (reset! active-viz nil)
    (viz-run [old-renderer :destroy])
    (viz-run [renderer :setup])
    (reset! active-viz renderer)))

(let [viz-count (count viz-list)]
  (defn shift-renderer
    [dir]
    (println dir)
    (let [curr-index (some (fn [[i v]]
                             (when (= v @active-viz)
                               i))
                           (map-indexed (fn [i v]
                                          [i v])
                                        viz-list))
          next-index (condp = dir
                       :left (dec curr-index)
                       :right (inc curr-index))
          next-index (mod (+ next-index viz-count) viz-count)
          next-renderer (nth viz-list next-index)]
      (change-renderer next-renderer))))

(defn init-key-handler
  []
  (let [key-chan (chan)]
    (aset js/document
          "onkeydown"
          #(put! key-chan (aget % "keyCode")))
    key-chan))


(defn -main
  []
  (let [files-chan (init-file-handling)
        audio-chan (chan)
        ui-chan (chan)
        analyzer (atom nil)
        key-chan (init-key-handler)]
    (animloop ui-chan 0)
    (viz-run [@active-viz :setup])
    (go
     (loop [audio-source nil]
       (let [buff (<! audio-chan)
             source-node (play-sound-buff buff)]
         (when audio-source
           (.noteOff audio-source 0))
         (reset! analyzer (sound-+>display source-node))
         (recur source-node))))
    (go (loop [prev-data nil]
          (let [frame-time (<! ui-chan)]
            (if @analyzer
              (do
                (let [arr (new window/Uint8Array (.-innerWidth js/window))]
                  (.getByteFrequencyData @analyzer arr)
                  (let [audio-data (for [i (range (.-length arr))]
                                     (aget arr i))]
                    (if-let [renderer @active-viz]
                      (recur (viz-run [renderer :sound-render] audio-data prev-data))
                      (recur prev-data)))))
              (recur prev-data)))))
    (go-loop (let [files (<! files-chan)
                   file (aget files 0)]
               (add-class (by-id "drop_zone_wrapper") "loading")
               (let [audio (<! (local-file->chan file))]
                 (remove-class (by-id "drop_zone_wrapper") "loading")
                 (add-class (by-id "drop_zone_wrapper") "corner")
                 (put! audio-chan audio))))
    (go-loop (let [key-code (<! key-chan)]
               (condp = key-code
                 37 (shift-renderer :left)
                 39 (shift-renderer :right)
                 "Lol")))))

(-main)
