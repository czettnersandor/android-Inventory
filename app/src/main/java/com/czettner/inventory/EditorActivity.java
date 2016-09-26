package com.czettner.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.czettner.inventory.data.InventoryContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private EditText mSkuEditText;
    private EditText mNameEditText;
    private AutoCompleteTextView mSupplierAutocomplete;
    private EditText mQtyEditText;

    private Uri mStockUri;
    private boolean mStockHasChanged = false;
    private static final int EXISTING_STOCK_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mStockUri = getIntent().getData();
        if (mStockUri == null) {
            setTitle("New Stock");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            getLoaderManager().initLoader(EXISTING_STOCK_LOADER, null, this);
            setTitle("Edit stock");
        }

        mSkuEditText = (EditText) findViewById(R.id.sku_edit);
        mNameEditText = (EditText) findViewById(R.id.name_edit);
        mSupplierAutocomplete = (AutoCompleteTextView) findViewById(R.id.supplier_autocomplete);
        mQtyEditText = (EditText) findViewById(R.id.qty_edit);

        mSkuEditText.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mSupplierAutocomplete.setOnTouchListener(mTouchListener);
        mQtyEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * Listen to the touch events on edit fields and mark the data as changed
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mStockHasChanged = true;
            return false;
        }
    };

    /**
     * Save stock entry
     */
    private void saveStock() {
        String skuString = mSkuEditText.getText().toString().trim();
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierAutocomplete.getText().toString().trim();
        int qtyInt = 0;

        if (!TextUtils.isEmpty(mSkuEditText.getText())) {
            Integer.parseInt(mSkuEditText.getText().toString());
        }

        if (mStockUri == null && TextUtils.isEmpty(skuString) && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(supplierString)) {
            // If it's a new pet and all fields are empty, do nothing, the user probably tapped save
            // accidentally.
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.StockEntry.COLUMN_SKU, skuString);
        values.put(InventoryContract.StockEntry.COLUMN_NAME, nameString);
        values.put(InventoryContract.StockEntry.COLUMN_SUPPLIER, supplierString);
        values.put(InventoryContract.StockEntry.COLUMN_QTY, qtyInt);

        if (mStockUri == null) {
            // New Stock
            Uri newUri = getContentResolver().insert(InventoryContract.StockEntry.CONTENT_URI, values);
            if (newUri != null) {
                Toast.makeText(this, "New stock saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving new stock", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mStockUri, values, null, null);
            if (rowsAffected == 1) {
                Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating stock", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.StockEntry._ID,
                InventoryContract.StockEntry.COLUMN_NAME,
                InventoryContract.StockEntry.COLUMN_SKU,
                InventoryContract.StockEntry.COLUMN_QTY,
                InventoryContract.StockEntry.COLUMN_SUPPLIER
        };

        return new CursorLoader(this, mStockUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG, "Cursor loading finished");
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        cursor.moveToFirst();

        int skuColumnIndex = cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_SKU);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_SUPPLIER);
        int qtyColumnIndex = cursor.getColumnIndex(InventoryContract.StockEntry.COLUMN_QTY);

        String sku = cursor.getString(skuColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String supplier = cursor.getString(supplierColumnIndex);
        int qty = cursor.getInt(qtyColumnIndex);

        mSkuEditText.setText(sku);
        mNameEditText.setText(name);
        mSupplierAutocomplete.setText(supplier);
        mQtyEditText.setText(qty);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSkuEditText.setText("");
        mNameEditText.setText("");
        mSupplierAutocomplete.setText("");
        mQtyEditText.setText("");
    }
}
