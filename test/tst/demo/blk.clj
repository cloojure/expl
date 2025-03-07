(ns tst.demo.blk
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [schema.core :as s]
    [tupelo.profile :as prof]
    [tupelo.schema :as tsk]
    ))

(s/defn vadd2 :- [s/Num]
  [as :- [s/Num]
   bs :- [s/Num]]
  (mapv + as bs))

; how to wrap a function so it prints execution time
(s/defn vadd2-time
  [& args]
  (prof/with-timer-print :vadd2-time
    (apply vadd2 args)))

(s/defn vadd2-blk-expl :- [s/Num]
  "An explicit block version of vadd2"
  [N :- s/Int
   as :- [s/Num]
   bs :- [s/Num]]
  (prof/with-timer-print :vadd2-blk-expl
    (let [ablks  (partition-all N as)
          bblks  (partition-all N bs)
          rblks  (mapv vadd2 ablks bblks)
          result (apply glue rblks)]
      result)))

(verify
  (let [data-1    (thru 0 9)
        data-2    (thru 1 10)

        r1        (vadd2-time data-1 data-2)
        r2        (vadd2-blk-expl 3 data-1 data-2)
        >>        (newline)

        ; convert any function into a block function
        vadd2-blk (block-fn 3 vadd2-time
                            {:enter (fn [ctx] (prn :enter (:index ctx)))})
        r3-blk    (vadd2-blk data-1 data-2)
        r3        (data-2d->1d r3-blk)]
    (is= data-1 [0 1 2 3 4 5 6 7 8 9])
    (is= data-2 [1 2 3 4 5 6 7 8 9 10])

    (is= r3-blk [[1 3 5]
                 [7 9 11]
                 [13 15 17]
                 [19]])
    (is= [1 3 5 7 9 11 13 15 17 19]
      r1 r2 r3)
    ))

