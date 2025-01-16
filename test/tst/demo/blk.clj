(ns tst.demo.blk
  (:use demo.util
        tupelo.core
        tupelo.test)
  (:require
    [schema.core :as s]
    [tupelo.misc :as misc]
    [tupelo.profile :as prof]
    [tupelo.schema :as tsk]
    ))

(verify
  (let
    [vecsum     (fn [v] (apply + v))
     vecsum-blk (fn-blockify vecsum)

     v1         (thru 1 10)
     v1s        (vecsum v1)

     b1         (data-blockify 3 v1)
     b1s1       (forv [b b1]
                  (vecsum b))

     b1s2       (vecsum-blk b1)
     b1f1       (vecsum b1s1)
     b1f2       (vecsum b1s2)
     ]
    (is= v1s 55)

    (is= b1 [[1 2 3]
             [4 5 6]
             [7 8 9]
             [10]])
    (is= b1s1 [6 15 24 10])
    (is= b1s2 [6 15 24 10])
    (is= b1f1 55)
    (is= b1f2 55)))



(s/defn vadd2 :- [s/Num]
  [as :- [s/Num]
   bs :- [s/Num]]
  (mapv + as bs))

(s/defn vadd2-blk :- [s/Num]
  [N :- s/Int
   as :- [s/Num]
   bs :- [s/Num]]
  (let [ablks  (partition-all N as)
        bblks  (partition-all N bs)
        rblks  (mapv vadd2 ablks bblks)
        result (apply glue rblks)]
    result))

(s/defn blockify :- tsk/Fn
  ([N :- s/Num
    f :- tsk/Fn]
   (blockify N f {:enter noop :leave noop}))
  ([N :- s/Num
    f :- tsk/Fn
    intc :- {:enter tsk/Fn
             :leave tsk/Fn}]
   (with-map-vals intc [enter leave]
     (s/fn fn-blockified :- [s/Any]
       [& args :- [[s/Any]]]
       (let [args-blks        (for [arg args]
                                (partition-all N arg))
             blk-args         (apply mapv vector args-blks)
             blk-args-indexed (zip* {:strict false} (range) blk-args)

             rblks            (forv [[idx blks] blk-args-indexed]
                                (enter {:index idx})
                                (with-result (apply f blks)
                                  (leave {:index idx})))
             ]
         rblks)))))

(verify-focus
  (let [as         (thru 0 9)
        bs         (thru 1 10)
        r1         (vadd2 as bs)
        r2         (vadd2-blk 3 as bs)

        vadd2-time (fn [& args]
                     (prof/with-timer-print :vadd2-time
                       (apply vadd2 args)))

        vadd2-blk  (blockify 3 vadd2-time {:enter (fn [ctx] (prn :enter (:index ctx)))
                                           :leave noop ; (fn [ctx] (prn :leave (:index ctx)))
                                           })
        rs-blk     (vadd2-blk as bs)
        r-blk      (apply glue rs-blk)]

    (is= as [0 1 2 3 4 5 6 7 8 9])
    (is= bs [1 2 3 4 5 6 7 8 9 10])

    (is= rs-blk [[1 3 5]
                 [7 9 11]
                 [13 15 17]
                 [19]])
    (is= [1 3 5 7 9 11 13 15 17 19]
      r1 r2 r-blk)

    ))

