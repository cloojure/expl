(ns demo.debug
  (:use tupelo.core)
  (:require
    [clojure.edn :as edn]
    [schema.core :as s]
    [tupelo.misc :as misc]
    [tupelo.schema :as tsk]
    [tupelo.string :as str]
    )
  (:gen-class))

(s/defn debug-line? :- s/Bool
  "Returns true iff a string contains a debug output line."
  [s :- s/Str]
  (str/contains-str? s ":dbg--"))

(s/defn stdout->nondebug-lines :- [s/Str]
  "Takes a multi-line string (e.g. from stdout), splits into separate lines, and removes all
  debug output lines."
  [outstr :- s/Str]
  (let [out-lines    (str/split-lines outstr)
        nondbg-lines (drop-if debug-line? out-lines)]
    nondbg-lines))

(s/defn str-args->edn-vec :- [s/Any]
  "Converts a sequence of string args into a vector of EDN data"
  [args :- [s/Str]]
  (mapv edn/read-string args))

(s/defn cmdstr->main->edn :- s/Any
  "Execute a BASH command-string that invokes `-main`, parse the stdout (after removing debug
  lines), returning the result as EDN data."
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

(s/defn -main
  "Call like either:
        clj -X demo.debug/-main     :a 1 :b 2           (implicit EDN map)
        clj -X demo.debug/-main  '{ :a 1 :b 2 }'        (explicit EDN map)"
  [opts :- tsk/KeyMap] ; Clojure converts args to EDN map automatically
  (assert (map? opts)) ; verify
  (prn opts))
