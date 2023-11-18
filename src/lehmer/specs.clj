(ns lehmer.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]))

(s/def ::index pos-int?)

(s/def ::element (s/or :integer int?
                       :double double?
                       :boolean boolean?
                       :character char?))

(defonce ^:private distinct-generator-opts {:min-elements 1})

(s/def ::elements-string (s/with-gen (s/and string?
                                            not-empty
                                            #(apply distinct? %))
                           #(gen/fmap (fn [coll] (apply str coll))
                                      (gen/list-distinct gen/char-alpha distinct-generator-opts))))

(s/def ::elements-coll (s/with-gen (s/and (s/coll-of ::element
                                                     :distinct true
                                                     :kind #(or (vector? %) (list? %)))
                                          not-empty)
                         #(gen/one-of [(gen/list-distinct (s/gen :lehmer.specs/element) distinct-generator-opts)
                                       (gen/vector-distinct (s/gen :lehmer.specs/element) distinct-generator-opts)])))

(s/def ::elements (s/or :string ::elements-string
                        :coll ::elements-coll))

(s/def ::permutation ::elements)