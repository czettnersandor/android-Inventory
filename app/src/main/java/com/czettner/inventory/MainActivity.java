package com.czettner.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.czettner.inventory.data.InventoryContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int URL_LOADER = 0;

    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Ok lets experiment with something new. I'll try to use SimpleCursorAdapter
        // Creating the SimpleCurorAdapter with a null cursor (add it later at {@link #onLoadFinished()})
        String[] columns = {
                InventoryContract.StockEntry.COLUMN_SKU,
                InventoryContract.StockEntry.COLUMN_NAME,
                InventoryContract.StockEntry.COLUMN_SUPPLIER,
                InventoryContract.StockEntry.COLUMN_QTY
        };
        int[] views = {
                R.id.sku_text,
                R.id.name_text,
                R.id.supplier_text,
                R.id.qty_text
        };
        mAdapter = new SimpleCursorAdapter(this, R.layout.list_item, null, columns, views, 0);

        // Find the ListView which will be populated with the pet data
        ListView stockListView = (ListView) findViewById(R.id.list_view);
        stockListView.setAdapter(mAdapter);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        stockListView.setEmptyView(emptyView);

        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(InventoryContract.StockEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        // Initialise CursorLoader
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    /**
     * Inflate menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_dummy:
                insertDummy();
                return true;
            case R.id.delete_all:
                // Delete all Stock entry, dangerous
                getContentResolver().delete(InventoryContract.StockEntry.CONTENT_URI, null, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    private void insertDummy() {
        ContentValues values = new ContentValues();
        values.put(InventoryContract.StockEntry.COLUMN_SKU, "ABC1234");
        values.put(InventoryContract.StockEntry.COLUMN_NAME, "Porridge Oats");
        values.put(InventoryContract.StockEntry.COLUMN_SUPPLIER, "Quakers");
        values.put(InventoryContract.StockEntry.COLUMN_QTY, 12);
        Uri newUri = getContentResolver().insert(InventoryContract.StockEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (loaderID) {
            case URL_LOADER:
                String[] projection = {
                        InventoryContract.StockEntry._ID,
                        InventoryContract.StockEntry.COLUMN_SKU,
                        InventoryContract.StockEntry.COLUMN_NAME,
                        InventoryContract.StockEntry.COLUMN_SUPPLIER,
                        InventoryContract.StockEntry.COLUMN_QTY
                };
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,            // Parent activity context
                        InventoryContract.StockEntry.CONTENT_URI, // Uri to query
                        projection,      // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
