(ns test-reagent-bootstrap
  (:require [purnam.test]
            [reagent-bootstrap :as b]
            [clojure.string :as string]
            [dommy.core :as dommy])
  (:use-macros [purnam.test :only [describe it is is-not fact]]
               [dommy.macros :only [node sel sel1]]))

;; Test utilities
(defn dfs-search [tree pred?]
  "Simple dfs-search that do not care about stackoverflows.. ((re)curses)
   Assuming dom trees are not very deep, this should do fine."
  (cond
   (pred? tree) tree                    ; Match case
   (and (coll? tree) (empty? tree)) nil ; Collection ends case
   (coll? tree)                         ; Recur case
     (first (filter #(not (nil? %)) (map #(dfs-search % pred?) tree)))
   :else nil)) ; Something else

(defn hiccup-contains? [hiccup-tag needle]
  (not (nil? (dfs-search hiccup-tag #(= % needle)))))

(defn log [items &] ;; Debugging purposes
  (.log js/console (string/join " " items)))

(defn exists-in-dom [selector]
  (not (nil? (sel1 selector))))

;; Tests
(describe "rand-uuid-str"
  (it "should give 100 uuids without repetitions"
    (let [uuids (repeatedly 100 (fn [] (b/rand-uuid-str)))
          cnt (count (set uuids))]
      (is cnt 100))))

(let [tag-1-items [:div {:class "foo"} "This is content."]
      tag-2-items [:div {:class "bar"} "This is content." [:span "and some more"]]
      tag-3-items [:div "This is content."]
      tag-4-items [:ul {:class "foo"}]
      no-tag-items ["Just content."]]

  (describe "attrs-content"
    (it "should split simple attributes and content"
      (is (b/attrs-content tag-1-items) [{:class "foo"} ["This is content."]]))
    (it "should split attributes and content when multiple contents"
      (is (b/attrs-content tag-2-items) [{:class "bar"} ["This is content." [:span "and some more"]]]))
    (it "should split attrs and content when no attrs"
      (is (b/attrs-content tag-3-items) [{} ["This is content."]]))
    (it "should split attrs and content when no content."
      (is (b/attrs-content tag-4-items) [{:class "foo"} []]))
    (it "should not fail if there's no tag."
      (is (b/attrs-content no-tag-items) [{} ["Just content."]]))
    (it "should fail if not collection given"
      (try
        (is-not (b/attrs-content "Raw content.") [{} (for [c "Raw content."] c)])
        (is 1 2) ; Should not reach this line
      (catch js/Error e))))

  (describe "merge-attrs"
    (it "should merge simple attrs"
      (is (b/merge-attrs {:foo "1" :bar "2"} {:bar "1" :baz "3"}) {:foo "1" :bar "1" :baz "3"}))
    (it "should specially merge classes"
      (is (b/merge-attrs {:class "fobar baz"} {:class "bar"}) {:class "fobar baz bar"})))

  (describe "merge-tags"
    (it "should merge two simple tags"
      ;; Not sure if it would be better to have content out side of the vector..
      (is (b/merge-tags tag-1-items tag-3-items) [:div {:class "foo"} ["This is content." "This is content."]]))
    (it "should include both classes"
      (is (b/merge-tags tag-1-items tag-2-items) [:div {:class "foo bar"} ["This is content." "This is content." [:span "and some more"]]]))))

(describe "nav-item"
  (it "should not create active item as default"
    (is (b/nav-item "#" "Foo") [:li [:a {:href "#"} "Foo"]]))
  (it "should give active and \"not-active\" as demanded"
    (is (b/nav-item "#" "Foo" false) [:li [:a {:href "#"} "Foo"]])
    (is (b/nav-item "#" "Foo" true) [:li {:class "active"} [:a {:href "#"} "Foo"]])))

(describe "nav-pils"
  (let [item-1 (b/nav-item "#" "Foo" true)
        item-2 (b/nav-item "#" "Bar" false)]
    (it "should create simple pils navigation"
      (is (b/nav-pills item-1 item-2) [:ul {:class "nav nav-pills"} item-1 item-2]))))

(let [title "Panel title"
      content "Panel content"
      primary-panel (b/panel title content :primary)]
  (describe "panel"
    (it "should have content inside"
      (is (hiccup-contains? primary-panel content) true))
    (it "should have title inside"
      (is (hiccup-contains? primary-panel title) true))
    (it "should make default panel when style is not specified"
      (is (hiccup-contains? (b/panel "foo" "bar") "panel panel-default") true))
    (it "should make the panel that is explicitly specified"
      (is (hiccup-contains? (b/panel "foo" "bar" :default) "panel panel-default") true)
      (is (hiccup-contains? (b/panel "foo" "bar" :primary) "panel panel-primary") true)
      (is (hiccup-contains? (b/panel "foo" "bar" :success) "panel panel-success") true)
      (is (hiccup-contains? (b/panel "foo" "bar" :info) "panel panel-info") true)
      (is (hiccup-contains? (b/panel "foo" "bar" :warning) "panel panel-warning") true)
      (is (hiccup-contains? (b/panel "foo" "bar" :danger) "panel panel-danger") true))
    (it "should fail if unspecified style given"
      (try
        (b/panel "foo" "bar" :my-style)
        (is 1 2) ; Should not reach this line
      (catch js/Error e)))
    (it "should create panel without title if only one attribute is given"
      (let [panel (b/panel [:p "Long text goes here.."])]
        (is (hiccup-contains? panel "panel-title") false)
        (is (hiccup-contains? panel "Long text goes here..") true)))))

(let [accordion-id "parent-id"
      footer "This is footer"
      i1-body [:div {:class "foo"} "This is body"]
      i1-title "This is title"
      i1 (b/accordion-item accordion-id i1-title i1-body)
      i2-body [:div {:class "foo"} "Body 2"]
      i2-title "Title 2"
      i2 (b/accordion-item accordion-id i2-title i2-body)
      accordion (b/accordion accordion-id i1 i2)]
  (describe "accordion-item"
    (it "should have content inside"
      (is (hiccup-contains? i1 i1-body) true))
    (it "should have title inside"
      (is (hiccup-contains? i1 i1-title) true))
    (it "should have #parent-id inside"
      (is (hiccup-contains? i1 (str "#" accordion-id)) true)))
  (describe "accordion"
    (it "consists of accordion-items"
      (is (hiccup-contains? accordion i1) true)
      (is (hiccup-contains? accordion i2) true))
    (it "should have specified id"
      (is (hiccup-contains? accordion accordion-id) true))))

(let [title "Foo"
      content "Bar"
      item1 (b/clickable-list-group-item title content)
      item2 (b/clickable-list-group-item title content)
      lgroup (b/list-group item1 item2)]
  (describe "clickable-list-group-item"
    (it "should have title inside"
      (is (hiccup-contains? item1 title) true))
    (it "should have content inside"
      (is (hiccup-contains? item1 content) true))
    (it "should allow empty content"
      (is-not (b/clickable-list-group-item title) nil)))
  (describe "list-group"
    (it "can consist of clickable-list-group-items"
      (is (hiccup-contains? lgroup item1) true)
      (is (hiccup-contains? lgroup item2) true))))

(describe "primary-button"
  (let [button (b/primary-button [:span "Press Me!"])
        button2 (b/primary-button "Text1" "Text2")
        button3 (b/primary-button {:on-click #(js/alert "foo")} "Foo Alert!")]
    (it "should be button"
      (is (hiccup-contains? button :button) true))
    (it "should have contents inside"
      (is (hiccup-contains? button2 "Text1") true)
      (is (hiccup-contains? button2 "Text2") true))
    (it "should have given attributes in it"
      (is (hiccup-contains? button3 :on-click) true))))

(describe "modal-close-button"
  (let [button (b/modal-close-button "Close & Save")
        button2 (b/modal-close-button {:on-click #(js/alert "Closed!")} "Close")]
    (it "should be button"
      (is (hiccup-contains? button :button) true))
    (it "should have contents inside"
      (is (hiccup-contains? button "Close & Save") true))
    (it "should have given attributes inside"
      (is (hiccup-contains? button2 :on-click) true))))

(describe "modal"
  (let [modal1 (b/modal "Title1" [:div "Content1"] "Footer1")]
    (it "should contain title"
      (is (hiccup-contains? modal1 "Title1") true))
    (it "should contain contents"
      (is (hiccup-contains? modal1 [:div "Content1"]) true))
    (it "should contain footer"
      (is (hiccup-contains? modal1 "Footer1") true))))

(describe "open-modal"
  (let [modal1 (b/modal "Title1" [:div {:id "this-is-a-secret"} "Content1"] "Footer1")]
    (it "should show my modal in the dom somewhere"
      (is (exists-in-dom "#this-is-a-secret") false)
      (b/open-modal modal1)
      (is (exists-in-dom "#this-is-a-secret") true))))
