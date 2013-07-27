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
  [data data-prev]
  (let [canvas (by-id "canvas_graph")
        canvas-context (.getContext (by-id "canvas_graph") "2d")
        width (.-width canvas)
        height (.-height canvas)]
    (let [data (for [i (range (.-length data))]
                 {:val (aget data i)
                  :color-index 0
                  :tip (inc (aget data n))
                  :index n})
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
