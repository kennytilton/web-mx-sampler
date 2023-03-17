(ns tiltontec.example.ticktock
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.matrix.api
             :refer [make cF cF+ cFn cFonce cI cf-freeze c-value
                     mpar mget mset! mswap! mset! with-cc
                     fasc fmu fm! minfo]]
            [tiltontec.web-mx.api
             :refer [evt-md target-value make-css-inline js-interval-register
                     img section h1 h2 h3 input footer p a
                     span i label ul li div button br
                     svg g circle p span div]]
            [tiltontec.example.util :as ex-util]))

;;; -------------------------------------------------------

(defn lawrence-welk [beats]
  (div {:style {:display :flex}}
    (mapv #(p (str "ah, " % "..."))
      (map (fn [bn] (pp/cl-format nil "~r" (inc bn)))
        (range beats)))))

(defn time-color-value [me]
  (if-let [tc (fmu :timecolor)]
    (mget tc :value)
    (throw (js/Error "time-color-value> Unable to find widget named :timecolor"))))

(defn clock []
  (div {:class   "example-clock"
        :style   (cF (str "color:#"
                       (time-color-value me)))
        :content (cF (if-let [tick (mget me :tick)]
                       (-> tick
                         .toTimeString
                         (str/split " ")
                         first)
                       "*checking*"))}
    {:tick   (cI nil)
     ;; todo md-quiesce handling
     :ticker (let [i (atom nil)]
               (cF+ [:watch (fn [_ me new _ _]
                              (prn :watch!! new)
                              (reset! i new))
                     :on-quiesce (fn [c]
                                   (prn :finalizer!!! (c-value c))
                                   #_ (when (number? @i)
                                     (js/clearInterval @i))
                                   (when (number? (c-value c))
                                     (js/clearInterval (c-value c))))]
                 (when (mget (fmu :app) :ticking)
                   (js/setInterval #(mset! me :tick (js/Date.)) 1000))))}))

(defn color-input [initial-color]
  (div {:class "color-input"} {:name :color-inpt}
    "Hex Time Color #&nbsp"
    (input {:tag/type "text"
            :value    (cI initial-color)
            :onchange (fn [e]
                        (mset! (evt-md e) :value
                          (target-value e)))
            :title    "RGB color in hex format, either XXX or XXXXXX, without the octothorpe."
            :style    (cF (make-css-inline me
                            :width "100%"
                            :max-width "96px"
                            :border :solid :border-width :thin
                            :background-color (cF (let [rgb-status (mget (mget me :tag) :value-status)]
                                                    (case rgb-status
                                                      :invalid "#fcc"
                                                      ;:valid "#0f0"
                                                      ;:blank "cyan"
                                                      "white")))))
            }
      {:name         :timecolor
       :value-status (cF (let [rgb (mget me :value)]
                           (cond
                             (str/blank? rgb) :blank
                             (not-any? #{(count rgb)} [3 6]) :invalid
                             (re-matches #"[0-9a-fA-F]+" rgb) :valid
                             :else :invalid)))})))

(defn matrix-build! []
  (make ::ticktock
    :mx-dom (cFonce
              (div {:class "ticktock"}
                {:name    :app
                 :ticking (cI true)}
                ; the theme below is that the DIV (and every HTML element that takes children)
                ; does not need to be given a flat list of children. The macrology hides
                ; the flattening and removal of nils done for us, which we need when
                ; generating children with collections and conditionals. And we can get children
                ; by calling functions such as 'lawerence-welk' and 'color-input'.
                (h1 "Hello, world.")
                (when false (h2 "Hi, Mom!"))
                (when (mget me :ticking)
                  [(h2 "The time is now....")
                   (lawrence-welk 2)
                   (clock)])
                (color-input "57a8a4")
                (button
                  {:class   "button-2"
                   :style   {:margin-top "16px"}
                   :onclick #(mswap! (fmu :app (evt-md %)) :ticking not)}
                  (if (mget (fmu :app) :ticking)
                    "Stop" "Start"))))))

(ex-util/main matrix-build!)

