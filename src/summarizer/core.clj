(ns summarizer.core
  (:use [summarizer.preprocess :only [sentence-split preprocess]]
	[summarizer.rank :only [rank]]))

(defn summarize
  [file]
  "Takes a filename. Reads its contents and writes to summary.txt a string of
   top ranked sentences in their original order"
  (println "Starting Summary...")
  (let [text (slurp file)
	sentences (sentence-split text)
        preprocessed (preprocess sentences)
        ranks (rank preprocessed)
	;; Reduce the length to 1/4 of original
        summ-length (/ (count sentences) 4)
	;; Sort first summ-length sentences and get indices
        chosen-indices (keys (sort (take summ-length ranks)))
	;; Get sentences at chosen indices, separate with spaces and make string
	summary (apply str (interpose " " (map #(sentences %) chosen-indices)))]
    (spit "summary.txt" summary)
    (print "Summary Complete: You can find your summary in summary.txt\n")))

