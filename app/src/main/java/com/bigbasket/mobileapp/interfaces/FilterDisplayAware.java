package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.product.FilterOptionCategory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public interface FilterDisplayAware {

    public void setFilterView(ArrayList<FilterOptionCategory> filterOptionCategories,
                              Map<String, Set<String>> filteredOn, String fragmentTag);
}
