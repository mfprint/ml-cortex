(ns ml-cortex.core
    (:require [cortex.nn.network :as network]
        [cortex.nn.layers :as layers]
        [cortex.nn.execute :as execute]
        [cortex.optimize.adam :as adam]
        [ml-cortex.data :as data]
        [clojure.pprint :refer [pprint]])
    (:gen-class))

(slurp "./resources/googlePlayStoreCleaned.txt")
(with-open [rdr (clojure.java.io/reader "./resources/googlePlayStoreCleaned.txt")]
(def data (reduce conj [] (line-seq rdr)) )
    ;; (println data)
)

;; Data Vector with lists in it
(def dataList (into [] (partition 1  data)))

;; Data Vector with Vectors in it
(def dataVector (mapv vec dataList))
;; (println (get dataVector 1))

;; Recives a vector with a single string and returns a vector with multiple data
(defn splitString[_vector]
    (def temp (get _vector 0))
    (def tempVector (clojure.string/split (apply str temp) #"\t"))
)

(def dataStructure
    (into []
        (map (fn [data]
            (splitString data)
            ;;assoc to replace previous string value
            (def add0 (assoc data 0 (get tempVector 0)))
            ;;conj to append data
            (def add1 (conj add0 (get tempVector 1)))
            (def add2 (conj add1 (get tempVector 2)))
            (def add3 (conj add2 (get tempVector 3)))
            (def add4 (conj add3 (get tempVector 4)))
            (def add5 (conj add4 (get tempVector 5)))
            (def add6 (conj add5 (get tempVector 6)))
            (def add7 (conj add6 (get tempVector 7)))
            (def add8 (conj add7 (get tempVector 8)))
            (def add9 (conj add8 (get tempVector 9)))
            (def add10 (conj add9 (get tempVector 10)))
            (def add11 (conj add10 (get tempVector 11)))
            ;;need to finish without def statement
            (conj add11 (get tempVector 12 ))
        ) dataVector)
    )
)

;;Filter Operations
(def paidApps
    (into []
        (filter (fn [data]
            (def _type (get data 6))
            (= _type "Paid")
        ) dataStructure)
    )
)
;;(println paidApps)

(def gameApps
    (into []
        (filter (fn [data]
            (def category (get data 1))
            (= category "GAME")
        ) dataStructure)
    )
)
;;(println gameApps)

;; Map operations
(def appNamesOnly
    (into []
        (map (fn [data]
            (subvec data 0 1)
        ) dataStructure)
    )
)
;;(println appNamesOnly)

;;Map and reduce Operation
(def reviewsOnly
    (into []
        (map (fn [data]
            (subvec data 3 4)
        ) dataStructure)
    )
)

;;(println reviewsOnly)
(def ReviewsNumber (subvec reviewsOnly 1) )
;;(println ReviewsNumber)
;;(def totalReviews (conj (vector-of :int) ReviewsNumber) )

(def a [])
;;(def x (atom 0))

(doseq [n ReviewsNumber]
    ;;(println @x)
    (def a (conj a ( Integer/parseInt (get n 0) )) )
    ;;(swap! x inc)
)

;;Reduce Operation
(def totalReviews (reduce + a) )
;;(println totalReviews)

;; -----------------------| SECOND PARTIAL |----------------------- ;;

(def dataCount (count dataStructure))
(println "DATA COUNT:" dataCount)

;; ----------------|| Limpieza de datos ||---------------- ;;

(def x
    (into []
        (map (fn [data]
            (into [] (subvec data 2 5))
        ) dataStructure)
    )
)
;;(println x)

(def y
    (into []
        (map (fn [data]
            (into [] (subvec data 6 7))
        ) dataStructure)
    )
)
;;(println y)

(def xTrain
    (subvec x 1 (int (* dataCount 0.8)))
)
;;(println xTrain)

(def yTrain
    (subvec y 1 (int (* dataCount 0.8)))
)

(def xTest
    (subvec x (int (* dataCount 0.8)) dataCount)
)

(def yTest
    (subvec y (int (* dataCount 0.8)) dataCount)
)

;; ----------------|| Cortex ||---------------- ;;

; (def my_data (subvec (into [] (data/load-data)) 0 11))
(defonce all-data (-> xTrain
    ; (shuffle)
    ; (data/train-validation-split 0.70)
))

(def num-nodes 32) ;; Num de neuronas

(def network-architecture
    [(layers/input 11 1 1 :id :x)

    (layers/linear num-nodes)
    (layers/relu)

    (layers/linear num-nodes)
    (layers/relu)

    (layers/linear 1 :id :y)])

(def starting-network {:network  (network/linear-network
    network-architecture)
    :optimizer (adam/adam :alpha 0.01)})

(defn validate
    "Validación de la red"
    [cur-network]
    (println "Resultados:" (pr-str (data/stats (:network cur-network) (:validation all-data)))))

(defn train
    "Entrenamiento de la red"
    [incoming-network epoch-count]
    (loop [{:keys [network optimizer] :as cur-network} incoming-network epoch 0]
        (if (zero? (mod epoch 10))
          (println "Epoch: " epoch (pr-str (data/stats network (:train all-data)))))

        (if (> epoch-count epoch)
          (recur (execute/train network (:train all-data) :optimizer optimizer) (inc epoch))
          cur-network)))

(defn -main [& args]
    (println "Entenamiento en 100 epocas")
    (let [r1 (train starting-network 100)]
        (validate r1)))
