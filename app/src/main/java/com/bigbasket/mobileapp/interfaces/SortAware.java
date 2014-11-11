package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.product.Option;

import java.util.List;


public interface SortAware {
    List<Option> getSortOptions();

    String getSortedOn();

    void setSortedOn(String sortedOn);
}
