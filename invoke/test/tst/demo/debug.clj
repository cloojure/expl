(ns ^:test-refresh/focus tst.demo.debug
  (:use demo.debug
        tupelo.core
        tupelo.test)
  (:require
    [clojure.edn :as edn]
    [tupelo.misc :as misc]
    [tupelo.string :as str]
    ))

; Make sure BASH, Java, Clojure, and Leiningen are installed
; ***** see sdkman.io for best way to install/use/upgrade Java JDK *****
(verify
  (is (str/contains-str-frags? (:out (misc/shell-cmd "bash --version"))
                    "GNU bash" "version"))

  (it-> (:out (misc/shell-cmd "java --version"))
    (is (or (str/contains-str-frags? it "OpenJDK Runtime Environment" "build") ; OpenJDK
            (str/contains-str-frags? it "Java HotSpot" "build")))) ; Oracle
  (is (str/contains-str-frags? (:out (misc/shell-cmd "clj --version"))
        "Clojure" "CLI" "version" "1.12"))
  (is (str/contains-str-frags? (:out (misc/shell-cmd "lein --version"))
        "Leiningen 2." "on" "Java")))

(verify
  ; `read-string` will only read ONE string. Need a loop (aka mapv) to read multiple strings
  (is= [:a 1 :b 2] (mapv edn/read-string [":a" "1" ":b" "2"]))

  ; how to convert a sequence of key-value pairs into a map
  (is= {:a 1 :b 2}
    (hash-map :a 1 :b 2) ; 4 scalar args
    (apply hash-map [:a 1 :b 2])) ; a single vector arg

  ; cannot coerce data into a map via these expressions
  (throws? (into {} :a 1 :b 2))
  (throws? (into {} [:a 1 :b 2]))
  (throws? (hash-map [:a 1 :b 2]))
  (throws? (edn/read-string [":a" "1" ":b" "2"])))

; verify ability to strip out all debut lines
(verify
  (isnt (debug-line? "dbg--a"))
  (is (debug-line? ":dbg--b"))

  (let [result (str/join
                 (stdout->nondebug-lines
                   ":dbg--aaa
                     { :a 1 :b 2
                     :dbg--bbb
                     :c 3 }
                    :dbg--zzz "))
        parsed (edn/read-string result)]
    (is= parsed {:a 1 :b 2 :c 3})))

(verify
  (is= [:a 1 :b 2] (str-args->edn-vec [":a" "1" ":b" "2"]))
  (is= [:a] (str-args->edn-vec [":a 1 :b 2"])) ; ***** only get 1st item if not EDN collection in string
  (is= [:a 1 :b 2] (xfirst (str-args->edn-vec ["[:a 1 :b 2]"]))) ; string must be EDN collection to retain all values
  (is= {:a 1 :b 2} (xfirst (str-args->edn-vec ["{:a 1 :b 2}"]))) ; string must be EDN collection to retain all values
  )

(verify ; # explicit call to `-main` entrypoint
  (newline)
  (is= {:a 1 :b 2} (cmdstr->main->edn "clj -X demo.debug/-main     :a 1 :b 2   ")) ; implicit EDN map
  (is= {:a 1 :b 2} (cmdstr->main->edn "clj -X demo.debug/-main  '{ :a 1 :b 2 }'")) ; explicit EDN map
  )

