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

(def pos-map {"J" (. POS ADJECTIVE) 
              "N" (. POS NOUN)
              "R" (. POS ADVERB)
              "V" (. POS VERB)})

(defn get-synset
  [pos token]
  "Takes a word and its part of speech for lookup in wordnet.
   If found, returns lemma. Otherwise, returns nil" 
  (if-let [index-word (. wordnet getIndexWord pos token)]
    (if-let [synset (. index-word getSense 1)]
      (. (. synset getWord 0) getLemma))))

(defn get-lemma
  "Returns lemmatization from wordnet. If not
   found, returns token."
  [word]
  (let [token (key word)
        pos (.substring (val word) 0 1)]
    (if-let [pos-jwnl (get pos-map pos)]
      (if-let [synset (get-synset pos-jwnl token)]
        synset
        token)
      token)))
