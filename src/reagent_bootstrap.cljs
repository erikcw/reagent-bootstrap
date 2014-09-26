(ns reagent-bootstrap
  "This namespace contains some basic reagent bootstrap components"
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-uuid.core :as uuid]))

(def panel-styles {:default "panel-default"
                   :primary "panel-primary"
                   :success "panel-success"
                   :info "panel-info"
                   :warning "panel-warning"
                   :danger "panel-danger"
                   nil "panel-default"})

;; Utilities
(defn rand-uuid-str []
  (.-uuid (uuid/make-random)))

;; HTML helpers
(defn attrs-content [items]
  {:pre [(coll? items)]}
  "Search for attributes and content from the items and returns a vector of both."
  (let [attrs (or (first (filter map? items)) {})
        content (vec (or (filter #(not (or (map? %) (keyword? %))) items)))]
    [attrs content]))

(defn merge-attrs [attrs-1 attrs-2]
  "Merge attributes of two tags (should be maps).
   Classes are also merged if both contains class."
  (let [attrs-pre (merge attrs-1 attrs-2)
        class1 (:class attrs-1)
        class2 (:class attrs-2)
        class-m (if (and class1 class2) (str class1 " " class2) nil)]
    (if class-m
      (merge attrs-pre {:class class-m})
      attrs-pre)))

(defn merge-tags [t1-items t2-items]
  "Merge two dom branches from the top.
    - Tag of the first dom branch is used while tag of the second is discarded.
    - Root level attributes are merged.
    - Content is merged (in a top level).

  Mainly usefull when new attributes are needed for components."
  (let [[t1-attrs t1-content] (attrs-content t1-items)
        [t2-attrs t2-content] (attrs-content t2-items)
        tag (first (filter keyword? [(first t1-items) (first t2-items)]))]
    [tag (merge-attrs t1-attrs t2-attrs) (concat t1-content t2-content)]))

;; Components
(defn accordion [id items &]
  "Accordion item. You should give a wrapper (:div for example) that holds
   accordion-items in a collection."
  (merge-tags
   [:div {:class "panel-group" :id id}]
   items))

(defn nav-item
  ([link title]
    (nav-item title false))
  ([link title active?]
    (vec (concat [:li] (if active? [{:class "active"}]) [[:a {:href link} title]]))))

(defn nav-pills [items &]
  (vec (concat [:ul {:class "nav nav-pills"}] items)))

(defn panel
  ([content]
    [:div {:class "panel panel-default"}
      [:div {:class "panel-body"} content]])
  ([title content]
    (panel title content :default))
  ([title content style]
    {:pre [(contains? panel-styles style)]}
    "Panel item."
    [:div {:class (str "panel " (panel-styles style))}
      [:div {:class "panel-heading"}
        [:h3 {:class "panel-title"} title]]
      [:div {:class "panel-body"} content]]))

(defn accordion-item [parent-id title content]
  (let [id (rand-uuid-str)]
    [:div {:class "panel panel-default"}
     [:div {:class "panel-heading"}
      [:h4 {:class "panel-title"}
       [:a {:data-toggle "collapse" :data-parent (str "#" parent-id) :href (str "#" id)}
        title]]]
     [:div {:id id :class "panel-collapse collapse"}
      [:div {:class "panel-body"}
       content]]]))

(defn list-group [items &]
  (merge-tags
   [:div {:class "list-group"}]
   items))

(defn clickable-list-group-item [title content]
  [:a {:href "#" :class "list-group-item"}
   [:h4 {:class "list-group-item-heading"} title]
   (if content
     [:div {:class "list-group-item-text"} content])])

(defn primary-button [items &]
  (merge-tags
   [:button {:type "button", :class "btn btn-primary"}]
   items))

; Modal components
(defn modal-close-button [items &]
  (merge-tags
   [:button {:type "button", :class "btn btn-default", :data-dismiss "modal"}]
    items))

(defn modal [title body footer]
  [:div {:class "modal fade"}
   [:div {:class "modal-dialog"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:button {:type "button", :class "close", :data-dismiss "modal"}
       [:span {:aria-hidden "true"} "X"]
       [:span {:class "sr-only"} "Close"]]
      [:h4 {:class "modal-title"} title]]
     [:div {:class "modal-body"}
      [:p body]]
     [:div {:class "modal-footer"} footer]]]])

;; Functional functions
(defn open-modal [modal]
  (let [id (rand-uuid-str)
        wrapper (js/jQuery (str "<div id=\"" id "\"></div>"))
        foo (.append (js/jQuery "body") wrapper)
        wrapper-dom (. js/document (getElementById id))]
    (reagent/render-component modal wrapper-dom)
    (-> (js/jQuery (str "#" id " .modal"))
        (.modal "show")
        ; FIXME: This is against reacts philosophy. View contents are based
        ; on model anyway so removing "template" is just stupid.
        (.on "hidden.bs.modal" #(.remove (js/jQuery (str "#" id)))))))
