(ns tiltontec.example.omprakash
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.matrix.api
             :refer [make cF cF+ cFn cFonce cI cf-freeze
                     mpar mget mset! mswap! mset! with-cc
                     mdv! fasc fmu fm! minfo]]
            [tiltontec.web-mx.api
             :refer [evt-md target-value make-css-inline
                     title img section h1 h2 h3 input footer p a
                     span i label ul li div button br
                     svg g circle p span div]]
            [tiltontec.example.util :as ex-util]))

;;; --- the "DB" ----------------------------------------

(def lease-info
  (atom {:date-signed "01-02-2023"
         :occupancy   {:move-in  "02-02-2023"
                       :move-out "01-01-2023"}}))

;;; --- handy accessors ----------------------------------

(defn date-errors [id me]
  (mget (fm! id me) :errors))

(defn date-value [id me]
  (mget (fm! id me) :value))

(defn populated [id me]
  (not (str/blank? (date-value id me))))

(defn valid-populated [id me]
  (when-not (date-errors id me)
    (populated id me)))

;;; --- validation utilities ------------------------------

(defn dates-are-ascending? [a b]
  (let [[d-a m-a y-a] (str/split a #"-")
        [d-b m-b y-b] (str/split b #"-")
        comp- (compare (str/join "-" [y-a m-a d-a]) (str/join "-" [y-b m-b d-b]))]
    (or (neg? comp-)
      (zero? comp-))))

(defn valid-date-format? [date]
  (or (empty? date)
    (boolean (re-matches #"[\d]{2}\-[\d]{2}\-[\d]{4}" date))))

;;; --- components ----------------------------------------------------

(defn date-field [id initial-value]
  (div {}
    {:name   id
     ;; we collect here value and errors so component can represent
     :value  (cF (mdv! :date-in :value))
     :errors (cF (not (valid-date-format? (mget me :value))))}
    (input {:class       (cF (conj ["input"]
                               (when (mget (fmu id) :errors)
                                 "is-danger")))
            :type        "text"
            :value       (cI initial-value)
            :placeholder "DD-MM-YYYY"
            :oninput     #(mset! (evt-md %)
                            :value (target-value %))}
      {:name :date-in})
    (p {:class "help is-danger"
        :style (cF (str "visibility:"
                     (if (mget (fmu id) :errors)
                       "visible" "hidden")))}
      "incorrect input date format, use DD-MM-YYYY")))

(defn date-range [id]
  (div {:class ["my-6" "is-flex is-align-items-center "]}
    {:name   id
     :errors (cF (when (and (valid-populated :move-in me)
                         (valid-populated :move-out me))
                   (not (dates-are-ascending?
                          (date-value :move-in me)
                          (date-value :move-out me)))))}
    (p {:class "pr-3"} "from")
    (span {:class "pr-3"}
      (date-field :move-in
        (get-in @lease-info [id :move-in])))
    (p {:class "pr-3"} "to")
    (span {:class "pr-3"}
      (date-field :move-out
        (get-in @lease-info [id :move-out])))
    (p {:class "help is-danger"
        :style (cF (str "visibility:" (if (mget (fmu id) :errors) "visible" "hidden")))}
      "dates must be ascending")))

(defn submit-button []
  (button {:class    ["button" "my-3" "is-dark"]
           :disabled (cF (mget (fmu :lease) :errors))
           :onclick  #(let [me (evt-md %)]
                        (reset! lease-info
                          {:date-signed (date-value :date-signed me)
                           :occupancy   {:move-in  (date-value :move-in me)
                                         :move-out (date-value :move-out me)}})
                        (prn :set!!!! @lease-info))}
    "submit"))

;;; --- the app -----------------------------------------------

(defn matrix-build! []
  (make ::omprakash
    :mx-dom (cFonce
              (div {:class ["container" "my-6"]}
                {:name   :lease
                 :errors (cF (or
                               (some (fn [id]
                                       (let [d (fm! id me)]
                                         (or (str/blank? (mget d :value))
                                           (mget d :errors))))
                                 [:date-signed :move-in :move-out])
                               (mget (fm! :occupancy me) :errors)))}
                (h1 "Rental Agreement")
                (date-field :date-signed
                  (:date-signed @lease-info))
                (date-range :occupancy)
                (submit-button)))))

(ex-util/main matrix-build!)


