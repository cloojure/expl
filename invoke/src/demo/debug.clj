(ns demo.debug
  (:use tupelo.core)
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [schema.core :as s]
    [tupelo.schema :as tsk])
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

(verify
  (is= [:a 1 :b 2] (str-args->edn-vec [":a" "1" ":b" "2"]))
  (is= [:a] (str-args->edn-vec [":a 1 :b 2"])) ; ***** only get 1st item if not EDN collection in string
  (is= [:a 1 :b 2] (xfirst (str-args->edn-vec ["[:a 1 :b 2]"]))) ; string must be EDN collection to retain all values
  (is= {:a 1 :b 2} (xfirst (str-args->edn-vec ["{:a 1 :b 2}"]))) ; string must be EDN collection to retain all values
  )

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

