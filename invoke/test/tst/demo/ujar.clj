(ns tst.demo.ujar
  (:use demo.ujar
        tupelo.core
        tupelo.test)
  (:require
    [demo.debug :as dbg]
    [tupelo.misc :as misc]
    ))

(verify
  (newline)
  (println "***** creating uberjar*****")
  (let [result (:out (misc/shell-cmd "lein clean; lein uberjar"))]
    (println result))

  ; Use an explicit EDN map in a single-quote string
  (let [result (dbg/cmdstr->main->edn
                 "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  <<EOF
                   {:a 1
                    :b 2 }
                   EOF")]
    (is= {:a 1 :b 2} (:stdio-args result))))
