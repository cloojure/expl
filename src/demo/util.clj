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

(s/defn fn-blockify :- tsk/Fn
  "Vectorize a function, so that instead of operating on a scalar value,
  it operates on each value in a 1D array. Used twice, the resulting function operates
  on each value in a 2D array. Not lazy."
  [f :- tsk/Fn]
  (s/fn [block :- [s/Any]]
    (mapv f block)))

(s/defn fn-blockify-lazy :- tsk/Fn
  "Vectorize a function, so that instead of operating on a scalar value,
  it operates on each value in a 1D array. Used twice, the resulting function operates
  on each value in a 2D array. Lazy."
  [f :- tsk/Fn]
  (s/fn [block :- [s/Any]]
    (map f block)))

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

