(ns tst.demo.core
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [clojure.edn :as edn]
    [schema.core :as s]
    [tupelo.misc :as misc]
    [tupelo.schema :as tsk]
    [tupelo.string :as str]
    ))

(verify
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

(s/defn debug-line? :- s/Bool
  [s :- s/Str]
  (str/contains-str? s ":dbg--"))

(s/defn stdout->nondebug-lines :- [s/Str]
  [outstr :- s/Str]
  (let [out-lines    (str/split-lines outstr)
        nondbg-lines (drop-if debug-line? out-lines)]
    nondbg-lines))

(verify
  (isnt (debug-line? "dbg--a"))
  (is (debug-line? ":dbg--b"))

  (let [result (str/join
                 (stdout->nondebug-lines
                   ":dbg--aaa
                   { :a 1 :b 2
                   :dbg--bbb
                   :c 3 }
                  :dbg--zzz
                   "))
        parsed (edn/read-string result)]
    (is= parsed {:a 1 :b 2 :c 3})))

(s/defn cmdstr->main->edn :- s/Any
  [cmdstr :- s/Str]
  (println "testing cmdstr:    " cmdstr)
  (let
    [out          (:out (misc/shell-cmd cmdstr))
     ;>>           (prn :out)
     ;>>           (println out)
     keep-lines   (stdout->nondebug-lines out)
     out-nondebug (str/join \space keep-lines)
     ;>>           (prn :out-nondebug)
     ;>>           (println out-nondebug)
     out-edn      (edn/read-string out-nondebug)]
    ;(nl)
    ;(prn :out)
    ;(println out)
    ;(nl)
    out-edn))

(verify
  (is (str/contains-str-frags? (:out (misc/shell-cmd "clj --version"))
        "Clojure" "CLI" "version" "1.12"))

  ;---------------------------------------------------------------------------------------------------
  ; # explicit call to `-main` entrypoint
  (newline)

  (is= (cmdstr->main->edn "clj -X demo.core/-main  :a 1 :b 2")
    {:cmdline-args [{:a 1 :b 2}] ; implicit EDN map, in a seq
     :stdio-args   nil})

  (is= (cmdstr->main->edn "clj -X demo.core/-main  '{ :a 1 :b 2 }'")
    {:cmdline-args [{:a 1 :b 2}] ; explicit EDN map, in a seq
     :stdio-args   nil})

  ; deps.edn alias =>  :run  {:exec-fn demo.core/-main}
  (is= (cmdstr->main->edn "clj -X:run demo.core/-main  :a 1 :b 2")
    {:cmdline-args [{:a 1 :b 2}] ; implicit EDN map, in a seq
     :stdio-args   nil})

  ; deps.edn alias =>  :run  {:exec-fn demo.core/-main}
  (is= (cmdstr->main->edn "clj -X:run demo.core/-main  '{ :a 1 :b 2 }'")
    {:cmdline-args [{:a 1 :b 2}] ; explicit EDN map, in a seq
     :stdio-args   nil})


  ;---------------------------------------------------------------------------------------------------
  ; # uses default `-main` entrypoint
  (newline)

  (is= (cmdstr->main->edn "clj -M -m demo.core    :a 1 :b 2")
    {:cmdline-args '(":a" "1" ":b" "2") ; seq of strings
     :stdio-args   nil})

  (is= (cmdstr->main->edn "clj -M -m demo.core    ':a 1 :b 2'")
    {:cmdline-args '(":a 1 :b 2") ; seq of 1 string
     :stdio-args   nil})

  (let [result (cmdstr->main->edn "clj -M -m demo.core    '{:a 1 :b 2}'")]
    (is= result
      {:cmdline-args '("{:a 1 :b 2}") ; seq of 1 string
       :stdio-args   nil})

    (is= {:a 1 :b 2} ; map from 1st string
      (edn/read-string (xfirst (:cmdline-args result)))))

  ;---------------------------------------------------------------------------------------------------
  ; # lein:  uses default `-main` entrypoint
  (newline)

  (is= (cmdstr->main->edn "lein run    :a 1 :b 2")
    {:cmdline-args '(":a" "1" ":b" "2") ; seq of strings
     :stdio-args   nil})

  (is= (cmdstr->main->edn "lein run    ':a 1 :b 2'")
    {:cmdline-args '(":a 1 :b 2") ; seq of 1 string
     :stdio-args   nil})

  (let [result (cmdstr->main->edn "lein run    '{:a 1 :b 2}'")]
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
  (is= (cmdstr->main->edn "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  :a 1 :b 2")
    {:cmdline-args '(":a" "1" ":b" "2") ; seq of strings
     :stdio-args   nil})

  (let [result (cmdstr->main->edn "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  '{:a 1 :b 2}'")]
    ;(prn :result)
    ;(println result)
    (is= result
      {:cmdline-args ["{:a 1 :b 2}"] ; seq of 1 string
       :stdio-args   nil})

    (is= {:a 1 :b 2} ; map from 1st string
      (edn/read-string (xfirst (:cmdline-args result)))))

  (let [result (cmdstr->main->edn
                 "java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  <<EOF
                   {:a 1
                    :b 2 }
                   EOF")]
    (is= result {:cmdline-args nil
                 :stdio-args   {:a 1 :b 2}}) ; parsed multi-line string from stdio
    )

  ; You *COULD* mix params from cmdline and stdio, but *PLEASE* just choose only one technique!!!
  (let [result (cmdstr->main->edn
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
