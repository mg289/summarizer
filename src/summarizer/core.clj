(ns summarizer.core
  (:use [summarizer.preprocess :only [sentence-split preprocess]]
        [summarizer.rank :only [rank]]))

(defn summarize
  [file & [length-factor]]
  "Takes a filename and a length-factor, reads its contents and returns a string of
   top ranked sentences in their original order."
  (let [text (slurp file)
        sentences (sentence-split text)
        preprocessed (preprocess sentences)
        ranks (rank preprocessed)
        summ-length (* (count sentences) (or length-factor 0.2))
        ;; Sort first summ-length sentences and get indices
        chosen-indices (keys (sort (take summ-length ranks)))]
    ;; Get sentences at chosen indices, separate with spaces and make string
    (->> chosen-indices
         (map #(sentences %))
         (interpose " ")
         (apply str))))
