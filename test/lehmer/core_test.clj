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
   (run-generative-tests
    f
    spec
    {::stc/opts {:num-tests 1000}}))
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

(s/def ::valid-elements
  (s/with-gen
    :lehmer.specs/elements
    #(gen/list-distinct (s/gen :lehmer.specs/element))))

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

  (testing "invalid index argument"
    (run-generative-tests
     lehmer/nth-permutation
     (s/fspec :args (s/cat :n ::valid-index
                           :elements ::invalid-elements)
              :ret ::assertion-error))))