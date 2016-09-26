package com.czettner.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.czettner.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STOCK = "stock";

    // Make sure this class can't be instantiated
    private InventoryContract() {}

    /**
     * Stock database table
      */
    public static final class StockEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCK);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        // Database related constants
        public final static String TABLE_NAME = "stock";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SKU = "sku";
        public final static String COLUMN_SUPPLIER = "supplier";
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_QTY = "qty";
    }
}
