package com.codemobiles.android.yourmapdemo;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class AddressListActivity extends AppCompatActivity {

    private String mInputAddress;
    public List<Address> addressList;
    private ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);
        mListView = (ListView)findViewById(R.id.listview);
        Intent i = getIntent();
        mInputAddress = i.getStringExtra("ADDRESS");
        Toast.makeText(getApplicationContext(), mInputAddress,Toast.LENGTH_LONG).show();
        runGeoCodingSearch();
        setupToolbar();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Address address = addressList.get(position);

                int maxAddressLines = address.getMaxAddressLineIndex();
                String locationDescription = "";
                for (int i = 0; i < maxAddressLines; i++) {
                    locationDescription = locationDescription + address.getAddressLine(i) + "\n";
                }

                double lat = address.getLatitude();
                double lng = address.getLongitude();

                Intent i = new Intent();
                i.putExtra("LAT", lat);
                i.putExtra("LNG", lng);
                i.putExtra("TITLE", locationDescription);

                setResult(RESULT_OK, i);
                finish();
            }
        });
    }


    private void setupToolbar() {
        // basic setup actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }


    private void runGeoCodingSearch() {

        final Handler handler = new Handler();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Geocoder geoCoder = new Geocoder(getApplicationContext(), new Locale("TH"));
                     addressList = geoCoder.getFromLocationName(mInputAddress, 30);  // normal geocoding
                    //addressList = geoCoder.getFromLocation(13.2581015, 100.920621, 30); // บางแสน 13.2581015, 100.920621 reverse geo coding

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListView.setAdapter(new EfficientAdapter(AddressListActivity.this));
                        }
                    });
                }catch (Exception e){
                    Log.i("codemobiles", e.getMessage());
                }
            }
        }).start();
    }


    public class EfficientAdapter extends BaseAdapter{

        private LayoutInflater mInflater;

        public EfficientAdapter(Context context){
            mInflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return addressList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null){
                convertView = mInflater.inflate(R.layout.address_list_item, null);
                holder = new ViewHolder();
                holder.titleTextView = (TextView) convertView.findViewById(R.id.addressTitleTextView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            Address item = addressList.get(position);
            String title = item.getAddressLine(0);
            String lat = String.valueOf(item.getLatitude());
            String lng = String.valueOf(item.getLongitude());
            String index = String.valueOf(position + 1);
            String msg = String.format("<b><font color='#FEAA3F'>%s. %s </font></b><br/>%s°, %s°", index, title,lat,lng);
            holder.titleTextView.setText(Html.fromHtml(msg));

            return convertView;
        }
    }

    public class ViewHolder{
        TextView titleTextView;

    }
}
