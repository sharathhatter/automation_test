package com.bigbasket.mobileapp.interfaces;

import android.widget.AutoCompleteTextView;

import java.util.List;

public interface EmailAddressAware {
    public void addEmailsToAutoComplete(List<String> emailAddressCollection, AutoCompleteTextView emailView);
}
