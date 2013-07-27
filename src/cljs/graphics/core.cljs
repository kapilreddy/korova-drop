(ns graphics.core
  (:require [utils.helpers
             :refer [set-html by-id update-in!]]))


(def camera (THREE.PerspectiveCamera. 50 (/ window/innerWidth
                                            window/innerHeight) 1 2000))
(def renderer (THREE.WebGLRenderer. (clj->js {:antialias true})))
(def scene (THREE.Scene.))
(def group (new THREE.Object3D))

(defn create-nurbs
  []
  (let [nurbs-degree 3
        total 28
        nurbs-control-points (map (fn [i]
                                    (new THREE.Vector4
                                         (- (* (.random js/Math) 900) 200)
                                         (* (.random js/Math) 900)
                                         (- (* (.random js/Math) 900) 200)
                                         2))
                                  (range total))
        nurbs-knots (concat (repeat 0 2)
                            (map (fn [i]
                                   (.clamp THREE.Math
                                           (/ (+ i 1) (- total nurbs-degree))
                                           0
                                           1))
                                 (range total)))
        nurbs-curve (new THREE.NURBSCurve
                         nurbs-degree
                         (clj->js nurbs-knots)
                         (clj->js nurbs-control-points))
        nurbs-geometry (doto (new THREE.Geometry)
                         (aset "vertices" (.getPoints nurbs-curve 200)))
        nurbs-material (new THREE.LineBasicMaterial
                            (clj->js {:linewidth 10
                                      :color 0x883333
                                      :transparent true}))
        nurbs-line (new THREE.Line nurbs-geometry nurbs-material)]
    (.set (.-position nurbs-line) 200 -100 0)
    (.add group nurbs-line)))

(defn scene-setup
  []
  (.setSize renderer window/innerWidth window/innerHeight)

  (.appendChild (.-body js/document) (.-domElement renderer))
  (.set (.-position camera) 0 150 750)

  ;; Light setup
  (.add scene (new THREE.AmbientLight 0x808080))
  (let [directional-light (new THREE.DirectionalLight 0xffffff 1)]
    (.set (.-position directional-light) 1 1 1)
    (.add scene directional-light))

  (aset group "position" "y" 50)
  (doseq [n (range 3)]
    (create-nurbs))
  (.add scene group))

(defn render []
  (update-in! group ["rotation" "y"] (fn [i]
                                       (+ i 0.01)))
  (.render renderer scene camera))

(defn animate []
  (.requestAnimationFrame js/window animate)
  (render))

(scene-setup)
(animate)
