package com.bigbasket.mobileapp.adapter.product;

import android.support.v7.widget.RecyclerView;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;

/**
 * Created by muniraju on 18/12/15.
 */
public class SponsoredProductInfo {
    private SectionData sectionData;
    private int lastInjectedPosition = 0;
    private int injectionWindow = -1;
    private int remainingItems;
    private int totalItems;

    public SponsoredProductInfo(SectionData sectionData) {
        this.sectionData = sectionData;
        if(sectionData != null && sectionData.getSections() != null){
            totalItems = remainingItems = sectionData.getSections().size();
        }
    }

    public SectionData getSectionData() {
        return sectionData;
    }

    public void setSectionData(SectionData sectionData) {
        this.sectionData = sectionData;
    }

    public int getLastInjectedPosition() {
        return lastInjectedPosition;
    }

    public void setLastInjectedPosition(int lastInjectedPosition) {
        this.lastInjectedPosition = lastInjectedPosition;
    }

    public int getInjectionWindow() {
        return injectionWindow;
    }

    public void setInjectionWindow(int injectionWindow) {
        this.injectionWindow = injectionWindow;
    }

    public int getRemainingItems() {
        return remainingItems;
    }

    public boolean hasMoreItems() {
        return remainingItems > 0;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setRemainingItems(int remainingItems) {
        this.remainingItems = remainingItems;
    }

    public int getNextInjectPosition(){
        if(injectionWindow > 0) {
            return lastInjectedPosition + injectionWindow;
        }
        return RecyclerView.NO_POSITION;
    }

    public Section getNextSponsoredItem(){
        int nextIndex = getNextSponsoredItemIndex() ;
        if(nextIndex >= 0 && nextIndex < totalItems) {
            return sectionData.getSections().get(nextIndex);
        }
        return null;
    }

    public int getNextSponsoredItemIndex(){
        return totalItems - remainingItems ;
    }

    public void reset() {
        lastInjectedPosition = 0;
        sectionData = null;
        totalItems = 0;
        remainingItems = 0;
    }

    public void reset(SectionData sponsoredSectionData) {
        if(sponsoredSectionData != null && sponsoredSectionData.getSections() != null){
            sectionData = sponsoredSectionData;
            totalItems = remainingItems = sponsoredSectionData.getSections().size();
            lastInjectedPosition = 0;
        } else {
            reset();
        }
    }
}
