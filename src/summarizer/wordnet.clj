(ns summarizer.wordnet
  (:import net.didion.jwnl.JWNL
           net.didion.jwnl.data.Word
           net.didion.jwnl.data.Synset
           net.didion.jwnl.data.POS
           net.didion.jwnl.dictionary.Dictionary
           java.io.FileInputStream))

;; Set wordnet to an instance of the WordNet database 
(def properties-path "resources/jwnl_properties.xml")
(def input-stream (new FileInputStream properties-path))
(. JWNL initialize input-stream)
(def wordnet (. Dictionary getInstance))

(defn get-synset
  [pos token]
  "Takes a word and its part of speech for lookup in wordnet.
   If found, returns lemma. Otherwise, returns nil" 
  (let [index-word
        (if (= pos "ADJECTIVE")
          (. wordnet getIndexWord (. POS ADJECTIVE) token)
          (if (= pos "ADVERB")
            (. wordnet getIndexWord (. POS ADVERB) token)
            (if (= pos "NOUN")
              (. wordnet getIndexWord (. POS NOUN) token)
              (if (= pos "VERB")
                (. wordnet getIndexWord (. POS VERB) token)))))]
    (if index-word
      (let [synset (. index-word getSense 1)]
        (if synset
          (. (. synset getWord 0) getLemma))))))
