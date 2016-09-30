package com.czettner.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.czettner.inventory.data.InventoryContract;

import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private EditText mSkuEditText;
    private EditText mNameEditText;
    private AutoCompleteTextView mSupplierAutocomplete;
    private EditText mQtyEditText;
    private ImageView mPicture;
    private Button mTakePicture;

    private Uri mStockUri;
    private String mPicturePath;
    private boolean mStockHasChanged = false;
    private static final int EXISTING_STOCK_LOADER = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

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
        mPicture = (ImageView) findViewById(R.id.picture);
        mTakePicture = (Button) findViewById(R.id.take_picture);

        mTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStockHasChanged = true;
                dispatchTakePictureIntent();
            }
        });

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
     * Inflate menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveStock();
                finish();
                return true;
            case R.id.action_delete:
                deleteStock();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    /**
     * Take a picture using the camera
     */
    private void dispatchTakePictureIntent() {

        Intent intent = new Intent();
        // Accept only images
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Save stock entry
     */
    private void saveStock() {
        String skuString = mSkuEditText.getText().toString().trim();
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierAutocomplete.getText().toString().trim();
        int qtyInt = 0;

        if (!TextUtils.isEmpty(mQtyEditText.getText())) {
            qtyInt = Integer.parseInt(mQtyEditText.getText().toString());
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
            Log.e(LOG_TAG, "" + rowsAffected);
            if (rowsAffected == 1) {
                Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating stock", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteStock() {
        if (mStockUri == null) {
            return;
        }
        int r = getContentResolver().delete(mStockUri, null, null);
        if (r == 1) {
            Toast.makeText(this, "Deleting stock was successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to delete stock", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.StockEntry._ID,
                InventoryContract.StockEntry.COLUMN_NAME,
                InventoryContract.StockEntry.COLUMN_SKU,
                InventoryContract.StockEntry.COLUMN_QTY,
                InventoryContract.StockEntry.COLUMN_SUPPLIER,
                InventoryContract.StockEntry.COLUMN_PICTURE
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
        mQtyEditText.setText(Integer.toString(qty));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSkuEditText.setText("");
        mNameEditText.setText("");
        mSupplierAutocomplete.setText("");
        mQtyEditText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                mPicture.setImageBitmap(bitmap);
                mPicturePath = getRealPathFromURI(selectedImageUri);
                Log.d(LOG_TAG, mPicturePath);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
}
