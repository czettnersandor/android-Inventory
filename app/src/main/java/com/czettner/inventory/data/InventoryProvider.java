package com.czettner.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.czettner.inventory.R;

import org.w3c.dom.Text;

public class InventoryProvider extends ContentProvider {
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    public static final int STOCK = 100;
    public static final int STOCK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_STOCK, STOCK);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_STOCK + "/#", STOCK_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                cursor = database.query(InventoryContract.StockEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case STOCK_ID:
                selection = InventoryContract.StockEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.StockEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                Uri newUri = insertStock(uri, contentValues);
                getContext().getContentResolver().notifyChange(uri, null);
                return newUri;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert new stock to the database
     * @param uri This must be the uri of the Stock table
     * @param values
     * @return Uri of the new stock entry
     */
    private Uri insertStock(Uri uri, ContentValues values) {
        String sku = values.getAsString(InventoryContract.StockEntry.COLUMN_SKU);
        String name = values.getAsString(InventoryContract.StockEntry.COLUMN_NAME);
        String supplier = values.getAsString(InventoryContract.StockEntry.COLUMN_SUPPLIER);
        String picture = values.getAsString(InventoryContract.StockEntry.COLUMN_PICTURE);
        int qty = values.getAsInteger(InventoryContract.StockEntry.COLUMN_QTY);


        if (sku == null || name == null || supplier == null) {
            throw new IllegalArgumentException("A stock entry requires SKU, Name, Supplier");
        }

        if (TextUtils.isEmpty(sku)
                || TextUtils.isEmpty(name)
                || TextUtils.isEmpty(supplier)
                || qty == 0) {
            throw new IllegalArgumentException("A stock entry requires valid SKU, Name, Supplier, Qty");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(InventoryContract.StockEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, getContext().getString(R.string.failed_to_insert_row) + uri);
            return null;
        }

        Log.e(LOG_TAG, getContext().getString(R.string.successfully_inserted_row_for) + uri);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case STOCK:
                rowsUpdated = database.delete(InventoryContract.StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STOCK_ID:
                selection = InventoryContract.StockEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = database.delete(InventoryContract.StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsUpdated != 0) {
            // Notify listeners about changes
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case STOCK:
                return updateStock(uri, contentValues, selection, selectionArgs);
            case STOCK_ID:
                selection = InventoryContract.StockEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                int rowsUpdated = updateStock(uri, contentValues, selection, selectionArgs);
                if (rowsUpdated != 0) {
                    // Notify listeners about changes
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateStock(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String sku = values.getAsString(InventoryContract.StockEntry.COLUMN_QTY);
        String name = values.getAsString(InventoryContract.StockEntry.COLUMN_NAME);
        String supplier = values.getAsString(InventoryContract.StockEntry.COLUMN_SUPPLIER);

        if (values.containsKey(InventoryContract.StockEntry.COLUMN_QTY) && sku == null) {
            throw new IllegalArgumentException("A stock entry requires a valid QTY");
        }

        if (values.containsKey(InventoryContract.StockEntry.COLUMN_NAME) && name == null) {
            throw new IllegalArgumentException("A stock entry requires a valid Name");
        }

        if (values.containsKey(InventoryContract.StockEntry.COLUMN_SUPPLIER) && supplier == null) {
            throw new IllegalArgumentException("A stock entry requires a valid Supplier");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Update the pets with the given values
        int rows = database.update(InventoryContract.StockEntry.TABLE_NAME, values, selection, selectionArgs);
        // If the rows is -1, then the update failed. Log an error and return null.
        if (rows == -1) {
            Log.e(LOG_TAG, getContext().getString(R.string.failed_to_update) + uri);
            return 0;
        }

        return rows;
    }
}
