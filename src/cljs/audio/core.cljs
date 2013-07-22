(ns audio.core
  (:require [cljs.core.async :refer [chan sliding-buffer put! timeout]]
            [clojure.string :as string]
            [clojure.browser.repl :as repl]
            [audio.spectrum-plot :as sp]
            [utils.helpers
            :refer [event-chan set-html by-id add-class remove-class]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [utils.macros :refer [go-loop]]))


(def audio-context (new window/webkitAudioContext))
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

(set! (.-color js/window) "#90fe2e")




(defn sound-+>display
  [source-node]
  (let [analyzer (.createAnalyser audio-context)]
    (set! (.-fftSize analyzer) 1024)
    (set! (.-smoothingTimeConstant analyzer) 0.7)
    (.connect source-node analyzer)
    (.connect analyzer (.-destination audio-context))
    analyzer))


(defn -main
  []
  (let [files-chan (init-file-handling)
        audio-chan (chan)
        ui-chan (chan)
        analyzer (atom nil)
        canvas (by-id "canvas_graph")
        canvas-context (.getContext canvas "2d")
        window-resize-chan (chan)
        init-canvas (fn []
                      (set! (.-height canvas) (.-innerHeight js/window))
                      (set! (.-width canvas) (.-innerWidth js/window)))]
    (.addEventListener js/window init-canvas)
    (init-canvas)
    (go
     (loop [audio-source nil]
       (let [buff (<! audio-chan)
             source-node (play-sound-buff buff)]
         (when audio-source
           (.noteOff audio-source 0))
         (reset! analyzer (sound-+>display source-node))
         (recur source-node))))
    (go-loop
     (<! (timeout 50))
     (.requestAnimationFrame js/window #(put! ui-chan %)))
    (go (loop [data []]
          (let [frame-time (<! ui-chan)]
            (if @analyzer
              (do
                (let [arr (new window/Uint8Array (.-width canvas))]
                  (.getByteFrequencyData @analyzer arr)
                  (recur (sp/sound->display arr
                                            data))))
              (recur data)))))
    (go-loop (let [files (<! files-chan)
                   file (aget files 0)]
               (add-class (by-id "drop_zone_wrapper") "loading")
               (let [audio (<! (local-file->chan file))]
                 (remove-class (by-id "drop_zone_wrapper") "loading")
                 (add-class (by-id "drop_zone_wrapper") "corner")
                 (put! audio-chan audio))))))

(-main)

(repl/connect "http://cljs.helpshift.mobi:9000/repl")
