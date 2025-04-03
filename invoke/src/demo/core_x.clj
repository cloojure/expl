(ns demo.core-x
  (:use tupelo.core)
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [schema.core :as s]
    [tupelo.schema :as tsk])
  (:gen-class))

; NOTE:  Testing code will strip all output to STDIO containing fragment ":dbg--".
;        All other output will be parsed into an EDN expression
(defn -main
  "Call like either:
        clj -X demo.core/-main     :a 1 :b 2           (implicit EDN map)
        clj -X demo.core/-main  '{ :a 1 :b 2 }'        (explicit EDN map)"
  [opts :- tsk/KeyMap]
  (prn opts))

