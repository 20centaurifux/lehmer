(ns lehmer.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :refer [check-fn]]
            [clojure.core.match :refer [match]]
            [lehmer.core :as lehmer])
  (:use [lehmer.specs]))

(alias 'stc 'clojure.spec.test.check)

(defonce factorial #'lehmer.core/factorial)

(s/def ::assertion-error #(instance? AssertionError %))

(defn- run-generative-tests
  ([f spec]
   (run-generative-tests f spec {::stc/opts {:num-tests 1000}}))
  ([f spec opts]
   (let [results (check-fn
                  (fn [& args]
                    (try
                      (apply f args)
                      (catch Throwable e e)))
                  spec
                  opts)]
     (is (get-in results [:clojure.spec.test.check/ret :pass?])))))

(s/def ::valid-index :lehmer.specs/index)

(s/def ::valid-elements :lehmer.specs/elements)

(s/def ::invalid-index
  (s/with-gen
    (complement #(s/valid? :lehmer.specs/index %))
    #(gen/such-that (partial s/valid? ::invalid-index) gen/any)))

(s/def ::invalid-elements
  (s/with-gen
    (complement #(s/valid? :lehmer.specs/elements %))
    #(gen/such-that (partial s/valid? ::invalid-elements) gen/any)))

(deftest nth-permutation
  (testing "valid arguments"
    (run-generative-tests
     lehmer/nth-permutation
     (s/fspec :args (s/cat :n ::valid-index
                           :elements ::valid-elements)
              :ret (s/or :nil nil?
                         :permutation :lehmer.specs/permutation)
              :fn #(match [%]
                     [{:args {:n n :elements [_ elements]}
                       :ret [:nil _]}] (or (<= n 0)
                                           (> n (factorial (count elements))))
                     [{:args {:n n :elements [_ elements]}
                       :ret [:permutation _]}] (<= n (factorial (count elements)))))))

  (testing "invalid index argument"
    (run-generative-tests
     lehmer/nth-permutation
     (s/fspec :args (s/cat :n ::invalid-index
                           :elements ::valid-elements)
              :ret ::assertion-error)))

  (testing "invalid elements argument"
    (run-generative-tests
     lehmer/nth-permutation
     (s/fspec :args (s/cat :n ::valid-index
                           :elements ::invalid-elements)
              :ret ::assertion-error))))

(def ^:private sample-elements-count 20)

(s/def ::valid-permutation
  (s/with-gen
    #(= (set (range sample-elements-count)) (set %))
    #(gen/shuffle (range sample-elements-count))))

(deftest permutation->lehmer-code
  (testing "valid arguments"
    (run-generative-tests
     lehmer/permutation->lehmer-code
     (s/fspec :args (s/cat :permutation ::valid-permutation
                           :elements ::valid-permutation)
              :ret :lehmer.specs/lehmer-code)))

  (testing "invalid permutation argument"
    (run-generative-tests
     lehmer/permutation->lehmer-code
     (s/fspec :args (s/cat :permutation ::invalid-elements
                           :elements ::valid-permutation)
              :ret ::assertion-error)))

  (testing "invalid elements argument"
    (run-generative-tests
     lehmer/permutation->lehmer-code
     (s/fspec :args (s/cat :permutation ::valid-permutation
                           :elements ::invalid-elements)
              :ret ::assertion-error)))

  (testing "permutation isn't rearrangement of elements"
    (run-generative-tests
     lehmer/permutation->lehmer-code
     (s/fspec :args (s/cat :permutation ::valid-permutation
                           :elements ::valid-elements)
              :ret ::assertion-error))))