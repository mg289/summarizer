(ns summarizer.main
  (:use [summarizer.core :only [summarize]])
  (:import java.io.File)
  (:gen-class))

(declare usage file-exists?)

(defn -main
  "Main entry point"
  [& args]
  (if-let [filename (first args)]  
    (if (file-exists? filename)
      (summarize filename)
      (binding [*out* *err*]
        (println (str "File not found: " filename))))
    (usage)))

(defn usage
  []
  (println "Usage: lein run <filename>"))

(defn file-exists?
  [filename]
  (. (new File filename) exists))
