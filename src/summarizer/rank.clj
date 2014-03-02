(ns summarizer.rank
  (:import org.jgrapht.graph.SimpleWeightedGraph
           org.jgrapht.graph.DefaultWeightedEdge
           org.jgrapht.Graphs
           java.lang.Math))

;; Graph: SimpleWeightedGraph provided by JGraphT (therefore will be mutated)
;; Vertices: Map of id (num), sentence pairs
;;   Sentence: Vector of sets of words
;; Edges: DefaultWeightedEdge provided by JGraphT

(def damping-factor 0.85)
(def convergence-threshold 0.005)
(def max-iterations 10)

(defn calc-overlap
  "Returns number of content words (or synonyms) in
   both sentenceA and sentenceB"
  [sentenceA sentenceB]
  (if (seq sentenceA)
    (let [fstA (first sentenceA)
          rstA (rest sentenceA)]
      (if (contains? sentenceB fstA)
        (inc (calc-overlap rstA sentenceB))
        (calc-overlap rstA sentenceB)))
    0))

(defn calc-weight
  "Calculates similarity between sentences
   in vertexA and vertexB"
  [vertexA vertexB]
  (let [sentenceA (val vertexA)
        sentenceB (val vertexB)
        overlap (calc-overlap sentenceA sentenceB)
        log-length-A (Math/log (count sentenceA))
        log-length-B (Math/log (count sentenceB))
        log-sum (+ log-length-A log-length-B)]
    (if (> log-sum 0)
      (/ overlap log-sum)
      0)))

(defn update-totals
  "Updates scores based on constant weight-totals
   and the score updated in previous iteration"
  [func graph weight-totals scores]
  (let [vertices (. graph vertexSet)]
    (loop [update-map {}
           v vertices]
      (if (seq v)
        (let [fst (first v)]
          (recur
            (assoc update-map (key fst)
                  (func graph fst weight-totals scores))
           (rest v)))
        update-map))))

(defn add-to-summation
  "Adds element to summation for V_j = vertex
   according to the formula in section 2.2 of
   acl.ldc.upenn.edu/acl2004/emnlp/pdf/Mihalcea.pdf"

  [neighbour edge-weight weight-totals scores]
  (let [neighbour-id (key neighbour)
        neighbour-weight-total (get weight-totals neighbour-id)
        neighbour-score (get scores neighbour-id)]
    (if (> neighbour-weight-total 0)
      (/ (* edge-weight neighbour-score) neighbour-weight-total)
      0)))

(defn update-score-for-vertex
  "Returns new score for vertex using constant
   weight-totals and scores updated from previous run"
  [graph vertex weight-totals scores]
  (let [edges (. graph edgesOf vertex)
        id (key vertex)]
    (loop [summation 0
           e edges]
      (if (seq e)
        (let [fst (first e)
              src (. graph getEdgeSource fst)
              trg (. graph getEdgeTarget fst)
              weight (. graph getEdgeWeight fst)
              neighbour (if (= src vertex) trg src)
              v (if (= src vertex) src trg)]
          (recur
           (+ summation
              (add-to-summation neighbour weight weight-totals scores))
           (rest e)))
        (+ (- 1 damping-factor) (* damping-factor summation))))))

(defn update-scores
  "Returns new score using constant weight-totals
   and scores updated from previous run"
  [graph weight-totals scores]
  (update-totals update-score-for-vertex graph weight-totals scores))

(defn add-weighted-edges-for-vertex
  "Adds all non-zero edges to vertex"
  [graph vertex]
  (fn [neighbour]
    (let [weight (calc-weight vertex neighbour)]
      (if (and (> weight 0) (not (= vertex neighbour)))
        (do
          (. Graphs addEdgeWithVertices graph vertex neighbour weight)
          weight)
        0))))

(defn add-weighted-edges
  "Adds all non-zero edges to the graph"
  [graph]
  (let [vertices (. graph vertexSet)]
    (loop [v vertices
           weight-totals {}]
      (if (seq v)
        (let [vertex (first v)
              weights (map (add-weighted-edges-for-vertex graph vertex)
                           vertices)
              weight-total (reduce + weights)]
          (recur (rest v)
                 (assoc weight-totals (key vertex) weight-total)))
        weight-totals))))

(defn add-vertices
  "Adds a vertex for each element of vertices coll"
  [graph vertices]
  (if (seq vertices)
    [(. graph addVertex (first vertices))
     (add-vertices graph (rest vertices))]))

(defn build-vertices
  "Takes preprocessed text and returns id,sentence pair"
  [sentences]
  (loop [s sentences
         vertices {}
         id 0]
    (if (seq s)
      (recur (rest s)
             (assoc vertices id (first s))
             (inc id))
      vertices)))

(defn build-graph
  "Takes preprocessed text and returns a SimpleWeightedGraph"
  [sentences]
  (let [graph (new SimpleWeightedGraph DefaultWeightedEdge)
        vertices (build-vertices sentences)]
    (add-vertices graph vertices)
    graph))

(defn init-scores
  "Initialize scores to 0.15"
  [sentences]
  (let [s-count (count sentences)]
    (loop [scores {}
           id 0]
      (if (< id s-count)
        (recur (assoc scores id 0.15)
               (inc id))
        scores))))

(defn converge?
  "Checks for convergence at convergence-threshold"
  [new-scores old-scores]
  (let [s-count (count new-scores)]
    (loop [id 0]
      (if (< id s-count)
        (let [new (get new-scores id)
              old (get old-scores id)]
          (if (>= (Math/abs (- new old)) convergence-threshold)
            false
            (recur (inc id))))
        true))))

(defn run-algorithm
  "Updates scores until convergence is reached"
  [graph weight-totals old-scores rec]
  (let [new-scores (update-scores graph weight-totals old-scores)]
    (if (and (< rec max-iterations)
             (not (converge? new-scores old-scores)))
       (run-algorithm graph weight-totals new-scores (inc rec))
      new-scores)))

(defn rank
  [sentences]
  "Takes a vector of sentences and returns the ranking
   for each sentence"
  (let [graph (build-graph sentences)
        weight-totals (add-weighted-edges graph)
        scores (init-scores sentences)
        results (run-algorithm graph weight-totals scores 0)]
    (into (sorted-map-by (fn [k1 k2] (>= (get results k1) (get results k2))))
          results)))
