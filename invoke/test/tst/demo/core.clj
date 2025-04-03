(ns tst.demo.core
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [clojure.edn :as edn]
    [demo.debug :as dbg]
    [schema.core :as s]
    [tupelo.misc :as misc]
    [tupelo.schema :as tsk]
    [tupelo.string :as str]
    ))

(verify
  ;---------------------------------------------------------------------------------------------------
  ; # explicit call to `-main` entrypoint
  (newline)

  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X demo.core/main-x     :a 1 :b 2   ")) ; implicit EDN map
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X demo.core/main-x  '{ :a 1 :b 2 }'")) ; explicit EDN map

  ; in `deps.edn`, we have define `run-x` => demo.core/main-x
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X:run-x      :a 1 :b 2   ")) ; implicit EDN map
  (is= {:a 1 :b 2} (dbg/cmdstr->main->edn "clj -X:run-x   '{ :a 1 :b 2 }'")) ; explicit EDN map
  )

(verify
  ;---------------------------------------------------------------------------------------------------
  ; # explicit call to `-main` entrypoint
  (newline)

  (is= {:a 91 :b 2} (dbg/cmdstr->main->edn "clj -M -m demo.core/-main2  '{ :a 1 :b 2 }'")) ; explicit
  ; EDN map
  )


(verify
  ;---------------------------------------------------------------------------------------------------
  ; # explicit call to `-main` entrypoint
  (newline)

  (is= (dbg/cmdstr->main->edn "clj -X demo.core/-main  :a 1 :b 2")
    {:cmdline-args [{:a 1 :b 2}] ; implicit EDN map, in a seq
     :stdio-args   nil})

  (is= (dbg/cmdstr->main->edn "clj -X demo.core/-main  '{ :a 1 :b 2 }'")
    {:cmdline-args [{:a 1 :b 2}] ; explicit EDN map, in a seq
     :stdio-args   nil})

  ; deps.edn alias =>  :run  {:exec-fn demo.core/-main}
  (is= (dbg/cmdstr->main->edn "clj -X:run      :a 1 :b 2")
    {:cmdline-args [{:a 1 :b 2}] ; implicit EDN map, in a seq
     :stdio-args   nil})

  ; deps.edn alias =>  :run  {:exec-fn demo.core/-main}
  (is= (dbg/cmdstr->main->edn "clj -X:run   '{ :a 1 :b 2 }'")
    {:cmdline-args [{:a 1 :b 2}] ; explicit EDN map, in a seq
     :stdio-args   nil})


  ;---------------------------------------------------------------------------------------------------
  ; # uses default `-main` entrypoint
  (newline)

  (let [result (dbg/cmdstr->main->edn "clj -M -m demo.core    :a 1 :b 2")]
    (is= result
      {:cmdline-args '(":a" "1" ":b" "2") ; seq of strings
       :stdio-args   nil})
    (is= [:a 1 :b 2]
      (dbg/str-args->edn-vec (:cmdline-args result))))

  (let [result (dbg/cmdstr->main->edn "clj -M -m demo.core    ':a 1 :b 2'")]
    (is= result
      {:cmdline-args '(":a 1 :b 2") ; seq of 1 string
       :stdio-args   nil})
    (let [edn-map-str (str \{ (first (:cmdline-args result)) \})]
      (is= "{:a 1 :b 2}" edn-map-str)
      (is= {:a 1 :b 2}
        (edn/read-string edn-map-str))))

  (let [result (dbg/cmdstr->main->edn "clj -M -m demo.core    '{:a 1 :b 2}'")]
    (is= result
      {:cmdline-args '("{:a 1 :b 2}") ; seq of 1 string
       :stdio-args   nil})

    (is= {:a 1 :b 2} ; map from 1st string
      (edn/read-string (xfirst (:cmdline-args result)))))

  ;---------------------------------------------------------------------------------------------------
  ; # lein:  uses default `-main` entrypoint
  (newline)

  (let [result (dbg/cmdstr->main->edn "lein run    :a 1 :b 2")]
    (is= result
      {:cmdline-args '(":a" "1" ":b" "2") ; seq of strings
       :stdio-args   nil})
    (is= [:a 1 :b 2] (dbg/str-args->edn-vec (:cmdline-args result))))

  (let [result (dbg/cmdstr->main->edn "lein run    ':a 1 :b 2'")]
    (is= result
      {:cmdline-args '(":a 1 :b 2") ; seq of 1 string; NOT EDN collection
       :stdio-args   nil})
    (is= [:a] (dbg/str-args->edn-vec (:cmdline-args result))) ; ***** lost all but first EDN value

    (let [edn-map-str (str \{ (first (:cmdline-args result)) \})]
      (is= "{:a 1 :b 2}" edn-map-str)
      (is= {:a 1 :b 2} (edn/read-string edn-map-str))))

  (let [result (dbg/cmdstr->main->edn "lein run    '{:a 1 :b 2}'")]
    (is= result
      {:cmdline-args '("{:a 1 :b 2}") ; seq of 1 string
       :stdio-args   nil})
    (is=
      {:a 1 :b 2}   ; map from 1st string
      (edn/read-string (xfirst (:cmdline-args result)))))

  ;---------------------------------------------------------------------------------------------------
  ; # java -jar ./target/xxxxxx-standalone.jar :  uses default `-main` entrypoint
  (newline)

  ; create the uberjar
  (let [result (:out (misc/shell-cmd "lein clean; lein uberjar"))]
    (println "***** creating uberjar*****")
    (newline)
    (println result))

  (newline)
  (is= (dbg/cmdstr->main->edn "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  :a 1 :b 2")
    {:cmdline-args '(":a" "1" ":b" "2") ; seq of strings
     :stdio-args   nil})

  (let [result (dbg/cmdstr->main->edn "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  '{:a 1 :b 2}'")]
    ;(prn :result)
    ;(println result)
    (is= result
      {:cmdline-args ["{:a 1 :b 2}"] ; seq of 1 string
       :stdio-args   nil})

    (is= {:a 1 :b 2} ; map from 1st string
      (edn/read-string (xfirst (:cmdline-args result)))))

  (let [result (dbg/cmdstr->main->edn
                 "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  <<EOF
                   {:a 1
                    :b 2 }
                   EOF")]
    (is= result {:cmdline-args nil
                 :stdio-args   {:a 1 :b 2}}) ; parsed multi-line string from stdio
    )

  ; You *COULD* mix params from cmdline and stdio, but *PLEASE* just choose only one technique!!!
  (let [result (dbg/cmdstr->main->edn
                 "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  '{:x 7 :y 9}' <<EOF
                   {:a 1
                    :b 2 }
                   EOF")]
    ;(prn :result)
    ;(println result)
    (is= result {:cmdline-args ["{:x 7 :y 9}"] ; a seq of 1 string, ready to be parsed
                 :stdio-args   {:a 1 :b 2}}) ; parsed multi-line string from stdio
    )

  )
