(ns demo.core
  (:use tupelo.core)
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [schema.core :as s])
  (:gen-class))

(defn add2-public [x y] (+ x y))
(defn- add2-private [x y] (+ x y))

(defn -main
  [& args]
  (spy :main--enter)
  (do
    (spyx args)
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
  (let [in-str (slurp (io/reader System/in))
        in-data (edn/read-string in-str)]
    (spyx in-str)
    (spyx in-data)
    )

  (if (and
        (= 1 (count args))
        (map? (xfirst args)))
    (prn :single-map-arg)
    (prn :other-arg)
    )

  (spy :main--leave)
  )
