# lehmer

*lehmer* is Lehmer code implementation written in Clojure. You can use it to encode each possible permutation of a sequence of *n* numbers.

## Installation

The library can be installed from Clojars using Leiningen:

[![Clojars Project](https://img.shields.io/clojars/v/de.dixieflatline/lehmer.svg?include_prereleases)](https://clojars.org/de.dixieflatline/lehmer)

## Quick overview

```
(require '[lehmer.core :as lehmer])

;;; Use nth-permutation to compute permutations.
;;; lehmer supports distinct lists, vectors and strings.
(lehmer/nth-permutation 5 "abc")
=> "cab"

(lehmer/nth-permutation 5 [\a \b \c])
=> [\c \a \b]

;;; Provide a permutation and an ordered sequence of available
;;; elements to compute the Lehmer code.
(lehmer/permutation->lehmer-code "cab" "abc") 
=> (2 0 0)

;;; A Lehmer code can easily be converted to base 10.
(lehmer/lehmer-code->base-10 [2 0 0])
=> 5
```
