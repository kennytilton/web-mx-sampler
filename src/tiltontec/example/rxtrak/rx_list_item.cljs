(ns tiltontec.example.rxtrak.rx-list-item
  (:require [cljs.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.util.base :refer [mx-type]]
            [tiltontec.util.core :refer [pln xor now]]
            [tiltontec.cell.base :refer [unbound *within-integrity* *defer-changes*]]
            [tiltontec.cell.core :refer-macros [cF cF+ cFn cF+n cFonce] :refer [cI]]
            [tiltontec.cell.poly :refer [md-quiesce]]
            [tiltontec.matrix.api :refer [fn-watch]]
            [tiltontec.cell.synapse
             :refer-macros [with-synapse]
             :refer []]

            [tiltontec.model.core :refer [matrix mpar mget mset! mswap!
                                          fm-navig mxi-find mxu-find-type
                                          kid-values-kids] :as md]
            [tiltontec.web-mx.base :refer [tag-dom]]
            [tiltontec.web-mx.html
             :refer [io-read io-upsert io-clear-storage
                     tag-dom-create
                     mxu-find-tag mxu-find-class tagfo
                     dom-has-class dom-ancestor-by-tag]
             :as tag]
            [tiltontec.mxxhr.core
             :refer [make-xhr send-xhr send-unparsed-xhr xhr-send xhr-await xhr-status
                     xhr-status-key xhr-resolved xhr-error xhr-error? xhrfo synaptic-xhr synaptic-xhr-unparsed
                     xhr-selection xhr-to-map xhr-name-to-map xhr-response]]
            [tiltontec.web-mx.api :as wx
             :refer [section header h1 input footer p a span label ul li div button br i]]
            [tiltontec.web-mx.style :refer [make-css-inline]]

            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.dom.forms :as form]

            [tiltontec.example.rxtrak.rx
             :refer [make-rx rx-title rx-created bulk-rx
                     rx-completed rx-upsert rx-delete! load-all
                     rx-id rx-toggle-completed! rx-due-by]]
            [cljs-time.coerce :as tmc]
            [clojure.string :as $]))

(declare rx-edit
  adverse-event-checker
  due-by-input)

(defn rx-list-item [me rx matrix]
  (assert rx "no rx entering rx-list-item")
  (let [ul-tag me]
    (li {:class   (cF [(when (mget me :selected?) "chosen")
                       (when (mget me :editing?) "editing")
                       (when (rx-completed rx) "completed")])
         :display (cF (if-let [route (mget matrix :route)]
                        (cond
                          (or (= route "All")
                            (xor (= route "Active")
                              (rx-completed rx))) "block"
                          :default "none")
                        "block"))}
      ;;; custom slots
      {:rx        rx
       ;; above is also key to identify lost/gained LIs, in turn to optimize list maintenance
       :selected? (cF (some #{rx} (mget ul-tag :selections)))
       :editing?  (cI false)}

      (let [rx-li me]
        [(div {:class "view"}
           (input {:class   "toggle" ::tag/type "checkbox"
                   :checked (cF (not (nil? (rx-completed rx))))
                   :onclick #(rx-toggle-completed! rx)})
           (label {:onclick    (fn [evt]
                                 (mswap! ul-tag :selections
                                   #(if (some #{rx} %)
                                      (remove #{rx} %)
                                      (conj % rx))))
                   :ondblclick #(do
                                  (mset! rx-li :editing? true)
                                  (wx/input-editing-start
                                    (dom/getElementByClass "edit" (tag-dom rx-li))
                                    (rx-title rx)))}
             (rx-title rx))
           (adverse-event-checker rx)
           (due-by-input rx)
           (button {:class   "destroy"
                    :onclick #(rx-delete! rx)}))
         (letfn [(rx-edt [event]
                   (rx-edit event rx-li))]
           (input {:class     "edit"
                   :onblur    rx-edt
                   :onkeydown rx-edt}))]))))

(defn rx-edit [e rx-li]
  (let [edt-dom (.-target e)
        rx (mget rx-li :rx)
        li-dom (tag-dom rx-li)]

    (when (classlist/contains li-dom "editing")
      (let [title (str/trim (form/getValue edt-dom))
            stop-editing #(mset! rx-li :editing? false)]
        (cond
          (or (= (.-type e) "blur")
            (= (.-key e) "Enter"))
          (do
            (stop-editing)                                  ;; has to go first cuz a blur event will sneak in
            (if (= title "")
              (rx-delete! rx)
              (mset! rx :title title)))

          (= (.-key e) "Escape")
          ;; this could leave the input field with mid-edit garbage, but
          ;; that gets initialized correctly when starting editing
          (stop-editing))))))

;;; --- due-by input -------------------------------------------

(defn due-by-input [rx]
  (input {::tag/type "date"
          :class     "due-by"
          :value     (cFn (when-let [due-by (rx-due-by rx)]
                            (let [db$ (tmc/to-string (tmc/from-long due-by))]
                              (subs db$ 0 10))))

          :oninput   #(mset! rx :due-by
                        (tmc/to-long
                          (tmc/from-string
                            (form/getValue (.-target %)))))

          :disabled  (cF (rx-completed rx))

          :style     (cFonce (make-css-inline me
                               :border "none"
                               :font-size "18px"
                               :padding "8px"
                               :display (cF (if (and (rx-completed rx)
                                                  (not (rx-due-by rx)))
                                              "none" "block"))
                               :background-color (cF (when-let [clock (mxu-find-class (:tag @me) "std-clock")]
                                                       (prn :bgcolor-runs!!!)
                                                       (if-let [due (rx-due-by rx)]
                                                         (do
                                                           (prn :due-by!!! due)
                                                           (if (rx-completed rx)
                                                             _cache ;; cF expansion has _cache (prior value) in lexical scope
                                                             (let [time-left (- due (mget clock :clock))]
                                                               (prn :time-left time-left)
                                                               (cond
                                                                 (neg? time-left) "red"
                                                                 (< time-left (* 24 3600 1000)) "coral"
                                                                 (< time-left (* 2 24 3600 1000)) "yellow"
                                                                 :default "cyan"))))
                                                         "linen")))))}))

;;; -----------------------------------------------------------
;;; --- adverse events ----------------------------------------

(defn de-whitespace [s]
  ($/replace s #"\s" ""))

(def ae-by-brand "https://api.fda.gov/drug/event.json?search=patient.drug.openfda.brand_name:~(~a~)&limit=3")

(defn ae-brand-uri [rx]
  (pp/cl-format nil ae-by-brand
    (js/encodeURIComponent (rx-title rx))))

(defn xhr-scavenge [xhr]
  (when-not (or (= xhr unbound) (nil? xhr))
    (md-quiesce xhr)))

(defn adverse-event-checker [rx]
  (assert rx "no rx entering adverse-eent-checker")
  (prn :rx-title (rx-title rx))
  (i
    {:class   "aes material-icons"
     :title   "Nothing to take seriously; the AE DB almost always has something. Try 'dog', tho."
     :onclick #(js/alert "Feature to display AEs not yet implemented")
     :style   (cF (str "font-size:36px"
                    ";display:" (case (mget me :aes?)
                                  :no "none"
                                  "block")
                    ";color:" (case (mget me :aes?)
                                :undecided "gray"
                                :yes "orange"
                                ;; should not get here
                                "white")))}

    {:lookup   (cF+ [:watch (fn-watch (xhr-scavenge old))]
                 (make-xhr (pp/cl-format nil ae-by-brand
                             (js/encodeURIComponent
                               (de-whitespace (rx-title rx))))
                   {:name       name :send? true
                    :fake-delay (+ 500 (rand-int 2000))}))
     :response (cF (when-let [xhr (mget me :lookup)]
                     (xhr-response xhr)))
     :aes?     (cF (if-let [r (mget me :response)]
                     (if (= 200 (:status r)) :yes :no)
                     :undecided))}
    "warning"))

(defn ae-explorer [rx]
  (button {:class   "li-show"
           :style   (cF (str "display:"
                          (or (when-let [xhr (mget me :ae)]
                                (let [aes (xhr-response xhr)]
                                  (when (= 200 (:status aes))
                                    "block")))
                            "none")))
           :onclick #(js/alert "Feature not yet implemented.")}

    {:ae         (cF+ [:watch (fn-watch
                                (when-not (or (= old unbound) (nil? old))
                                  (md-quiesce old)))]
                   (when (mget (mxu-find-class me "ae-autocheck") :on?)
                     #_(prn :sending (pp/cl-format nil ae-by-brand
                                       (js/encodeURIComponent (rx-title rx))))
                     (make-xhr (pp/cl-format nil ae-by-brand
                                 (js/encodeURIComponent (rx-title rx)))
                       {:name name :send? true})))
     :aeresponse (cF (when-let [lookup (mget me :ae)]
                       (xhr-response lookup)))}

    (span {:style "font-size:0.7em;margin:2px;margin-top:0;vertical-align:top"}
      "View Adverse Events")))

