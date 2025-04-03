(ns tst.demo.main
  (:use demo.main
        tupelo.core
        tupelo.test)
  (:require
    [demo.debug :as dbg]
    ))

; Use an explicit EDN map in a single-quote string
(verify
  ; # implicit call to `-main` entrypoint
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -M -m demo.main    '{ :a 1 :b 2 }'"))

  ; in `deps.edn`, we have define `run-m` => demo.main (`-main` implicit)
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -M:run-m           '{ :a 1 :b 2 }'"))
  )
