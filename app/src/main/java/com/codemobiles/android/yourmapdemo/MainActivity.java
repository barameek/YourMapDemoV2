package com.codemobiles.android.yourmapdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codemobiles.android.yourmapdemo.util.CMMapUtil;
import com.codemobiles.android.yourmapdemo.util.DirectionsAPI;
import com.codemobiles.android.yourmapdemo.util.OnDirectionAPIListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private String mCurrentLocStr;
    List<Address> addressList = null;
    static ProgressDialog progDialog = null;
    private TextView mLocTextView;
    private EditText mAddressEditText;
    private ImageView mGeoCodingBtn;
    private GoogleMap mMapView;
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 1000;
    private List<LatLng> listOfLatLng = new ArrayList<>();
    private LocationRequest mRequest;
    public static int REQ_GEO_CODING_SEARCH = 1;
    private GoogleApiClient mApiClient;
    private Polygon polygon;
    private MarkerOptions mapMarker;
    int currentPt;
    int mAnimationZoom = 15;
    private TextView mAnimationBtn;
    int mPinDrawables[] = new int[]{R.drawable.pin_01,
            R.drawable.pin_02,
            R.drawable.pin_03,
            R.drawable.pin_04,
            R.drawable.pin_05,
            R.drawable.pin_06,
            R.drawable.pin_07};
    private int pinCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindWidget();
        setWidgetEventListener();

        mMapView.getUiSettings().setZoomControlsEnabled(true);
        mMapView.setTrafficEnabled(true);
        /*
         * MAP_TYPE_NONE No base map tiles. MAP_TYPE_NORMAL Basic maps.
		 * MAP_TYPE_SATELLITE Satellite maps with no labels. MAP_TYPE_HYBRID
		 * Satellite maps with a transparent layer of major streets.
		 * MAP_TYPE_TERRAIN
		 */
        mMapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        mMapView.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.marker_info_content, null);
                TextView tvTitle = (TextView) v.findViewById(R.id.tv_title);
                if (marker.getTitle() != null && !marker.getTitle().equals("")) {
                    tvTitle.setText(marker.getTitle());
                    tvTitle.setVisibility(View.VISIBLE);
                } else {
                    tvTitle.setVisibility(View.GONE);
                }
                LatLng latLng = marker.getPosition();
                TextView poistionTextView = (TextView) v.findViewById(R.id.position);
                DecimalFormat formatter = new DecimalFormat("#,###.000");

                String lat = formatter.format(latLng.latitude) + "°";
                String lng = formatter.format(latLng.longitude) + "°";
                poistionTextView.setText(lat + "," + lng);

                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });


        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        mRequest = LocationRequest.create();
                        mRequest.setInterval(UPDATE_INTERVAL);
                        mRequest.setFastestInterval(FASTEST_INTERVAL);
                        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mRequest, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                updateLocationTextView(location);
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(getApplicationContext(), "Connection is susppended!", Toast.LENGTH_LONG).show();

                    }
                }).build();
    }

    private void bindWidget() {
        SupportMapFragment mySupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mMapView);
        mMapView = mySupportMapFragment.getMap();
        mLocTextView = (TextView) findViewById(R.id.mLocationTextView);
        mGeoCodingBtn = (ImageView) findViewById(R.id.mGeoCodingBtn);
        mAnimationBtn = (TextView) findViewById(R.id.mAnimationBtn);
        mAddressEditText = (EditText) findViewById(R.id.mAddressEditText);
    }


    private void updateLocationTextView(Location location) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        final String lat = formatter.format(location.getLatitude());
        final String lng = formatter.format(location.getLongitude());

        mCurrentLocStr = String.format("Lat: %s°, Long: %s°", lat, lng);
        mLocTextView.setText(mCurrentLocStr);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CMMapUtil.turnLocationTrackingOn(this);
        mMapView.setMyLocationEnabled(true);

        mApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mApiClient.disconnect();

    }


    private void setWidgetEventListener() {


        mGeoCodingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AddressListActivity.class);
                i.putExtra("ADDRESS", mAddressEditText.getText().toString());
                startActivityForResult(i, REQ_GEO_CODING_SEARCH);


            }
        });

        mAnimationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                float zoomValue = (float) (mAnimationZoom + 2);

                mMapView.animateCamera(
                        CameraUpdateFactory.zoomTo(zoomValue),
                        5000,
                        MyCancelableCallback);

                currentPt = 0 - 1;
            }
        });


        mMapView.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //final float currentZoom = mMapView.getCameraPosition().zoom;
                //mMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom));


                // Already two locations
                if (mapMarker != null) {
                    mMapView.clear();
                    mapMarker = null;
                }


                mapMarker = new MarkerOptions();
                mapMarker.position(latLng).title("Destination").icon(BitmapDescriptorFactory.fromResource(getDummyMarkerDrawableID()));
                mMapView.addMarker(mapMarker);


                Location myLocation = mMapView.getMyLocation();
                if (myLocation != null) {
                    LatLng origin = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    LatLng dest = mapMarker.getPosition();


                    new DirectionsAPI(getApplicationContext()).drawDirection(mMapView, origin, dest, Color.parseColor("#E91E63"), new OnDirectionAPIListener() {
                        @Override
                        public void onFinished(DirectionsAPI api, ArrayList<LatLng> points) {
                            listOfLatLng = points;
                            // api.routeDistance
                        }
                    });
                }


            }
        });

        mMapView.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // placeMarker(latLng.latitude, latLng.longitude, "xxxx");
                drawPolygon(latLng);
            }
        });

        mMapView.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent i = new Intent(getApplicationContext(), StreetViewActivity.class);
                i.putExtra("lat", marker.getPosition().latitude);
                i.putExtra("lng", marker.getPosition().longitude);
                startActivity(i);

            }
        });


    }


    private void drawPolygon(LatLng latLng) {
        listOfLatLng.add(latLng);
        if (listOfLatLng.size() > 2) {
            if (polygon != null) {
                polygon.remove();
            }
            polygon = mMapView.addPolygon(new PolygonOptions()
                    .addAll(listOfLatLng)
                    .strokeColor(Color.parseColor("#3978DD"))
                    .fillColor(Color.parseColor("#773978DD")));
            polygon.setStrokeWidth(2);
            showAllMarkers();
            showSizeOfPolygon();
        }

        BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromResource(getDummyMarkerDrawableID());
        mMapView.addMarker(new MarkerOptions().position(latLng).icon(markerIcon));

    }

    private void showAllMarkers() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng position : listOfLatLng) {
            builder.include(position);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMapView.animateCamera(cu);

    }

    private void showSizeOfPolygon() {
        // calculate area in polygon
        double sizeInSquareMeters = CMMapUtil.calculatePolygonArea(polygon.getPoints());
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String msg = formatter.format(sizeInSquareMeters / 1000) + " kilometer²";
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }


    public int getDummyMarkerDrawableID() {

        int random = pinCount++ % 7;
        return mPinDrawables[random];
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQ_GEO_CODING_SEARCH) {
            double lat = data.getDoubleExtra("LAT", 0);
            double lng = data.getDoubleExtra("LNG", 0);
            String title = data.getStringExtra("TITLE");
            placeMarker(lat, lng, title);
        }

    }


    private void placeMarker(double lat, double lng, String title) {
        LatLng latLng = new LatLng(lat, lng);
        listOfLatLng.add(latLng);

        Marker marker = mMapView.addMarker(new MarkerOptions().position(latLng).title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_01)));
        //marker.remove(); // Deleting a marker
        mMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        // mMapView.clear(); // Clear all overlay
    }



    GoogleMap.CancelableCallback MyCancelableCallback =
            new GoogleMap.CancelableCallback() {

                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(), "On Cancel", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {

                    if (++currentPt < listOfLatLng.size()) {

                        //Get the current location
                        Location startingLocation = new Location("starting point");
                        startingLocation.setLatitude(mMapView.getCameraPosition().target.latitude);
                        startingLocation.setLongitude(mMapView.getCameraPosition().target.longitude);

                        //Get the target location
                        Location endingLocation = new Location("ending point");
                        endingLocation.setLatitude(listOfLatLng.get(currentPt).latitude);
                        endingLocation.setLongitude(listOfLatLng.get(currentPt).longitude);

                        //Find the Bearing from current location to next location
                        float targetBearing = startingLocation.bearingTo(endingLocation);

                        LatLng targetLatLng = listOfLatLng.get(currentPt);
                        float targetZoom = mAnimationZoom;

                        //Create a new CameraPosition
                        CameraPosition cameraPosition =
                                new CameraPosition.Builder()
                                        .target(targetLatLng)
                                        .bearing(targetBearing)
                                        .zoom(targetZoom)
                                        .build();


                        mMapView.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                5000,
                                MyCancelableCallback);

                        Toast.makeText(getApplicationContext(), "Animate to: " + listOfLatLng.get(currentPt) + "\n" +
                                "Bearing: " + targetBearing, Toast.LENGTH_SHORT).show();


                    } else {
                        Toast.makeText(getApplicationContext(), "onFinish", Toast.LENGTH_SHORT).show();
                    }

                }
            };



}
