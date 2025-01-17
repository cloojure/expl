(ns demo.util
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.core :as t]
    [tupelo.schema :as tsk]
    [tupelo.string :as str]
    )
  (:import
    [java.time LocalDate]
    ))

(def Interceptor {(s/optional-key :enter) tsk/Fn
                  (s/optional-key :leave) tsk/Fn})

(def interceptor-default {:enter noop
                          :leave noop})

(s/defn blockify-fn :- tsk/Fn
  "Accepts an integer N and a function like `(f as bs ...) => rs` where the `as`, `bs`, etc are
  input sequences (assumed identical length L), and the `rs` is an output sequence
  of any length. Returns a function that partitions the inputs into blocks of
  size N, and calls `f` repeatedly like `(f a1 b1 ...) => r1`, returning a vector like [r1 r2 ....].
  Optionally accepts an interceptor as the last arg, with :enter & :leave functions like

        (fn [ctx] ...)   ; ctx is a map like `{:index idx}`
   "
  ([N :- s/Num
    f :- tsk/Fn]
   (blockify-fn N f interceptor-default))
  ([N :- s/Num
    f :- tsk/Fn
    intc :- Interceptor]
   (let [intc-use (glue interceptor-default intc)]
     (with-map-vals intc-use [enter leave]
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
           rblks))))))

(s/defn data-blockify-lazy :- [[s/Any]]
  "Convert a 1D sequence to a lazy 2D array (possibly ragged) in row-major order."
  [N :- s/Int
   data :- [s/Any]]
  (partition-all N data))

(s/defn data-blockify :- [[s/Any]]
  "Convert a 1D sequence to a 2D array (possibly ragged) in row-major order. Not lazy."
  [N :- s/Int
   data :- [s/Any]]
  (unlazy (data-blockify-lazy N data)))

(s/defn data-ublockify :- [s/Any]
  "Concatenate rows of a 2D array (possibly ragged), returning a 1-D vector."
  [blocks :- [[s/Any]]]
  (apply glue blocks))

(s/defn data-unblockify-lazy :- [s/Any]
  "Concatenate rows of a 2D array (possibly ragged), returning a 1-D vector."
  [seq-2d :- [[s/Any]]]
  (apply concat seq-2d))

; #awt 2024-11-19 works, but no advantage to `(apply concat seq-2d)`
(comment
  (s/defn array-2d->1d-lazy-gen :- [s/Any]
    "Concatenate rows of a 2D array (possibly ragged), returning a 1-D vector."
    [seq-2d :- [[s/Any]]]
    (lazy-gen
      (doseq [row seq-2d]
        (doseq [item row]
          (yield item))))))

