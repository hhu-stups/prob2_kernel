(ns prob-ui.syncclient
  (:require #+cljs [goog.Uri :as uri]
            #+cljs [goog.net.XhrIo :as xhr]
            #+cljs [om.core :as om :include-macros true]
            #+cljs [om.dom :as dom :include-macros true]
            [cognitect.transit :as transit])
  #+clj (:import java.io.ByteArrayInputStream)
  )

(def traceln (constantly nil))
#_(def traceln println)

(def debug (constantly nil))
#_(def debug println)
(def warnln println)
(def errorln println)

(declare notify-watchers)

(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.
  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
             {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
    -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}
(from clojure-contrip/map-tools"
  [f & maps]
  (apply
   (fn m [& maps]
     (if (every? map? maps)
       (apply merge-with m maps)
       (apply f maps)))
   maps))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
nested structure. keys is a sequence of keys. Any empty maps that result
will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn prep-merge [path mergemap]
  (if (seq path) (assoc-in {} path mergemap) mergemap))

(def ^:export state (atom {:current -1 :state {} :localstate {:history-reverse true}}))

(defmulti mk-fn :action)


(defmethod mk-fn :set [{:keys [value path]}]
  (fn [s]
    (debug :set  path value)
    (if (seq path)
      (update-in s path (constantly value))
      (merge s val))))

(defmethod mk-fn :del-keys [{:keys [value path]}]
  (fn [s]
    (debug :delkeys path value)
    (let [x (doall (map #(conj (vec path) %) value))]
      (reduce (fn [s e] (dissoc-in s e)) s x))))

(defmethod mk-fn :merge [{:keys [value path]}]
  (fn [s] (debug :merge  path value)
    (deep-merge-with (fn [_ e] e) s (prep-merge path value))))

(defmethod mk-fn :concat [{:keys [value path]}]
  (fn [s] (debug :concat  path value)
    (update-in s path #(into % value))))

(defmethod mk-fn :clear [_]
  {})

(defmethod mk-fn :del [{:keys [value path]}]
  (fn [s]
    (update-in
     s path
     (fn [v]
       (into [] (remove nil?
                        (map-indexed (fn [i v] (if ((into #{} value) i) nil v)) v)))))))

(defn compute-new-state [old-state id changes]
  (let [fns (doall (map mk-fn changes))
        chg-fkt (apply comp fns)
        state' (chg-fkt (:state old-state))]
    (assoc old-state :current id :state state')))


#+clj (defn read-transit [^String msg]
        (let [in (ByteArrayInputStream. (.getBytes msg))
              r (transit/reader in :json-verbose)]
          (transit/read r)))

#+cljs (defn read-transit [msg]
         (let [r (transit/reader :json-verbose)]
           (transit/read r msg)))

#+cljs (defn receiver [event]
         (let [response (.getResponseText (.-target event))
               [id changes] (read-transit response)
               modpath (doall (map second changes))]
           (swap! state compute-new-state id changes)
           (when (seq modpath) (notify-watchers modpath)))
         (traceln "receiver done")
         :ok)

#+cljs (defn get-state [url]
         (xhr/send url receiver "GET" "")
         (traceln "get-state done"))

#+cljs (defn ^:export pp-state []
         (let [cs @state]
           (println "ID:" (cs :current))
           (println "State:" (cs :state))
           (println "Localstate" (cs :localstate))))

#+cljs (defn ^:export get-updates []
         (let [url (str  "/updates/" (:current @state))]
           (get-state url)))

#+cljs (def watchers (atom {}))

#+cljs (defn ^:export register-watcher [nme js-fn]
         (swap! watchers assoc nme js-fn))

#+cljs (defn ^:export deregister-watcher [nme]
         (swap! watchers dissoc-in nme))

#+cljs (defn notify-watchers [paths]
         (doseq [[_ w] @watchers]
           (w paths)))

#+cljs (defn ^:export get-value [path]
         (get-in @state (cons :state path)))