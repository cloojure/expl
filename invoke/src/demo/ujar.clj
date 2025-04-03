(ns demo.ujar
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.schema :as tsk]))

(s/defn some-fn
  [ctx :- tsk/KeyMap]
  (assert (map? ctx))
  :some-fn--result
  )
