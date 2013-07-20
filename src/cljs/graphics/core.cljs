(ns graphics.core
  (:require [cljs.core.async :refer [chan sliding-buffer put! timeout]]
            [clojure.string :as string]
            [utils.helpers
            :refer [event-chan set-html by-id]])
  (:require-macros [cljs.core.async.macros :as m :refer [go alts!]]
                   [utils.macros :refer [go-loop]]))


(comment (defn draw-circle
           [context radius {:keys [x y]} style]
           (.beginPath context)         ;
           (.arc context x y radius 0 (* 2 Math.PI) false)
           (set! (.-fillStyle context) style)
           (.fill context)
           (set! (.-lineWidth context) 5)
           (set! (.-strokeStyle context) "#003300")
           (.stroke context))


         (defn generate-balls
           [n radius {:keys [width height]}]
           (map (fn [x]
                  {:position {:x (rand-int (- width x))
                              :y (rand-int (- width y))}
                   :radius radius
                   :vector {:x inc
                            :y dec}})
                (range n)))


         (defn next-position
           [{:keys [position vector radisu] :as ball}
            {:keys [width height]}]
           (let [] {:position {:x ((:x vector) (:x position))
                               :y ((:y vector) (:y position))}
                    :vector (let [x (:x position)
                                  y (:y position)]
                              (cond
                               (and (> (+ x (:radius ball)) width)
                                    (> (+ y (:radius ball)) height))
                               {:x dec-vel :y dec-vel}

                               (> (+ x (:radius ball)) width)
                               {:x dec-vel :y y}

                               (> (+ y (:radius ball)) height)
                               {:x x :y dec-vel}

                               (and (neg? (- x (:radius ball))) (neg? (- y (:radius ball))))
                               {:x inc-vel :y inc-vel}

                               (neg? (- x (:radius ball)))
                               {:x inc-vel :y y}

                               (neg? (- y (:radius ball)))
                               {:x x :y inc-vel}

                               :else vector))}))

         (defn -main
           []
           (let [canvas (by-id "canvas_graph")
                 canvas-context (.getContext canvas "2d")
                 width (.-width canvas)
                 height (.-height canvas)
                 inc-vel (fn [x]
                           (+ x 10))
                 dec-vel (fn [x]
                           (- x 10))
                 balls (generate-balls 1 30 {:width width
                                             :height height})]
             (go
              (loop [position {:x 0 :y 0}
                     vector {:x inc-vel :y inc-vel}]
                (<! (timeout 50))
                (.clearRect canvas-context  0 0 width height)
                (range 10)
                (doseq [{:keys [radius position]} balls]
                  (draw-circle canvas-context radius position "green"))
                (recur balls)))))
         (-main))
