(ns audio.sphere-plot-3
  (:require [utils.helpers
             :refer [set-html by-id update-in! get-html]]))


(def camera (THREE.PerspectiveCamera. 45 (/ window/innerWidth
                                            window/innerHeight) 0.1 1000))
(def renderer (THREE.WebGLRenderer. (clj->js {:antialias true})))
(def scene (THREE.Scene.))

(def fragment-shader "
          uniform vec3 color;
          uniform float opacity;

          varying vec3 vColor;

          void main() {
          gl_FragColor = vec4( vColor * color, opacity );
          }")

(def vertex-shader "
          uniform float amplitude;

          attribute vec3 displacement;
          attribute vec3 customColor;

          varying vec3 vColor;

          void main() {

          vec3 newPosition = position + amplitude * displacement;

          vColor = customColor;

          gl_Position = projectionMatrix * modelViewMatrix * vec4( newPosition, 1.0 );

          }")

(def shader-material (THREE.ShaderMaterial.
                      (clj->js {:uniforms (clj->js {:amplitude {:type "f" :value "5.0"}
                                                    :opacity {:type "f" :value "0.1"}
                                                    :color {:type "c"
                                                            :value (new THREE.Color
                                                                        0xff0000)}})
                                :attributes (clj->js {:displacement {:type "v3" :value []}
                                                      :customColor {:type "c" :value []}})
                                :vertexShader vertex-shader
                                :blending THREE.AdditiveBlending
                                :fragmentShader fragment-shader
                                :depthTest false
                                :transparent true
                                :linewidth 4})))


(def object (atom nil))


(defn normalize-data
  [i]
  (* 0.0002 i (- 1 (rand-int 2))))


(defn sound->display
  [data prev-data]
  (let [time (* (.getTime (new js/Date)) 0.01)
        perc-change (map (fn [d1 d2]
                           (let [perc (/ (.abs js/Math (- d1 d2)) d1)]
                             (if (or (js/isNaN perc)
                                     (= perc js/Infinity))
                               0
                               perc)))
                         prev-data
                         data)
        max-val  (apply max data)
        max-perc-change (apply max perc-change)
        obj @object]
    ;; (aset obj "rotation" "z" (+ (aget obj "rotation" "z")
    ;;                             (* 0.15 max-perc-change)))
    (aset obj "rotation" "y" (+ (aget obj "rotation" "y")
                                (/ max-perc-change 20)))

    (aset camera "rotation" "z" (+ (aget camera "rotation" "z")
                                   0.01))
    (when (> max-perc-change 4)
      (.offsetHSL (aget shader-material
                        "uniforms" "color" "value")
                  (* 5 (normalize-data max-perc-change)) 0 0))
    (let [displacements (aget shader-material
                              "attributes" "displacement" "value")]
      (aset shader-material "uniforms" "amplitude" "value" max-perc-change)
      (doseq [i (range (.-length displacements))]
        (let [d (aget displacements i)]
          (aset d
                "x"
                (normalize-data max-val))
          (aset d
                "y"
                (normalize-data max-val))
          (aset d
                "z"
                (normalize-data max-val))))))
  (aset shader-material "attributes" "displacement" "needsUpdate" true)
  (.render renderer scene camera)
  data)


(defn scene-setup
  []
  (.setSize renderer window/innerWidth window/innerHeight)
  (.appendChild (.-body js/document) (.-domElement renderer))
  (.setAttribute (.-domElement renderer) "id" "canvas_graph")
  (.set (.-position camera) 0 0 10)
  (let [geom (new THREE.SphereGeometry 3 64 64)]
    (aset geom "dyanimc" true)
    (let [obj (new THREE.Line geom
                   shader-material
                   THREE.LineStrip)]
      (aset obj "rotation" "x" 0.2)
      (let [vertices (aget obj "geometry" "vertices")
            v-count (.-length vertices)]
        (aset shader-material
              "attributes"
              "displacement"
              "value"
              (apply array (map #(new THREE.Vector3) (range v-count))))
        (aset shader-material
              "attributes"
              "customColor"
              "value"
              (apply array (map (fn [i]
                                  (let [c (new THREE.Color 0xffffff)]
                                    (.setHSL c
                                             (/ i v-count)
                                             0.5
                                             0.5)
                                    c))
                                (range v-count))))
        (.add scene obj)
        (reset! object obj)))))

(defn scene-destroy
  []
  (when-let [c (by-id "canvas_graph")]
    (.removeChild (.-body js/document) c)))
