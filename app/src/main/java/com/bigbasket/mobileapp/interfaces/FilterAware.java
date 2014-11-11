package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.product.FilterOptionCategory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilterAware {

    List<FilterOptionCategory> getFilterOptionCategory();

    Map<String, Set<String>> getFilteredOn();
}
