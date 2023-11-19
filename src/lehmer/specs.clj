(ns lehmer.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]))

(s/def ::index pos-int?)

(s/def ::element (s/or :integer int?
                       :double double?
                       :boolean boolean?
                       :character char?))

(def ^:private distinct-gen-opts {:min-elements 1})

(s/def ::elements-string (s/with-gen (s/and string?
                                            not-empty
                                            #(apply distinct? %))
                           #(gen/fmap (partial apply str)
                                      (gen/list-distinct gen/char-alpha distinct-gen-opts))))

(def ^:private element-generator (s/gen :lehmer.specs/element))

(s/def ::elements-coll (s/with-gen (s/and (s/coll-of ::element
                                                     :distinct true
                                                     :kind #(or (vector? %) (list? %)))
                                          not-empty)
                         #(gen/one-of [(gen/list-distinct element-generator distinct-gen-opts)
                                       (gen/vector-distinct element-generator distinct-gen-opts)])))

(s/def ::elements (s/or :string ::elements-string
                        :coll ::elements-coll))

(s/def ::lehmer-code (s/coll-of nat-int?))

(s/def ::natural-integer (s/and integer? #(>= % 0))) ; clojure.core/nat-int? returns false for bigints