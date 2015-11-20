package com.bigbasket.mobileapp.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.NavigationAdapter;
import com.bigbasket.mobileapp.adapter.db.DynamicScreenAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.DynamicPageResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SectionTextItem;
import com.bigbasket.mobileapp.model.section.SubSectionItem;
import com.bigbasket.mobileapp.service.DynamicScreenSyncService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public final class SectionCursorHelper {
    private SectionCursorHelper() {
    }

    @SuppressWarnings("unchecked")
    public static NavigationAdapter getNavigationAdapter(Context context, Cursor sectionCursor,
                                                         @Nullable String selectedCategoryId) {
        Object[] data = getSectionNavigationItems(context, sectionCursor);
        ArrayList<SectionNavigationItem> sectionNavigationItems = (ArrayList<SectionNavigationItem>) data[0];
        String baseImgUrl = (String) data[1];
        HashMap<Integer, Renderer> rendererHashMap = (HashMap<Integer, Renderer>) data[2];

        NavigationAdapter navigationAdapter = new NavigationAdapter(context,
                FontHolder.getInstance(context).getFaceRobotoMedium(),
                sectionNavigationItems, DynamicScreenSyncService.MAIN_MENU, baseImgUrl, rendererHashMap);
        if (!TextUtils.isEmpty(selectedCategoryId)) {
            navigationAdapter.setSelectedCategoryString(selectedCategoryId);
        }
        return navigationAdapter;
    }

    private static Object[] getSectionNavigationItems(Context context, Cursor sectionCursor) {
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();
        sectionNavigationItems.addAll(getPreBakedNavigationItems(context));

        SectionData sectionData = null;
        GetDynamicPageApiResponse response = getSectionData(sectionCursor);
        if (response != null) {
            sectionData = response.sectionData;
            if (sectionData != null && sectionData.getSections() != null && sectionData.getSections().size() > 0) {
                for (Section section : sectionData.getSections()) {
                    if (section == null || section.getSectionItems() == null || section.getSectionItems().size() == 0)
                        continue;
                    if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
                        sectionNavigationItems.add(new SectionNavigationItem(section));
                    }
                    setSectionNavigationItemList(sectionNavigationItems, section.getSectionItems(),
                            section);
                }
            }

        }
        return new Object[]{sectionNavigationItems, sectionData != null ? sectionData.getBaseImgUrl() : null,
                sectionData != null ? sectionData.getRenderersMap() : null};
    }

    public static GetDynamicPageApiResponse getSectionData(Cursor sectionCursor) {
        if (sectionCursor != null && sectionCursor.moveToFirst()) {
            String sectionRespJson = sectionCursor.
                    getString(sectionCursor.getColumnIndex(DynamicScreenAdapter.COLUMN_SCREEN_DATA));
            if (!TextUtils.isEmpty(sectionRespJson)) {
                Gson gson = new Gson();
                DynamicPageResponse apiResponse = gson.fromJson(sectionRespJson, DynamicPageResponse.class);
                switch (apiResponse.status) {
                    case 0:
                        return apiResponse.apiResponseContent;
                }
            }
        }
        return null;
    }

    public static <T extends SectionItem> void setSectionNavigationItemList(
            ArrayList<SectionNavigationItem> sectionNavigationItems,
            ArrayList<T> sectionItems,
            Section section) {
        for (int i = 0; i < sectionItems.size(); i++) {
            SectionItem sectionItem = sectionItems.get(i);
            if ((sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText()))
                    || (sectionItem instanceof SubSectionItem && ((SubSectionItem) sectionItem).isLink())) {
                if (i == 0 && ((sectionItem instanceof SubSectionItem) && !((SubSectionItem) sectionItem).isLink())) {
                    // Duplicate the first element as it'll be used to display the back arrow
                    sectionNavigationItems.add(new SectionNavigationItem<>(section, sectionItem));
                }
                sectionNavigationItems.add(new SectionNavigationItem<>(section, sectionItem));
            }
        }
    }

    private static ArrayList<SectionNavigationItem> getPreBakedNavigationItems(Context context) {
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();

        // Home
        SectionItem homeSectionItem = new SectionItem(new SectionTextItem(context.getString(R.string.home), 0),
                null, null, -1, new DestinationInfo(DestinationInfo.HOME, null));
        ArrayList<SectionItem> homeSectionItems = new ArrayList<>();
        homeSectionItems.add(homeSectionItem);
        Section homeSection = new Section(null, null, Section.MSG, homeSectionItems, null);
        sectionNavigationItems.add(new SectionNavigationItem<>(homeSection, homeSectionItem));

        // My Basket
        SectionItem myBasketSectionItem = new SectionItem(new SectionTextItem(context.getString(R.string.my_basket_header), 0),
                null, null, -1, new DestinationInfo(DestinationInfo.BASKET, null));
        ArrayList<SectionItem> myBasketSectionItems = new ArrayList<>();
        myBasketSectionItems.add(myBasketSectionItem);
        Section myBasketSection = new Section(null, null, Section.MSG, myBasketSectionItems, null);
        sectionNavigationItems.add(new SectionNavigationItem<>(myBasketSection, myBasketSectionItem));

        // Smart Basket
        String smartBasketDeepLink = "bigbasket://smart-basket/";
        SectionItem smartBasketSectionItem = new SectionItem(new SectionTextItem(context.getString(R.string.smartBasket), 0),
                null, null, -1, new DestinationInfo(DestinationInfo.DEEP_LINK, smartBasketDeepLink));
        ArrayList<SectionItem> smartBasketSectionItems = new ArrayList<>();
        smartBasketSectionItems.add(smartBasketSectionItem);
        Section smartBasketSection = new Section(null, null, Section.MSG, smartBasketSectionItems, null);
        sectionNavigationItems.add(new SectionNavigationItem<>(smartBasketSection, smartBasketSectionItem));

        // Shopping List
        String shoppingListDeepLink = "bigbasket://sl/";
        SectionItem shoppingListSectionItem = new SectionItem(new SectionTextItem(context.getString(R.string.shoppingList), 0),
                null, null, -1, new DestinationInfo(DestinationInfo.DEEP_LINK, shoppingListDeepLink));
        ArrayList<SectionItem> shoppingListSections = new ArrayList<>();
        shoppingListSections.add(shoppingListSectionItem);
        Section shoppingListSection = new Section(null, null, Section.MSG, shoppingListSections, null);
        sectionNavigationItems.add(new SectionNavigationItem<>(shoppingListSection, shoppingListSectionItem));
        return sectionNavigationItems;
    }
}
