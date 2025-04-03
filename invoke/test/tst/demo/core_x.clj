(ns tst.demo.core-x
  (:use demo.core-x
        tupelo.core
        tupelo.test)
  (:require
    [clojure.edn :as edn]
    [schema.core :as s]
    [tupelo.misc :as misc]
    [tupelo.schema :as tsk]
    [tupelo.string :as str]
    ))

(verify-focus
  ;---------------------------------------------------------------------------------------------------
  ; # explicit call to `-main` entrypoint
  (newline)

  (is= {:a 1 :b 2} (cmdstr->main->edn "clj -X demo.core/main-x     :a 1 :b 2   ")) ; implicit EDN map
  (is= {:a 1 :b 2} (cmdstr->main->edn "clj -X demo.core/main-x  '{ :a 1 :b 2 }'")) ; explicit EDN map

  ; in `deps.edn`, we have define `run-x` => demo.core/main-x
  (is= {:a 1 :b 2} (cmdstr->main->edn "clj -X:run-x      :a 1 :b 2   ")) ; implicit EDN map
  (is= {:a 1 :b 2} (cmdstr->main->edn "clj -X:run-x   '{ :a 1 :b 2 }'")) ; explicit EDN map
  )

