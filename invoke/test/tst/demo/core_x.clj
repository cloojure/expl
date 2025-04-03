(ns ^:test-refresh/focus tst.demo.core-x
  (:use demo.core-x
        tupelo.core
        tupelo.test)
  (:require
    [demo.debug :as dbg]
    ))

(verify ; Use an explicit EDN map in a single-quote string
  ; implicit `-main` doesn't work
  (throws?         (dbg/cmdstr->main->edn "clj -X demo.core-x           '{ :a 1 :b 2 }'"))

  ; # explicit call to `-main` entrypoint
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X demo.core-x/-main     '{ :a 1 :b 2 }'"))

  ; in `deps.edn`, we have define `run-x` => demo.core/main-x
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X:run-x                 '{ :a 1 :b 2 }'"))

  ; # explicit call to arbitrary function
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X demo.core-x/any-func  '{ :a 1 :b 2 }'")))

