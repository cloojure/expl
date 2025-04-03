(ns tst.demo.core-m
  (:use demo.core-m
        tupelo.core
        tupelo.test)
  (:require
    [demo.debug :as dbg]
    ))

(verify ; Use an explicit EDN map in a single-quote string
  ; # implicit call to `-main` entrypoint
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -M -m demo.core-m    '{ :a 1 :b 2 }'"))

  ; in `deps.edn`, we have define `run-m` => demo.core-m (`-main` implicit)
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -M:run-m             '{ :a 1 :b 2 }'"))
  )
