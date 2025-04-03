(ns demo.main
  (:use tupelo.core)
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [schema.core :as s]
    [tupelo.schema :as tsk])
  (:gen-class))


(s/defn -main
  "Call like this (fully-qualified function name):
        clj -M -m demo.core  '{ :a 1 :b 2 }'  " ; explicit EDN map
  [arg-str :- s/Str]
  (assert (string? arg-str)) ; verify
  (let [opts (edn/read-string arg-str)]
    (prn opts)))
