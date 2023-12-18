(ns lehmer.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :refer [check-fn]]
            [clojure.core.match :refer [match]]
            [lehmer.core :as lehmer]
            [lehmer.specs :as specs]))

(alias 'stc 'clojure.spec.test.check)

;;; generative test runner & specs
(def ^:private num-tests 1000)

(defn- run-generative-tests
  ([f spec]
   (let [results (check-fn
                  (fn [& args]
                    (try
                      (apply f args)
                      (catch Throwable e e)))
                  spec
                  {::stc/opts {:num-tests num-tests}})]
     (is (get-in results [:clojure.spec.test.check/ret :pass?])))))

(s/def ::assertion-error #(instance? AssertionError %))

(defmacro ^:private defcomplement-spec
  [name spec]
  `(s/def ~name
     (s/with-gen
       #(s/invalid? (s/conform ~spec %))
       #(gen/such-that (partial s/valid? ~name) gen/any))))

(defcomplement-spec ::invalid-elements ::specs/elements)

;;; nth-permutation
(def ^:private factorial #'lehmer.core/factorial)

(defcomplement-spec ::invalid-index ::specs/index)

(deftest nth-permutation
  (testing "valid arguments"
    (run-generative-tests
     lehmer/nth-permutation
     (s/fspec :args (s/cat :n ::specs/index
                           :elements ::specs/elements)
              :ret (s/or :nil nil?
                         :permutation ::specs/elements)
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
                           :elements ::specs/elements)
              :ret ::assertion-error)))

  (testing "invalid elements argument"
    (run-generative-tests
     lehmer/nth-permutation
     (s/fspec :args (s/cat :n ::specs/index
                           :elements ::invalid-elements)
              :ret ::assertion-error))))

;;; permutation->lehmer-code
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
              :ret ::specs/lehmer-code)))

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
                           :elements ::specs/elements)
              :ret ::assertion-error))))

;;; lehmer-code->base-10
(defcomplement-spec ::invalid-lehmer-code ::specs/lehmer-code)

(deftest lehmer-code->base-10
  (testing "valid argument"
    (run-generative-tests
     lehmer/lehmer-code->base-10
     (s/fspec :args (s/cat :lehmer-code ::specs/lehmer-code)
              :ret ::specs/natural-integer)))

  (testing "invalid argument"
    (run-generative-tests
     lehmer/lehmer-code->base-10
     (s/fspec :args (s/cat :lehmer-code ::invalid-lehmer-code)
              :ret ::assertion-error))))

;;; permutation to lehmer code to base 10
(s/def ::valid-permutation-index (s/int-in 1 sample-elements-count))

(defn- nth-permutation->lehmer-code->base10
  [index elements]
  (-> (lehmer/nth-permutation index elements)
      (lehmer/permutation->lehmer-code elements)
      lehmer/lehmer-code->base-10))

(deftest all
  (testing "nth-permutation->lehmer-code->base10"
    (run-generative-tests
     nth-permutation->lehmer-code->base10
     (s/fspec :args (s/cat :index ::valid-permutation-index :elements ::valid-permutation)
              :ret ::specs/natural-integer
              :fn #(match [%]
                     [{:args {:index index :elements _}
                       :ret n}] (= index n))))))
