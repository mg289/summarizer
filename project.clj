(defproject summarizer "1.0.0-SNAPSHOT"
  :description "Text summarizer"
  :main summarizer.main 
  :dependencies [[org.clojure/clojure "1.3.0"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [org.clojure/tools.cli "0.2.1"]
		 [commons-logging/commons-logging "1.1.1"]
                 [net.sf.jwordnet/jwnl "1.4_rc3"]
                 [org.apache.opennlp/opennlp-tools "1.5.2-incubating"]
                 [org.jgrapht/jgrapht-jdk1.5 "0.7.3"]])
