(ns lehmer.core
  (:require [clojure.spec.alpha :as s]))

(def ^:private factorial (memoize #(reduce * % (range 1N %))))

(defn- elements->indeces
  [elements]
  (into [] (range (count elements))))

(defn- permutate-indeces
  [n v]
  (if (= (count v) 1)
    v
    (let [divisor (factorial (dec (count v)))
          [l [r & xs]] (split-at (quot n divisor) v)]
      (when r
        (cons r
              (permutate-indeces (rem n divisor)
                                 (concat l xs)))))))

(defn- nthx
  [coll indeces]
  (map #(nth coll %) indeces))

(defn- nth-collection
  [n elements]
  (->> (elements->indeces elements)
       (permutate-indeces (dec n))
       (nthx elements)
       (into (empty elements))))

(defn nth-permutation
  "Returns n-th permutation of elements or nil if n is greater than number of
   permutations. elements must be a distinct list, vector or string."
  [n elements]
  {:pre [(s/valid? :lehmer.specs/index n)
         (s/valid? :lehmer.specs/elements elements)]
   :post [#(s/valid? :lehmer.specs/permutation %)
          #(= (type %) (type elements))]}
  (when (<= n (factorial (count elements)))
    (cond->> (nth-collection n elements)
      (string? elements) (apply str))))