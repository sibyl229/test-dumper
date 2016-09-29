(ns starter.core
  (:require [datomic.api :as d]
            [clojure.string :as str]
            [clj-time.coerce :as tc]
            [clj-time.format :as tf]
	    [environ.core :refer (env)]))

(def uri (env :trace-db))
(def con (d/connect uri))

(def class-names ["Gene"])


(d/q '[:find ?class-ident .
       :in $ ?class
       :where
       [?attr :pace/identifies-class ?class]
       [?attr :db/ident ?class-ident]] (d/db con) "Gene")

(defn is-wb-id [id]
  (re-matches #"WBGene.*" id))

(defn dump-class
  "Dump object of class `class` from `db`."
  [db class & {:keys [query delete tag follow format limit]}]
  (if-let [ident (d/q '[:find ?class-ident .
                        :in $ ?class
                        :where
                        [?attr :pace/identifies-class ?class]
                        [?attr :db/ident ?class-ident]]
                      db class)]
    (->> (d/q '[:find [?gid ...]
                :in $ ?ident
                :where [?id ?ident ?gid]]
              db ident)
         (sort)
         (take (or limit Integer/MAX_VALUE)))
;;     (doseq []
;;       ( id)
;; ;;      (dump-object (ace-object db id))
;; 	)
;;    (throw-exc "Couldn't find '" class "'")
    ))

(defn writelines [file-path lines]
  (with-open [wtr (clojure.java.io/writer file-path)]
    (doseq [line lines] (.write wtr (str line "\n")))))

(->> (dump-class (d/db con) "Gene")
     (filter is-wb-id)
     (writelines "output/gene-ids.txt"))

;;(use 'starter.core :reload)
;;(dump-class (d/db con) "Gene")
