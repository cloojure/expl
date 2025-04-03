(ns demo.core-x
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.schema :as tsk])
  (:gen-class))

(s/defn any-func
  "Call like either:
        clj -X demo.cor3-x/any-func     :a 1 :b 2           (implicit EDN map)
        clj -X demo.cor3-x/any-func  '{ :a 1 :b 2 }'        (explicit EDN map)"
  [opts :- tsk/KeyMap] ; Clojure converts args to EDN map automatically
  (assert (map? opts)) ; verify
  (prn opts))

(s/defn -main
  "Call like either:
        clj -X demo.core/-main     :a 1 :b 2           (implicit EDN map)
        clj -X demo.core/-main  '{ :a 1 :b 2 }'        (explicit EDN map)"
  [opts :- tsk/KeyMap] ; Clojure converts args to EDN map automatically
  (assert (map? opts)) ; verify
  (prn opts))

