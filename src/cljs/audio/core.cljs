(ns audio.core
  (:require [cljs.core.async :refer [chan sliding-buffer put! timeout]]
            [clojure.string :as string]
            [clojure.browser.repl :as repl]
            [utils.helpers
            :refer [event-chan set-html by-id]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [utils.macros :refer [go-loop]]))

(def can-pref {:width 300 :height 300})

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

(def color ["#fde50f" "#feca2f" "#febf03" "#fd4403" "#fd4403"])

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
                                       100)))
          perc-change-color (/ perc-change 20)]
      (assoc curr :color (if (= (:val curr) (:val prev) 0)
                           (first color)
                           (if (>= perc-change-color 5)
                             (last color)
                             (nth color perc-change-color)))
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
                      :color (first color)
                      :tip (inc (aget data n))
                      :index n}))
          data (remove #(= (:val %) NaN) data)
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
      (doseq [{:keys [val index color tip]} (filter (fn [{:keys [index]}]
                                                  (even? index))
                                                data)]
        (set! (.-fillStyle canvas-context) color)
        (set! (.-shadowBlur canvas-context)
              1)
        (set! (.-shadowColor canvas-context)
              "#feca2f")
        (.fillRect canvas-context
                   (+ index (* index 1))
                   (- height val)
                   1
                   val)
        (set! (.-fillStyle canvas-context) "white")
        (set! (.-shadowBlur canvas-context) "none")
        (.fillRect canvas-context
                   (+ index (* index 1))
                   (- height
                      tip
                      3)
                   1
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
        canvas-context (.getContext (by-id "canvas_graph") "2d")
        audio-source (chan)]
    (go
     (loop []
       (let [buff (<! audio-chan)
             source-node (play-sound-buff buff)]
         (reset! analyzer (sound-+>display source-node))
         (recur))))
    (go
     (loop [data []]
       (<! (timeout 50))
       (if @analyzer
         (recur (sound->display @analyzer data))
         (recur data))))
    (go-loop (let [files (<! files-chan)
                   file (aget files 0)
                   audio (<! (local-file->chan file))]
               (println audio)
               (put! audio-chan audio)))))

(-main)

(repl/connect "http://localhsot:9000/repl")
