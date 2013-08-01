(ns audio.spectrum-plot
  (:require [utils.helpers :refer [by-id]]))

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
  [data-seq data-prev]
  (let [canvas (by-id "canvas_graph")
        canvas-context (.getContext (by-id "canvas_graph") "2d")
        width (.-width canvas)
        height (.-height canvas)]
    (let [data-seq (map (fn [v i] {:val v
                                   :color-index 0
                                   :tip (inc v)
                                   :index i})
                        data-seq
                        (range (count data-seq)))
          max-val (apply max (map :val data-seq))
          val-multi (if (zero? max-val)
                      0
                      (/ height max-val))
          data-seq (map (fn [d]
                          (assoc d :val (* (:val d) val-multi)))
                        data-seq)
          data-seq (if data-prev
                     (map add-render-data
                          data-seq
                          data-prev)
                     data-seq)]
      (.clearRect canvas-context 0 0
                  width
                  height)
      (set! (.-fillStyle canvas-context) (.-color js/window))
      (doseq [{:keys [val index color-index tip]} (filter (fn [{:keys [index]}]
                                                            (even? index))
                                                          data-seq)]
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
      data-seq)))


(defn scene-setup
  []
  (let [canvas (.createElement js/document "canvas")]
    (aset canvas "width" window/innerWidth)
    (aset canvas "height" window/innerHeight)
    (.setAttribute canvas "id" "canvas_graph")
    (.appendChild (.-body js/document) canvas)))


(defn scene-destroy
  []
  (.removeChild (.-body js/document) (by-id "canvas_graph")))
