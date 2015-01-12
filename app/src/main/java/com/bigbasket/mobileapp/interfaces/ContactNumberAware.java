package com.bigbasket.mobileapp.interfaces;

import java.util.ArrayList;

/**
 * Created by jugal on 23/12/14.
 */
public interface ContactNumberAware {

    public ArrayList<String> getSelectedContacts();

    public void onContactNumberSelected(String contactNumber);

    public void onContactNumberNotSelected(String contactNumber);
}
