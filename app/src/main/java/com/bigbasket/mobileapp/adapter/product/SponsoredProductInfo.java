package com.bigbasket.mobileapp.adapter.product;

import android.support.v7.widget.RecyclerView;

import com.bigbasket.mobileapp.model.ads.SponsoredAds;
import com.bigbasket.mobileapp.model.section.Section;

/**
 * Created by muniraju on 18/12/15.
 */
public class SponsoredProductInfo {
    private SponsoredAds sponsoredAds;
    private int lastInjectedPosition = -1;
    private int startPosition = -1;
    private int remainingItems;
    private int totalItems;

    public SponsoredProductInfo(SponsoredAds sponsoredAds) {
        this.sponsoredAds = sponsoredAds;
        if (sponsoredAds != null && sponsoredAds.getSectionData() != null
                && sponsoredAds.getSectionData().getSections() != null) {
            totalItems = remainingItems = sponsoredAds.getSectionData().getSections().size();
        }
    }

    public SponsoredAds getSponsoredAds() {
        return sponsoredAds;
    }

    public void setSponsoredAds(SponsoredAds sponsoredAds) {
        this.sponsoredAds = sponsoredAds;
    }

    public int getLastInjectedPosition() {
        return lastInjectedPosition;
    }

    public void setLastInjectedPosition(int lastInjectedPosition) {
        this.lastInjectedPosition = lastInjectedPosition;
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

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        if (sponsoredAds != null && this.startPosition < 0) {
            this.startPosition = startPosition * sponsoredAds.getPageStart();
        }
    }

    public int getNextInjectPosition() {
        if (sponsoredAds == null || sponsoredAds.getSectionData() == null
                || sponsoredAds.getSectionData().getSections() == null) {
            return RecyclerView.NO_POSITION;
        }
        if (lastInjectedPosition >= 0) {
            return lastInjectedPosition + sponsoredAds.getPageOffset() + 1;
        } else if (startPosition >= 0) {
            return startPosition;
        }
        return RecyclerView.NO_POSITION;
    }

    public Section getNextSponsoredItem() {
        int nextIndex = getNextSponsoredItemIndex();
        if (nextIndex >= 0 && nextIndex < totalItems) {
            return sponsoredAds.getSectionData().getSections().get(nextIndex);
        }
        return null;
    }

    public int getNextSponsoredItemIndex() {
        return totalItems - remainingItems;
    }

    public void reset() {
        lastInjectedPosition = -1;
        startPosition = -1;
        sponsoredAds = null;
        totalItems = 0;
        remainingItems = 0;
    }

    public void reset(SponsoredAds sponsoredSectionData) {
        if (sponsoredSectionData != null && sponsoredSectionData.getSectionData() != null
                && sponsoredSectionData.getSectionData().getSections() != null) {
            sponsoredAds = sponsoredSectionData;
            totalItems = remainingItems = sponsoredSectionData.getSectionData().getSections().size();
            lastInjectedPosition = -1;
            startPosition = -1;
        } else {
            reset();
        }
    }
}
