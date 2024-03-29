(ns tiltontec.example.intro
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.matrix.api
             :refer [make cF cF+ cFn cFonce cI cf-freeze
                     mpar mget mset! mswap! mset! with-cc
                     fasc fmu fm! minfo]]
            [tiltontec.web-mx.api
             :refer [evt-md target-value make-css-inline
                     img section h1 h2 h3 input footer p a
                     span i label ul li div button br
                     svg g circle p span div text radialGradient defs stop
                     rect ellipse line polyline path polygon script use]]
            [tiltontec.example.util :as exu]))

;;; -------------------------------------------------------

(defn demo-svg []
  (svg {:width 100 :height 180}
    (rect {:x            10 :y 10 :width 30 :height 30
           :stroke       :red
           :stroke-width 5 :fill :transparent})
    (rect {:x       60 :y 10 :rx 10 :ry 10 :width 30 :height 30
           :stroke  :black :stroke-width 5 :fill (cI :transparent)
           :onclick (cF (fn foo [e]
                          (mset! me :fill :red)))})
    (path {:d    ["M20,60" "Q40,35" "50,60" "T90,60"]
           :fill :none :stroke :blue :stroke-width 5})

    (polygon {:points [50 80 55 100 70 100 60 110 65 125 50 115 35 125 40 110 30 100 45 100]
              :stroke :green :stroke-width 5 :fill :transparent})
    ))

;;; --- matrix build ------------------------------------------------

(defn matrix-build! []
  (make ::intro
    :mx-dom (div {:class "intro"}
              (h2 "Nothing to see here.")
              (p "But feel free to hang out.")
              (button
                {:class   :button-2
                 :disabled false
                 :onclick #(js/alert "Hello, world.") ;;#(mset! (fmu :climber (evt-md %)) :src  "image/chain-dag.jpeg")
                 }
                "Speak")
              (demo-svg)
              (img {:alt "Female professional rock climber hanging under climbing wall overhang."
                    :src (cI "image/climber.jpg")
                    :max-width "100%" :max-height "100%" :height :auto}
                {:name :climber}))))

(exu/main matrix-build!)

