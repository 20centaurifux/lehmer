(ns lehmer.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::index pos-int?)

(s/def ::element (s/or :integer int?
                       :double double?
                       :boolean boolean?
                       :character char?))

(s/def ::elements-string (s/and string?
                                not-empty
                                #(apply distinct? %)))

(s/def ::elements-coll (and (s/or :list? list?
                                  :vector vector?)
                            not-empty
                            (s/coll-of ::element :distinct true)))

(s/def ::elements (s/or :string ::elements-string
                        :coll ::elements-coll))

(s/def ::permutation ::elements-coll)