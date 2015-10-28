package com.bigbasket.mobileapp.model.account;

import java.util.ArrayList;

public class SocialAccountType {
    public static final String FB = "fb";
    public static final String GP = "gp";


    public static ArrayList<String> getSocialLoginTypes() {
        ArrayList<String> socialAccountTypes = new ArrayList<>();
        socialAccountTypes.add(FB);
        socialAccountTypes.add(GP);
        return socialAccountTypes;
    }


}
