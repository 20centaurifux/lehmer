(ns lehmer.core
  (:require [clojure.spec.alpha :as s]
            [lehmer.specs :as specs]))

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
  (mapv #(nth coll %) indeces))

(defn- nth-elements
  [n elements]
  (->> (elements->indeces elements)
       (permutate-indeces (dec n))
       (nthx elements)))

(defn nth-permutation
  "Returns n-th permutation of elements or nil if n is greater than number of
   permutations. elements must be a distinct list, vector or string."
  [n elements]
  {:pre [(s/valid? ::specs/index n)
         (s/valid? ::specs/elements elements)]
   :post [(or (nil? %)
              (and (s/valid? ::specs/elements %)
                   (= (type elements) (type %))))]}
  (when (<= n (factorial (count elements)))
    (cond->> (nth-elements n elements)
      (string? elements) (apply str)
      (list? elements) (apply list))))

(defn- permutation->indeces
  [permutation elements]
  (let [m (zipmap elements
                  (range (count elements)))]
    (mapv #(m %) permutation)))

(defn- lehmer-code
  [l [r & xs]]
  (let [n (- r (count (filter #(< % r) l)))]
    (if-not xs
      [n]
      (cons n (lehmer-code (conj l r) xs)))))

(defn permutation->lehmer-code
  "Returns lehmer code of permutation, where permutation is a rearrangement of
   elements. elements must be a distinct list, vector or string."
  [permutation elements]
  {:pre [(s/valid? ::specs/elements permutation)
         (s/valid? ::specs/elements elements)
         (= (set permutation) (set elements))]
   :post [(s/valid? ::specs/lehmer-code %)]}
  (lehmer-code [] (permutation->indeces permutation elements)))

(defn lehmer-code->base-10
  "Converts lehmer-code to base 10."
  [lehmer-code]
  {:pre [(s/valid? ::specs/lehmer-code lehmer-code)]
   :post [(s/valid? ::specs/natural-integer %)]}
  (reduce + 1 (map-indexed (fn [idx n]
                             (* (factorial (- (count lehmer-code) idx 1)) n))
                           lehmer-code)))
