# Summarizer

Extractive graph-based text summarize.

## Installation

![Latest version](https://clojars.org/summarizer/latest-version.svg)

## Usage

```clojure
(require '[summarizer.core :refer [summarize]])

;; the text is reduced to 20% of its original length.
(summarize "some-file.txt")

;; or you may want to set summarization ratio to 25%
(summarize "some-file.txt" 0.25)
```
