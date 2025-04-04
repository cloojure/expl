(ns demo.ujar
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.schema :as tsk])
  )

(s/defn target :- s/Any
  [ctx :- tsk/KeyMap]
  (assert (map? ctx))

  ; constant return value
  :target--result)

