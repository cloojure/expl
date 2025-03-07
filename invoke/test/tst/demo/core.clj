(ns tst.demo.core
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [clojure.edn :as edn]
    [schema.core :as s]
    [tupelo.core :as t]
    [tupelo.schema :as tsk]
    [tupelo.string :as str]
    ))

(dotest
  (is= 5 (+ 3 2))

  (is= 7 (add2-public 4 3)) ; normal public access
  (is= 7 (#'demo.core/add2-private 4 3)) ; use fully-qualified Var for private access

  (is= [:a 1 :b 2] (mapv edn/read-string [":a" "1" ":b" "2"]))
  (is= {:a 1 :b 2}
    (hash-map :a 1 :b 2) ; 4 scalar args
    (apply hash-map [:a 1 :b 2])) ; a single vector arg

  ; cannot coerce data into a map via these expressions
  (throws? (into {} :a 1 :b 2))
  (throws? (into {} [:a 1 :b 2]))
  (throws? (hash-map [:a 1 :b 2]))
  (throws? (edn/read-string [":a" "1" ":b" "2"]))

  )
