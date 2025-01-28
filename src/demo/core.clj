(ns demo.core
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.core :as t]
    [tupelo.schema :as tsk]
    ))

;---------------------------------------------------------------------------------------------------
(def Interceptor {(s/optional-key :enter) tsk/Fn
                  (s/optional-key :leave) tsk/Fn})

(def interceptor-default {:enter noop
                          :leave noop})

(s/defn block-fn-lazy :- tsk/Fn
  "Same as `block-fn`, but returns a lazy sequence."
  ([N :- s/Num
    f :- tsk/Fn]
   (block-fn-lazy N f interceptor-default))
  ([N :- s/Num
    f :- tsk/Fn
    intc :- Interceptor]
   (let [intc-use (glue interceptor-default intc)]
     (with-map-vals intc-use [enter leave]
       (s/fn fn-blockified :- [s/Any]
         [& data-vecs :- [[s/Any]]]
         (let [vecs-blocked      (for [arg data-vecs]
                                   (partition-all N arg))
               blkd-vecs         (apply map vector vecs-blocked)
               blkd-vecs-indexed (zip* {:strict false} (range) blkd-vecs)

               result-blks       (for [[idx blks] blkd-vecs-indexed]
                                   (do (enter {:index idx})
                                       (with-result (apply f blks)
                                         (leave {:index idx}))))]
           result-blks))))))

(s/defn block-fn :- tsk/Fn
  "Accepts an integer N and a function like `(f as bs ...) => rs` where the `as`, `bs`, etc are
  input sequences (assumed identical length L), and the `rs` is an output sequence
  of any length. Returns a function that partitions the inputs into blocks of
  size N, and calls `f` repeatedly like `(f a1 b1 ...) => r1`, returning a vector like [r1 r2 ....].
  Optionally accepts an interceptor as the last arg, with `:enter` & `:leave` functions like

        (fn [ctx] ...)   ; ctx is a map like `{:index idx}`

   Final result is a non-lazy vector."
  ([N :- s/Num
    f :- tsk/Fn]
   (block-fn N f interceptor-default))
  ([N :- s/Num
    f :- tsk/Fn
    intc :- Interceptor]
   (unlazy (block-fn-lazy N f intc))))

;-----------------------------------------------------------------------------
(s/defn data-1d->2d-lazy :- [[s/Any]]
  "Convert a 1D sequence to a lazy 2D array (possibly ragged) in row-major order."
  [N :- s/Int
   data-1d :- [s/Any]]
  (partition-all N data-1d))

(s/defn data-1d->2d :- [[s/Any]]
  "Convert a 1D sequence to a 2D array (possibly ragged) in row-major order. Not lazy."
  [N :- s/Int
   data-1d :- [s/Any]]
  (unlazy (data-1d->2d-lazy N data-1d)))

;-----------------------------------------------------------------------------
(s/defn data-2d->1d-lazy :- [s/Any]
  "Concatenate rows of a 2D array (possibly ragged), returning a 1-D vector."
  [data-2d :- [[s/Any]]]
  (apply concat data-2d))

(s/defn data-2d->1d :- [s/Any]
  "Concatenate rows of a 2D array (possibly ragged), returning a 1-D vector."
  [data-2d :- [[s/Any]]]
  (unlazy (data-2d->1d-lazy data-2d)))

; #awt 2024-11-19 works, but no advantage to `(apply concat seq-2d)`
(comment
  (s/defn data-2d->1d-lazy-gen :- [s/Any]
    "Concatenate rows of a 2D array (possibly ragged), returning a 1-D vector."
    [seq-2d :- [[s/Any]]]
    (lazy-gen
      (doseq [row seq-2d]
        (doseq [item row]
          (yield item))))))

