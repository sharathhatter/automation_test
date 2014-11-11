package com.bigbasket.mobileapp.model.search;

/**
 * Created with IntelliJ IDEA.
 * User: jugal
 * Date: 29/1/14
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoCompleteTermModel {

    private String term;
    private String category;
    private int termSize;
    private int categorySize;
    private String headerTitle;

    private String title;
    private boolean isGroupHeader = false;
    String tag;

    public AutoCompleteTermModel() {

    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public AutoCompleteTermModel(String title, String hearder) {
        this(title);
        isGroupHeader = true;
    }

    public AutoCompleteTermModel(String title) {
        super();
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isGroupHeader() {
        return isGroupHeader;
    }

    public void setGroupHeader(boolean groupHeader) {
        isGroupHeader = groupHeader;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    @Override
    public String toString() {
        return title;
    }
}
