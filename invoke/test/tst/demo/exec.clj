(ns tst.demo.exec
  (:use demo.exec
        tupelo.core
        tupelo.test)
  (:require
    [demo.debug :as dbg]
    ))

; Use an explicit EDN map in a single-quote string
(verify
  ; # explicit call to `-main` entrypoint
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X demo.exec/-main     '{ :a 1 :b 2 }'"))

  ; # explicit call to arbitrary function
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X demo.exec/any-func  '{ :a 1 :b 2 }'"))

  ; in `deps.edn`, we define alias `run-x` => demo.exec/-main
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X:run-x               '{ :a 1 :b 2 }'"))

  ; implicit `-main` doesn't work
  (throws?         (dbg/cmdstr->main->edn "clj -X demo.exec           '{ :a 1 :b 2 }'"))
  )

