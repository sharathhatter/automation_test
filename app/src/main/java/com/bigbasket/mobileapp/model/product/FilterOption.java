package com.bigbasket.mobileapp.model.product;

public class FilterOption {

    private String filterName;
    private String filterSlug;
    private boolean parent = false;
    private String parentSlug;

    public FilterOption(String filterName, String filterSlug, String parentSlug, boolean parent) {
        this.filterName = filterName;
        this.filterSlug = filterSlug;
        this.parentSlug = parentSlug;
        this.parent = parent;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getFilterSlug() {
        return filterSlug;
    }

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    public String getParentSlug() {
        return parentSlug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FilterOption that = (FilterOption) o;

        return !(filterSlug != null ? !filterSlug.equals(that.filterSlug) : that.filterSlug != null);

    }

    @Override
    public int hashCode() {
        return filterSlug != null ? filterSlug.hashCode() : 0;
    }
}
