(ns tst.demo.blk
  (:use demo.util
        tupelo.core
        tupelo.test)
  (:require
    [schema.core :as s]
    [tupelo.profile :as prof]
    [tupelo.schema :as tsk]
    ))

(verify
  (let
    [vecsum (fn [v] (apply + v))
     vb2    (blockify-fn 3 vecsum)

     v1     (thru 1 10)
     v1s    (vecsum v1)

     vb2rs  (vb2 v1)
     vb2r   (apply + vb2rs)

     b1     (data-blockify 3 v1)
     b1s1   (forv [b b1]
              (vecsum b))

     b1f1   (vecsum b1s1)
     ]
    (is= v1s 55)
    (is= vb2rs [6 15 24 10])
    (is= vb2r 55)

    (is= b1 [[1 2 3]
             [4 5 6]
             [7 8 9]
             [10]])
    (is= b1s1 [6 15 24 10])
    (is= b1f1 55)
    ))

;---------------------------------------------------------------------------------------------------
(s/defn vadd2 :- [s/Num]
  [as :- [s/Num]
   bs :- [s/Num]]
  (mapv + as bs))

(s/defn vadd2-blk-explicit :- [s/Num]
  [N :- s/Int
   as :- [s/Num]
   bs :- [s/Num]]
  (let [ablks  (partition-all N as)
        bblks  (partition-all N bs)
        rblks  (mapv vadd2 ablks bblks)
        result (apply glue rblks)]
    result))

(verify
  (let [as               (thru 0 9)
        bs               (thru 1 10)

        r1               (vadd2 as bs)
        r2               (vadd2-blk-explicit 7 as bs)

        vadd2-time       (fn [& args]
                           (prof/with-timer-print :vadd2-time
                             (apply vadd2 args)))

        vadd2-blockified (blockify-fn 3 vadd2-time {:enter (fn [ctx]
                                                             (prn :enter (:index ctx)))})
        rs-blk           (vadd2-blockified as bs)
        r-blk            (apply glue rs-blk)]

    (is= as [0 1 2 3 4 5 6 7 8 9])
    (is= bs [1 2 3 4 5 6 7 8 9 10])

    (is= rs-blk [[1 3 5]
                 [7 9 11]
                 [13 15 17]
                 [19]])
    (is= [1 3 5 7 9 11 13 15 17 19]
      r1 r2 r-blk)

    ))

