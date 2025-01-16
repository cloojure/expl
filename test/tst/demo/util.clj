(ns tst.demo.util
  (:use demo.util
        tupelo.core
        tupelo.test)
  (:require
    [tupelo.profile :as prof]
    [schema.core :as s]
    ))

(def verbose? true)

(verify
  (let [seq1            (range 5)
        arr1            (data-blockify 2 seq1)
        seq2            (data-ublockify arr1)

        inc-1d          (fn-blockify inc)
        inc-2d          (fn-blockify inc-1d)

        inc-1d-lazy     (fn-blockify-lazy inc)
        inc-2d-lazy     (fn-blockify-lazy inc-1d-lazy)

        seq1-inc-a      (inc-1d seq1)
        arr1-inc        (inc-2d arr1)
        seq1-inc-b      (data-ublockify arr1-inc)

        seq1-inc-a-lazy (inc-1d-lazy seq1)
        arr1-inc-lazy   (inc-2d-lazy arr1)
        seq1-inc-b-lazy (data-unblockify-lazy arr1-inc)
        ]
    (is= seq1 [0 1 2 3 4])
    (is= arr1 [[0 1]
               [2 3]
               [4]])
    (is= seq1 seq2)

    (is= seq1-inc-a [1 2 3 4 5])
    (is= arr1-inc [[1 2]
                   [3 4]
                   [5]])
    (is= seq1-inc-b [1 2 3 4 5])

    (is= seq1-inc-a-lazy [1 2 3 4 5])
    (is= arr1-inc-lazy [[1 2]
                        [3 4]
                        [5]])
    (is= seq1-inc-b-lazy [1 2 3 4 5])
    )

  (when false
    (let-spy
      [d1 (thru 0 4)
       d2 (thru 1 5)

       s1 (mapv + d1 d2)
       I1 (mapv inc d1)
       I2 (mapv inc d2)
       ]
      (is= d1 [0 1 2 3 4])
      (is= d2 [1 2 3 4 5])
      (is= s1 [1 3 5 7 9])

      (is= I1 [1 2 3 4 5])
      (is= I2 [2 3 4 5 6]))))

(verify
  (let-spy
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


(verify
  ; ***** Need to disable Plumatic Schema validation of arguments or destroys laziness!!! *****
  (s/without-fn-validation

    ; Avoids the concat StackOverflow bug:  https://stuartsierra.com/2015/04/26/clojure-donts-concat
    ; Normal stackoverflow at about 4000 frames on common JVMs
    ; 2024-11-18 will overflow for N=39999
    (let [N                     9999 ; Note:  Use 9999 for real stress test
          num-triangle-elements (it-> N
                                      (* it (inc it))
                                      (/ it 2))
          first-37              [1 1 2 1 2 3 1 2 3 4 1 2 3 4 5 1 2 3 4 5 6 1 2 3 4 5 6 7 1 2 3 4 5 6 7 8 1]

          lazy-fn               (fn lazy-fn []
                                  (let [triangle-2d (map #(range 1 (inc %)) (range 1 (inc N)))
                                        triangle-1d (data-unblockify-lazy triangle-2d)]
                                    (is= first-37 (take 37 triangle-1d))
                                    (is= num-triangle-elements (count triangle-1d))
                                    ))

          eager-fn              (fn eager-fn []
                                  (let [triangle-2d (mapv #(thru 1 %) (thru 1 N))
                                        triangle-1d (data-ublockify triangle-2d)]
                                    (is= first-37 (take 37 triangle-1d))
                                    (is= num-triangle-elements (count triangle-1d))
                                    ))]

      ; Results:  N=9999  (2024-11-18 Mac Studio)
      ;   :with-timer-print :lazy   0.000087
      ;   :with-timer-print :eager  1.873043
      (if verbose?
        (do
          (nl)
          (prn :-----------------------------------------------------------------------------)
          (prn :v1)
          (prof/with-timer-print :lazy (lazy-fn))
          (prof/with-timer-print :eager (eager-fn))
          )
        (do
          (lazy-fn))))))

(verify
  ; ***** Need to disable Plumatic Schema validation of arguments or destroys laziness!!! *****
  (s/without-fn-validation

    (let [N        1e7
          ncols    (long (Math/round (Math/sqrt N)))
          vals     (range N)

          lazy-fn  (fn lazy-fn []
                     (let [data-2d (data-blockify-lazy ncols vals)
                           data-1d (data-unblockify-lazy data-2d)]
                       (is= (range 37) (take 37 data-1d))))

          #_(comment
              lazy-fn-gen (fn lazy-fn []
                            (let [data-2d (data-blockify-lazy ncols vals)
                                  data-1d (array-2d->1d-lazy-gen data-2d)]
                              (is= (range 37) (take 37 data-1d)))))

          eager-fn (fn eager-fn []
                     (let [data-2d (data-blockify ncols vals)
                           data-1d (data-ublockify data-2d)]
                       (is= (range 37) (take 37 data-1d))))
          ]

      ; Results:  N=1e7  (2024-11-18 Mac Studio)
      ;   :with-timer-print :lazy-gen     0.000634
      ;   :with-timer-print :lazy-concat  0.000882
      ;   :with-timer-print :eager        1.826692
      (if verbose?
        (do
          (nl)
          (prn :-----------------------------------------------------------------------------)
          (prn :v2)
          (prof/with-timer-print :lazy (lazy-fn))
          ; (prof/with-timer-print :lazy-gen (lazy-fn-gen))
          (prof/with-timer-print :eager (eager-fn))
          )
        (do
          (lazy-fn)))))
  )

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

(s/defn blkify :- tsk/Fn
  [N :- s/Int
   f :- tsk/Fn]
  (let [fout (fn [& args]
               (let [args-blks (mapv #(partition-all N %) args)
                     rblks     (apply mapv f args-blks)
                     result    (apply glue rblks)]
                 )
               )])
  )

(verify-focus
  (let-spy
    [as   (thru 0 9)
     bs   (thru 1 10)
     r1   (vadd2 as bs)
     r2   (vadd2-blk 3 as bs)
     fblk (blkify 3 vadd2)
     ; rblk (fblk as bs)
     ]
    )
  )



