(ns demo.core
  (:use tupelo.core)
  (:require
    [clojure.edn :as edn]
    [clojure.pprint :as pp]
    [clojure.java.io :as io]
    [schema.core :as s]
    [tupelo.schema :as tsk]

    [demo.ujar]
    )
  (:gen-class))


(s/defn dispatch :- s/Any
  [ctx :- tsk/KeyMap]
  ; (nl)
  ; (spyx-pretty :dispatch-fn ctx)
  (let [invoke-fn (grab :invoke-fn ctx)
        ctx       (dissoc ctx :invoke-fn)
        form      (list invoke-fn ctx)]
    ; (nl)
    ; (spyx invoke-fn)

    ; (prn :92 (find-var invoke-fn))
    ; (prn :93 (requiring-resolve invoke-fn))
    ; (prn :94 (qualified-symbol? invoke-fn))
    ; (prn :95 (simple-symbol? invoke-fn))
    ; (nl)
    ; (prn :form)
    ; (pp/pprint form)
    ; (nl)
    (eval form)))

; NOTE:  Testing code will strip all output to STDIO containing fragment ":dbg--".
;        All other output will be parsed into an EDN expression

(defn -main
  "Call from JAR file like:
        lein clean; lein uberjar
        java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  <<EOF
          {:a 1
           :b 2 }
        EOF "
  [& args]
  (spy :dbg--main--enter)
  (do
    (spyx :dbg--args args)
    ;-----------------------------------------------------------------------------
    ; Results:
    ;-----------------------------------------------------------------------------
    ; > clj -X demo.core/-main  :a 1 :b 2
    ;     => ({:a 0, :b 2})     # implicit EDN map, in a seq
    ;
    ; > clj -X demo.core/-main  '{ :a 1 :b 2 }'
    ;     => ({:a 0, :b 2})     # explicit EDN map, in a seq
    ;
    ; > clj -X:run  :a 1 :b 2   # deps.edn alias =>  :run  {:exec-fn demo.core/-main}
    ;     => ({:a 1, :b 2})     # implicit EDN map, in a seq
    ;
    ;-----------------------------------------------------------------------------
    ; > clj -M -m demo.core    :a 1 :b 2      # uses default `-main` entrypoint
    ;     => (":a" "1" ":b" "2")    # seq of strings
    ;
    ; > clj -M -m demo.core   ':a 1 :b 2'     # uses default `-main` entrypoint
    ;     => (":a 1 :b 2")          # seq of 1 string
    ;
    ; > clj -M -m demo.core  '{:a 1 :b 2}'    # uses default `-main` entrypoint
    ;     => ("{ :a 1 :b 2 }")      # seq of 1 string
    ;
    ;-----------------------------------------------------------------------------
    ; > lein run  :a 1 :b 2
    ;     => (":a" "1" ":b" "2")      # seq of strings
    ;
    ; > lein uberjar                  # create the uberjar
    ;
    ; > java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  :a 1 :b 2
    ;       => [":a" "1" ":b" "2"]    # seq of strings
    ;
    ; > java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  '{ :a 1 :b 2 }'
    ;       => ["{ :a 1 :b 2 }"]      # seq of 1 string
    ;
    ; > java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  <<EOF
    ; heredoc> { :a 1
    ; heredoc>   :b 2 }
    ; heredoc> EOF
    ;
    ;     args => nil   ; stdin => no args to main program
    )

  ; informational printout
  (if (and
        (= 1 (count args))
        (map? (xfirst args)))
    (prn :dbg--args-single-map)
    (prn :dbg--args-other))

  (println "{")     ; beginning of EDN output map
  (prn :cmdline-args) ; key of mapentry #1
  (prn args)        ; val of mapentry #1
  (nl)

  (let [stdio-str (slurp (io/reader System/in))
        opts      (edn/read-string stdio-str)]
    (prn :dbg--in-str stdio-str)
    (prn :dbg--in-data opts)
    (nl)
    (prn :stdio-args) ; key of mapentry #2
    (prn opts) ; val of mapentry #2
    (let [invoke-fn (:invoke-fn opts)]
      (when-not (nil? invoke-fn)
        (prn :invoke-result) ; key of mapentry #3
        (prn (dispatch opts)) ; val of mapentry #3
        ))
    )
  (println "}")     ; end of EDN output map

  (spy :dbg--main--leave)
  )

