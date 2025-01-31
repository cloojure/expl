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

(s/defn vadd2-blk-expl :- [s/Num]
  "An explicit block version of vadd2"
  [N :- s/Int
   as :- [s/Num]
   bs :- [s/Num]]
  (let [ablks  (partition-all N as)
        bblks  (partition-all N bs)
        rblks  (mapv vadd2 ablks bblks)
        result (apply glue rblks)]
    result))

(verify
  (let [as         (thru 0 9)
        bs         (thru 1 10)

        r1         (vadd2 as bs)
        r2         (vadd2-blk-expl 7 as bs)

        ; how to wrap a function so it prints execution time
        vadd2-time (fn [& args]
                     (prof/with-timer-print :vadd2-time
                       (apply vadd2 args)))

        ; convert any function into a block function
        vadd2-blk  (block-fn 3 vadd2-time
                             {:enter (fn [ctx] (prn :enter (:index ctx)))})
        rs-blk     (vadd2-blk as bs)
        r-blk      (apply glue rs-blk)
        ]
    (is= as [0 1 2 3 4 5 6 7 8 9])
    (is= bs [1 2 3 4 5 6 7 8 9 10])

    (is= rs-blk [[1 3 5]
                 [7 9 11]
                 [13 15 17]
                 [19]])
    (is= [1 3 5 7 9 11 13 15 17 19]
      r1 r2 r-blk)
    ))

