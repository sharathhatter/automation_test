package com.bigbasket.mobileapp.model.product;

public class FilterOption {

    private String filterName;
    private String filterSlug;
    private boolean parent = false;
    private String parentSlug;
    private boolean selected = false;

    public FilterOption(String filterName, String filterSlug, String parentSlug, boolean parent) {
        this.filterName = filterName;
        this.filterSlug = filterSlug;
        this.parentSlug = parentSlug;
        this.parent = parent;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterSlug() {
        return filterSlug;
    }

    public void setFilterSlug(String filterSlug) {
        this.filterSlug = filterSlug;
    }

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getParentSlug() {
        return parentSlug;
    }

    public void setParentSlug(String parentSlug) {
        this.parentSlug = parentSlug;
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
