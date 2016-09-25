package com.czettner.inventory.data;


import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;

@RunWith(AndroidJUnit4.class)
public class InventoryDbHelperTest {

    private InventoryDbHelper db;

    public void setUp() throws Exception {
        // TODO: RenamingDelegatingContext is deprecated
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new InventoryDbHelper(context);
    }

    public void tearDown() throws Exception {
        db.close();
    }


    @Test
    public void dbCreatedproperly() throws Exception {
        // TODO: check if the database is created
    }
}
