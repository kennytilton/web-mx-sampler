(defproject com.tiltontec/web-mx-sampler "1.0.0-SNAPSHOT"
  :description "A Web un-Framework powered by Matrix(tm)"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/kennytilton/web-mx/"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/core.async "1.6.673"]
                 [org.slf4j/slf4j-nop       "1.7.30"]
                 [com.cognitect/transit-cljs "0.8.264"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [funcool/bide "1.7.0"]
                 [clj-http "3.10.3"]
                 [cljs-http "0.1.46"]
                 [cheshire "5.11.0"]
                 [com.taoensso/tufte "2.4.5"]
                 [com.tiltontec/matrix "5.0.1-SNAPSHOT"]
                 [com.tiltontec/web-mx "2.0.2-SNAPSHOT"]
                 [com.tiltontec/mxxhr "2.0.0-SNAPSHOT"]]
  :profiles
  {:dev
   {:dependencies [[org.clojure/clojurescript "1.10.773"]
                   [com.bhauman/figwheel-main "0.2.18"]
                   ;; optional but recommended
                   [com.bhauman/rebel-readline-cljs "0.1.4"]]
    :resource-paths ["target"]
    :clean-targets ^{:protect false} ["target"]}}
  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :source-paths ["src" "target/classes"]
  :clean-targets ["out" "release" :target-path]
  :target-path "target")