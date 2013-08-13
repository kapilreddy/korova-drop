(ns audio.core
  (:require [cljs.core.async :refer [chan sliding-buffer put! timeout]]
            [clojure.string :as string]
            [audio.sphere-plot :as sph]
            [audio.sphere-plot-3 :as sph3]
            [audio.spectrum-plot :as sp]
            [utils.helpers
             :refer [event-chan set-html by-id add-class remove-class]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [utils.macros :refer [go-loop]]))


(def active-viz (atom :sphere3))

(def viz-list [:sphere3 :sphere :spectrum])

(defn viz-run
  [keys & args]
  (let [viz-data {:spectrum {:setup sp/scene-setup
                             :sound-render sp/sound->display
                             :destroy sp/scene-destroy}
                  :sphere {:setup sph/scene-setup
                           :sound-render sph/sound->display
                           :destroy sph/scene-destroy}
                  :sphere3 {:setup sph3/scene-setup
                            :sound-render sph3/sound->display
                            :destroy sph3/scene-destroy}}]

    (apply (get-in viz-data keys) args)))

(def audio-context (if window/webkitAudioContext
                     (new window/webkitAudioContext)
                     window/AudioContext))
(def a-size 2048)

(defn local-file->chan
  "Return a channel 'c' for a given local file. Decoded mp3 data pushed to the channel 'c'"
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
  "All file drag and drop related handling this will return a files channels.
  'files-chan' will always recieve an array of files whenever files are
  dropped on 'drop-zon' div."
  []
  (let [drop-zone js/document
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
  "Play the given audioBuffer using global audio-context"
  [buffer]
  (let [source (.createBufferSource audio-context)]
    (set! (.-buffer source) buffer)
    (.connect source (.-destination audio-context))
    (.start source 0)
    source))


(defn sound-+>display
  "Connect an analyzer to audio source which will
   generate fft data for viz."
  [source-node]
  (let [analyzer (.createAnalyser audio-context)]
    (set! (.-fftSize analyzer) 1024)
    (set! (.-smoothingTimeConstant analyzer) 0.7)
    (.connect source-node analyzer)
    (.connect analyzer (.-destination audio-context))
    analyzer))

(defn animloop
  "Request animation loop for rendering everything."
  [ui-chan ts]
  (.requestAnimationFrame js/window (partial animloop ui-chan))
  (put! ui-chan ts))


(defn change-renderer
  "Switch a vizualization renderer. Every renderer must implement "
  [renderer]
  (let [old-renderer @active-viz]
    (reset! active-viz nil)
    (viz-run [old-renderer :destroy])
    (viz-run [renderer :setup])
    (reset! active-viz renderer)))


(let [viz-count (count viz-list)]
  (defn shift-renderer
    "Shift to next renderer to specified 'dir' (:right or :left)"
    [dir]
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
  "Key handler attached to js document. returns a 'key-chan'.
   keyCode are pushed to 'key-chan'"
  []
  (let [key-chan (chan)]
    (aset js/document
          "onkeydown"
          #(put! key-chan (aget % "keyCode")))
    key-chan))


(defn init-doc-handler
  []
  (let [onload-chan (chan)]
    (aset js/window "onload" (fn []
                               (put! onload-chan "ready")))
    onload-chan))


(defn -main
  []
  (let [files-chan (init-file-handling)
        audio-chan (chan)
        ui-chan (chan)
        analyzer (atom nil)
        progress-chan (chan)
        key-chan (init-key-handler)
        doc-ready-chan (init-doc-handler)]
    ;; Initiate animation loop
    (animloop ui-chan 0)

    ;; Initial setup for current active viz.
    (go-loop (<! doc-ready-chan)
             ;; This is a temporary fix for setup function in sphere3.
             (doseq [n (range 2)]
               (change-renderer @active-viz)))

    ;; Go block to handle drag and drop ui states. Also pass file data
    ;; to correct channel for later processing/rendering.
    (go-loop (let [files (<! files-chan)
                   file (aget files 0)]
               (add-class (by-id "drop_zone_wrapper") "loading")
               (let [audio (<! (local-file->chan file))]
                 (remove-class (by-id "drop_zone_wrapper") "loading")
                 (add-class (by-id "drop_zone_wrapper") "corner")
                 (remove-class (by-id "progress-bar-wrapper") "hidden")
                 (aset (by-id "progress") "style" "0%")
                 (put! audio-chan audio))))

    ;; Go block to handle playing and analyzing audio file that is
    ;; droppped to 'drop-zone'
    (go
     (loop [audio-source nil]
       (let [buff (<! audio-chan)
             source-node (play-sound-buff buff)]
         (put! progress-chan {:type :duration
                              :val (aget buff "duration")})
         (when audio-source
           (.noteOff audio-source 0))
         (reset! analyzer (sound-+>display source-node))
         (recur source-node))))

    ;; Go block to pass analyzed frequency data to current active viz renderer
    (go (loop [prev-data nil]
          (let [frame-time (<! ui-chan)]
            (put! progress-chan {:type :update
                                 :val frame-time})
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

    ;; Go block to handle key captured on js/document. Used to change
    ;; viz for now.
    (go-loop (let [key-code (<! key-chan)]
               (condp = key-code
                 37 (shift-renderer :left)
                 39 (shift-renderer :right)
                 "Other keys not supported")))

    ;; Go block to update progress bar.
    (go (loop [start-time 0
               duration 0]
          (let [{:keys [type val]} (<! progress-chan)

                curr-time (aget audio-context "currentTime")]
            (if (= type :duration)
              (do (aset (by-id "progress")
                        "style"
                        "width"
                        "0%")
                  (recur curr-time
                         val))
              (do (aset (by-id "progress")
                        "style"
                        "width"
                        (str (if (zero? end-time)
                               0
                               (* (/ (- curr-time start-time) duration) 100))
                             "%"))
                  (recur start-time duration))))))))

(-main)
