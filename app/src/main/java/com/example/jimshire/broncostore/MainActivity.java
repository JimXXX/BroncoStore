package com.example.jimshire.broncostore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvViewShoppingCart = (TextView) findViewById(R.id.tvViewShoppingCart);
        SpannableString content = new SpannableString(getText(R.string.shopping_cart));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvViewShoppingCart.setText(content);
        tvViewShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShoppingCartActivity.class);
                startActivity(intent);
            }
        });


        ListView lvProducts = (ListView) findViewById(R.id.lvProducts);
        lvProducts.addHeaderView(getLayoutInflater().inflate(R.layout.product_list_header, lvProducts, false));

//        ArrayList<Product> products = new ArrayList<Product>();
        productAdapter = new ProductAdapter(this, new ArrayList<Product>());
//        productAdapter.updateProducts(products);

        lvProducts.setAdapter(productAdapter);

        lvProducts.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Product product = productAdapter.getItem(position - 1);
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("product", product);

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        // Start the AsyncTask to fetch the products data
        ProductAsyncTask task = new ProductAsyncTask();
        task.execute(Constant.MENU_REQUEST_URL);

        //ac: register for beacon events
        LocalBroadcastManager.getInstance(this).registerReceiver(beaconStatusReceiver,
                new IntentFilter("custom-event-name"));
    }

    private BroadcastReceiver beaconStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("beaconStatus");
            Log.v("Bronco receiver", "Got message: " + message);
            TextView textView = (TextView) findViewById(R.id.beaconStatus);
            textView.setText(message);

        }
    };


    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                beaconStatusReceiver, new IntentFilter("beaconStatus"));

        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(beaconStatusReceiver);
    }

    private class ProductAsyncTask extends AsyncTask<String, Void, List<Product>>{


        @Override
        protected List<Product> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }
            List<Product> result = QueryUtils.fetchProductData(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(List<Product> data) {
            productAdapter.clear();

            if (data != null && !data.isEmpty()) {
                productAdapter.updateProducts(data);
            }
        }
    }

}

