package com.bigbasket.mobileapp;

import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

import com.bigbasket.mobileapp.adapter.db.DatabaseContentProvider;

/**
 * Created by manu on 2/11/15.
 */
public class ContentProviderTest extends ProviderTestCase2<DatabaseContentProvider> {

    private static final String TAG = ContentProviderTest.class.getName();

    private static MockContentResolver resolve; // in the test case scenario, we use the MockContentResolver to make queries


    /**
     * Constructor.
     *
     * @param providerClass     The class name of the provider under test
     * @param providerAuthority The provider's authority string
     */
    public ContentProviderTest(Class providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            Log.i(TAG, "Entered Setup");
            super.setUp();
            resolve = this.getMockContentResolver();
        }
        catch(Exception e) {
        }
    }

    @Override
    public void tearDown() throws Exception {
        try{
            super.tearDown();
        }
        catch(Exception e) {
        }
    }
}
