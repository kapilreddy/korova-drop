(ns audio.sphere-plot
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
                                :linewidth 3})))


(def object (atom nil))


(defn normalize-data
  [i]
  (* 0.0002 i (- 1 (rand-int 2))))


(defn sound->display
  [data prev-data]
  (let [time (* (.getTime (new js/Date)) 0.01)
        max-val  (.apply Math/max Math data)
        obj @object]
    (aset obj "rotation" "y" (* 0.15 time))
    (aset obj "rotation" "z" (* 0.15 time))
    (aset shader-material
          "uniforms" "amplitude" "value"
          (* (.sin js/Math (* 0.5 time))))
    (.offsetHSL (aget shader-material
                      "uniforms" "color" "value")
                (* 0.05 (normalize-data max-val)) 0 0)
    (let [displacements (aget shader-material
                              "attributes" "displacement" "value")]
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
  (.render renderer scene camera))


(defn scene-setup
  []
  (.setSize renderer window/innerWidth window/innerHeight)
  (.appendChild (.-body js/document) (.-domElement renderer))
  (.set (.-position camera) 0 0 7)
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
