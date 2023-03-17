(ns tiltontec.example.util
  (:require [goog.dom :as gdom]
            [cognitect.transit :as trx]
            [tiltontec.model.core :refer [mget]]
            [tiltontec.web-mx.api :refer [tag-dom-create]]))

;;; --- json -----------------------------

(defn map-to-json [map]
  (trx/write (trx/writer :json) map))

(defn json-to-map [json]
  (trx/read (trx/reader :json) json))

;;; ---

(defn main [mx-builder]
  (println "[main]: loading")
  (let [root (gdom/getElement "app")
        ;; ^^^ "app" must be ID of DIV defined in index.html
        app-matrix (mx-builder)
        app-dom (tag-dom-create
                  (mget app-matrix :mx-dom))]
    (set! (.-innerHTML root) nil)
    (gdom/appendChild root app-dom)))