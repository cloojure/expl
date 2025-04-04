(ns demo.ujar
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.schema :as tsk])
  (:gen-class))

(defn target
  [ctx]
  (assert (map? ctx))

  ; constant return value
  :target--result)

