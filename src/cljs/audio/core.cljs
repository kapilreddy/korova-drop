(ns audio.core
  (:require [cljs.core.async :refer [chan sliding-buffer put! timeout]]
            [clojure.string :as string]
            [clojure.browser.repl :as repl]
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

(def color (mapcat identity [(repeat 4 "#fdf403")
                             (repeat 4 "#fce303")
                             (repeat 4 "#fed224")
                             (repeat 4 "#f7c22f")
                             (repeat 4 "#f7b52f")
                             (repeat 4 "#fc8b0a")
                             (repeat 4 "#fd7d34")
                             (repeat 4 "#fb2916")
                             (repeat 4 "#fd1500")]))

(defn add-render-data
  [curr prev]
  (if prev
    (let [perc-change (if (and (zero? (:val curr))
                               (zero? (:val prev)))
                        0
                        (Math/floor (* (/ (Math/abs (- (:val curr)
                                                       (:val prev)))
                                          (max (:val curr)
                                               (:val prev)))
                                       (count color))))
          color-index (if (= (:val curr) (:val prev) 0)
                        0
                        (if (>= perc-change (count color))
                          (dec (count color))
                          perc-change))
          color-index (cond
                       (> color-index (:color-index prev)) color-index
                       (< color-index (:color-index prev)) (if (= (:color-index prev) 1)
                                                             0
                                                             (dec (:color-index prev))))]
      (assoc curr :color-index color-index
             :tip (if (> (:tip prev)
                         (:val curr))
                    (if (= (:tip prev) 1)
                      0
                      (- (:tip prev) 2))
                    (inc (:val curr)))))
    curr))


(defn sound->display
  [analyzer data-prev]
  (let [canvas (by-id "canvas_graph")
        canvas-context (.getContext (by-id "canvas_graph") "2d")
        width (.-width canvas)
        height (.-height canvas)
        data (new window/Uint8Array width)]
    (.getByteFrequencyData analyzer data)
    (let [data (for [n (range (.-length data))]
                 (do (aget data n)
                     {:val (aget data n)
                      :color-index 0
                      :tip (inc (aget data n))
                      :index n}))
          max-val (apply max (map :val data))
          val-multi (if (zero? max-val)
                      0
                      (/ height max-val))
          data (map (fn [d]
                      (assoc d :val (* (:val d) val-multi)))
                    data)
          data (if (seq data-prev)
                 (do
                   (map add-render-data
                        data
                        data-prev))
                 data)
          center {:x (/ width 2)
                  :y (/ height 2)}]
      (.clearRect canvas-context 0 0
                  width
                  height)
      (set! (.-fillStyle canvas-context) (.-color js/window))
      (doseq [{:keys [val index color-index tip]} (filter (fn [{:keys [index]}]
                                                  (even? index))
                                                data)]
        (set! (.-fillStyle canvas-context) (nth color color-index))
        (set! (.-shadowBlur canvas-context)
              3)
        (set! (.-shadowColor canvas-context)
              "#feca2f")
        (.fillRect canvas-context
                   (+ index (* index 2))
                   (- height val)
                   2
                   val)
        (set! (.-fillStyle canvas-context) "white")
        (set! (.-shadowBlur canvas-context) "none")
        (.fillRect canvas-context
                   (+ index (* index 2))
                   (- height
                      tip
                      3)
                   2
                   2))
      data)))


(defn sound-+>display
  [source-node]
  (let [analyzer (.createAnalyser audio-context)]
    (set! (.-fftSize analyzer) 1024)
    (set! (.-smoothingTimeConstant analyzer) 0.7)
    (.connect source-node analyzer)
    (.connect analyzer (.-destination audio-context))
    analyzer))


(defn fix-retina
  []
  (when-let [pixel-ratio (.-devicePixelRatio js/window)]
    (let [canvas (by-id "canvas_graph")
          width (.-width canvas)
          height (.-height canvas)
          context (.getContext canvas "2d")]
      (set! (.-height context) (* height pixel-ratio))
      (set! (.-width context) (* width pixel-ratio))
      (.scale context pixel-ratio pixel-ratio))))



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
              (recur (sound->display @analyzer data))
              (recur data)))))
    (go-loop (let [files (<! files-chan)
                   file (aget files 0)]
               (add-class (by-id "drop_zone_wrapper") "loading")
               (let [audio (<! (local-file->chan file))]
                 (remove-class (by-id "drop_zone_wrapper") "loading")
                 (add-class (by-id "drop_zone_wrapper") "corner")
                 (put! audio-chan audio))))))

(-main)


;; (repl/connect "http://cljs.helpshift.mobi/repl")
