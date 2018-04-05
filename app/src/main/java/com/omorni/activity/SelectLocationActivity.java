package com.omorni.activity;


import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.omorni.R;
import com.omorni.adapter.SelectLocationAdapter;

import com.omorni.model.SellerDetailResponse;

import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.RequestBody;


public class SelectLocationActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private TextView request_service;
    SelectLocationActivity context;
    SavePref savePref;

    GoogleApiClient mGoogleApiClient;
    LatLng latLng;
    LocationManager locationManager;
    GoogleMap mGoogleMap;
    private static final long MIN_TIME = 100;
    private static final float MIN_DISTANCE = 1000;
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String PLACES_DETAIL_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_DETAIL = "/details";
    private ArrayList<String[]> resultList;
    private String selectedlat = "", selectedlong = "", selected_place_name = "";
    private boolean is_search = false, is_selected_place = true;
    private float currentZoom = 12;
    private RelativeLayout loading_layout;
    private RelativeLayout main_layout;
    private TextView loading_text;
    private ImageView loading_image;
    private SellerDetailResponse sellerDetailResponse;

    private RecyclerView recycle;
    private EditText edt_search;
    SelectLocationAdapter deatilsAdapter;
    private ImageView search, cross;
    private String dummy_text = "";
    boolean push_screen = false;
    String request_id = "";
    Dialog dialog;
    ProgressDialog progressDialog;
    private String selected_package = "", seller_package_id = "";
    private boolean show_pin = false, isMap_move = true, setTimer = true;
    private TextView done;
    private ImageView loading_image_pin;
    Marker currLocationMarker;
    private ImageView current_location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        savePref = new SavePref(context);

//        if (savePref.getUserType().equals("1")) {
        setTheme(R.style.AppThemeSeller);
        setContentView(R.layout.select_location_activity_seller);
        /*} else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.select_location_activity_buyer);
        }*/

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        initialize();
        setToolbar();
        setUpUI();


    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.select_location);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpUI() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;


        if (!selectedlat.equals("")) {
            edt_search.setText(getIntent().getStringExtra("selected_place"));
            edt_search.setSelection(edt_search.getText().length());

            show_pin = false;
            Utils.hideKeyboard(SelectLocationActivity.this);
            request_service.setVisibility(View.VISIBLE);
            loading_image_pin.setVisibility(View.GONE);
            done.setVisibility(View.GONE);
            cross.setVisibility(View.GONE);
            isMap_move = false;
            LatLng selectedPlaceLatLng = new LatLng(Double.parseDouble(selectedlat), Double.parseDouble(selectedlong));

            if (Utils.isConfigRtl(SelectLocationActivity.this))
                currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_arabic)).snippet("true"));
            else
                currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_tip)).snippet("true"));

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceLatLng, currentZoom));
            Log.e("here", "there " + getIntent().getStringExtra("selected_place"));

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isGpsOn(context)) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else {
            showGPSDisabledAlertToUser();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected synchronized void buildGoogleApiClient() {
//        Toast.makeText(context, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    private void initialize() {
        selectedlat = getIntent().getStringExtra("selected_lat");
        selectedlong = getIntent().getStringExtra("selected_long");

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        selected_package = getIntent().getStringExtra("package");
        sellerDetailResponse = getIntent().getParcelableExtra("selected_package");
        push_screen = getIntent().getBooleanExtra("push_screen", false);
        edt_search = (EditText) findViewById(R.id.edt_search);
        request_service = (TextView) findViewById(R.id.request_service);

        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        loading_text = (TextView) findViewById(R.id.loading_text);
        done = (TextView) findViewById(R.id.done);
        loading_image = (ImageView) findViewById(R.id.loading_image);
        recycle = (RecyclerView) findViewById(R.id.recycle);
        search = (ImageView) findViewById(R.id.search);
        cross = (ImageView) findViewById(R.id.cross);
        loading_image_pin = (ImageView) findViewById(R.id.pin);
        current_location = (ImageView) findViewById(R.id.current_location);

        request_service.setOnClickListener(this);
        done.setOnClickListener(this);
        current_location.setOnClickListener(this);

        if (push_screen) {
            request_id = getIntent().getStringExtra("req_id");
            final AlertDialog.Builder alert = new AlertDialog.Builder(SelectLocationActivity.this);
            alert.setTitle(R.string.app_name).setCancelable(false)
                    .setMessage(getIntent().getStringExtra("message"))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }

        resultList = new ArrayList<String[]>();
        deatilsAdapter = new SelectLocationAdapter(SelectLocationActivity.this, resultList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectLocationActivity.this, LinearLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(linearLayoutManager);
        recycle.setAdapter(deatilsAdapter);


        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Utils.hideKeyboard(SelectLocationActivity.this);
                    if (!is_selected_place) {
                        if (v.getText().toString().length() > 0) {
                            recycle.setVisibility(View.VISIBLE);
                            cross.setVisibility(View.VISIBLE);
                            search.setVisibility(View.GONE);
                        } else {
                            recycle.setVisibility(View.GONE);
                            cross.setVisibility(View.GONE);
                            search.setVisibility(View.VISIBLE);
                        }

                        resultList = autocomplete1(v.getText().toString(), true);

                    }
                    return true;
                }
                return false;
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                request_service.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                loading_image_pin.setVisibility(View.VISIBLE);
                edt_search.setText("");
                show_pin = true;

            }
        });

        edt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                show_pin = true;
                edt_search.setEnabled(true);
                edt_search.setFocusable(true);
                isMap_move = true;
                request_service.setVisibility(View.GONE);

                if (loading_image_pin.getVisibility() == View.GONE) {
                    onCameraChange();
                    done.setVisibility(View.VISIBLE);
                    cross.setVisibility(View.VISIBLE);
                    search.setVisibility(View.GONE);
                }

                loading_image_pin.setVisibility(View.VISIBLE);
                Utils.showKeyboard(edt_search, SelectLocationActivity.this);

                if (currLocationMarker != null) {
                    currLocationMarker.remove();
                    currLocationMarker = null;
                }
            }
        });

        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().length() > 0) {
                    if (currLocationMarker != null) {
                        if (currLocationMarker.getSnippet().equals("true")) {

                        } else {
                            show_pin = true;
                            loading_image_pin.setVisibility(View.VISIBLE);
                            request_service.setVisibility(View.GONE);
                        }
                    } else {
                        show_pin = true;
                        loading_image_pin.setVisibility(View.VISIBLE);
                        request_service.setVisibility(View.GONE);
                    }

                } else {
                    if (currLocationMarker != null) {
                        if (currLocationMarker.getSnippet().equals("true")) {

                        } else {
                            show_pin = true;
                            loading_image_pin.setVisibility(View.VISIBLE);
                            request_service.setVisibility(View.GONE);
                            done.setVisibility(View.GONE);
                        }
                    } else {
                        show_pin = true;
                        loading_image_pin.setVisibility(View.VISIBLE);
                        request_service.setVisibility(View.GONE);
                        done.setVisibility(View.GONE);
                    }
                }


                if (!dummy_text.equals(charSequence.toString())) {
                    dummy_text = charSequence.toString();
                    if (!is_selected_place) {
                        if (charSequence.toString().length() > 0) {
                            recycle.setVisibility(View.VISIBLE);
                            cross.setVisibility(View.VISIBLE);
                            search.setVisibility(View.GONE);
                        } else {
                            recycle.setVisibility(View.GONE);
                            cross.setVisibility(View.GONE);
                            search.setVisibility(View.VISIBLE);
                        }
                        resultList = autocomplete1(charSequence.toString(), false);
                    } else {
                        if (charSequence.toString().length() > 0) {
                            cross.setVisibility(View.VISIBLE);
                            search.setVisibility(View.GONE);
                        } else {
                            cross.setVisibility(View.GONE);
                            search.setVisibility(View.VISIBLE);
                        }
                        recycle.setVisibility(View.GONE);
                        is_selected_place = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (savePref.getUserType().equals("1")) {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading_blue).into(imageViewTarget);
        } else {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading).into(imageViewTarget);
        }
    }

    public ArrayList<String[]> autocomplete1(final String input, final boolean show_dialog) {
        if (resultList.size() > 0)
            resultList.clear();
        Thread thread = new Thread() {
            @Override
            public void run() {

                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                try {
                    StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
                    sb.append("?key=" + Utils.API_KEY);
                    sb.append("&location=" + savePref.getLat() + "," + savePref.getLong());
                    sb.append("&types=establishment");
                    sb.append("&radius=1000");
                    sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                    URL url = new URL(sb.toString());

                    System.out.println("URL: " + url);
                    conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    // Load the results into a StringBuilder
                    int read;
                    char[] buff = new char[1024];
                    while ((read = in.read(buff)) != -1) {
                        jsonResults.append(buff, 0, read);
                    }
                } catch (MalformedURLException e) {
                    Log.e("Error", "Error processing Places API URL", e);
//                    return resultList;
                } catch (IOException e) {
                    Log.e("Error", "Error connecting to Places API", e);
//                    return resultList;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

                try {
                    // Create a JSON object hierarchy from the results
                    JSONObject jsonObj = new JSONObject(jsonResults.toString());
                    JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                    // Extract the Place descriptions from the results
//                    resultList = new ArrayList<String[]>(predsJsonArray.length());

                    for (int i = 0; i < predsJsonArray.length(); i++) {
                        String[] placeId = new String[2];
                        placeId[0] = predsJsonArray.getJSONObject(i).getString("description");
                        placeId[1] = predsJsonArray.getJSONObject(i).getString("place_id");
                        resultList.add(placeId);
                        // String value=predsJsonArray.getJSONObject(i).getString("place_id");
                    }

                    notifyAdapter(show_dialog, input);
                } catch (JSONException e) {
                    Log.e("Error", "Cannot process JSON results", e);
                }
            }
        };
        thread.start();
        return resultList;
    }

    private void notifyAdapter(final boolean show_dialog, final String input) {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deatilsAdapter.notifyDataSetChanged();

                        if (resultList.size() == 0 && show_dialog) {
                            new AlertDialog.Builder(context)
                                    .setTitle(getResources().getString(R.string.sorry))
                                    .setMessage(getResources().getString(R.string.no_result_found_for) + " " + input)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // continue with delete
                                        }
                                    })
                                    .show();
                        }
                    }
                });
            }
        }.start();
    }

    public void SetSelectLocation(String select_location, int position) {
        Utils.hideKeyboard(SelectLocationActivity.this);
        loading_text.setText(getResources().getString(R.string.searching_place) + " \n" + select_location);
        loading_layout.setVisibility(View.VISIBLE);
        main_layout.setVisibility(View.GONE);

        is_search = true;
        is_selected_place = true;

        recycle.setVisibility(View.GONE);
        selected_place_name = resultList.get(position)[0];

        edt_search.setText(select_location);
        edt_search.setSelection(edt_search.getText().length());

        String place_id = resultList.get(position)[1];
        Utils.hideKeyboard(SelectLocationActivity.this);
        PlaceDetailsJSONParser(place_id, select_location);
    }


    private void setGooglemapSetting() {
        if (mGoogleMap != null) {
            try {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            } catch (SecurityException ex) {
                ex.printStackTrace();
            }

            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    edt_search.setEnabled(true);
                    edt_search.setFocusable(true);

                    show_pin = true;
                    isMap_move = true;
                    loading_image_pin.setVisibility(View.VISIBLE);
                    Utils.showKeyboard(edt_search, SelectLocationActivity.this);
                    request_service.setVisibility(View.GONE);
                    done.setVisibility(View.VISIBLE);
                    cross.setVisibility(View.VISIBLE);

                    if (currLocationMarker != null) {
                        currLocationMarker.remove();
                        currLocationMarker = null;
                    }

                    onCameraChange();

                    return true;
                }
            });
            if (selectedlat.equals("")) {
                LatLng latlong = new LatLng(Double.parseDouble(savePref.getLat()), Double.parseDouble(savePref.getLong()));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, currentZoom));
            }
            mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition pos) {
                    currentZoom = pos.zoom;
                }
            });


            mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    onCameraChange();

                }

            });
        }

    }

    private void onCameraChange() {
        if (show_pin && isMap_move) {
            if (!is_search) {
                is_selected_place = true;
                final LatLng mPosition = mGoogleMap.getCameraPosition().target;
                final Geocoder geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        List<Address> addresses1 = null;
                        try {
                            addresses1 = geocoder.getFromLocation(mPosition.latitude, mPosition.longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final List<Address> addresses = addresses1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isMap_move) {
                                    if (addresses != null) {
                                        if (addresses.size() > 0) {
                                            selectedlat = String.valueOf(mPosition.latitude);
                                            selectedlong = String.valueOf(mPosition.longitude);
                                            Address address1 = addresses.get(0);
                                            String ad = "";
                                            ad = address1.getAddressLine(0);
                                            /*for (int i = 0; i < address1.getMaxAddressLineIndex(); i++) {
                                                ad = ad + " " + address1.getAddressLine(i);
                                            }*/

                                            edt_search.setText(ad);
                                            edt_search.setSelection(edt_search.getText().length());
                                            request_service.setVisibility(View.GONE);
                                            done.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
                thread.start();
            } else {
                is_selected_place = true;
                is_search = false;
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.request_service:
                if (selectedlat.equals("") || selectedlong.equals("")) {
                    Utils.showSnackBar(view, getResources().getString(R.string.select_service_location), SelectLocationActivity.this);
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("selected_lat", selectedlat);
                    returnIntent.putExtra("selected_long", selectedlong);
                    returnIntent.putExtra("selected_place", edt_search.getText().toString());
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }

                break;

            case R.id.done:
                show_pin = false;
                Utils.hideKeyboard(SelectLocationActivity.this);
                request_service.setVisibility(View.VISIBLE);
                loading_image_pin.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                cross.setVisibility(View.GONE);
                edt_search.setEnabled(true);
                isMap_move = false;
                LatLng selectedPlaceLatLng = new LatLng(Double.parseDouble(selectedlat), Double.parseDouble(selectedlong));

                if (Utils.isConfigRtl(SelectLocationActivity.this))
                    currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_arabic)).snippet("true"));
                else
                    currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_tip)).snippet("true"));

                break;

            case R.id.current_location:
                LatLng coordinate = new LatLng(Double.parseDouble(savePref.getLat()), Double.parseDouble(savePref.getLong()));
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        coordinate, currentZoom);
                mGoogleMap.animateCamera(location);
                break;
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

        Location mLastLocation = null;
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        if (mLastLocation != null) {
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            savePref.setLat(String.valueOf(mLastLocation.getLatitude()));
            savePref.setLong(String.valueOf(mLastLocation.getLongitude()));
            setGooglemapSetting();
        } else {
            showGPSDisabledAlertToUser();
        }

    }


    @Override
    public void onConnectionSuspended(int i) {
//        Toast.makeText(context, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Toast.makeText(context, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        savePref.setLat(String.valueOf(location.getLatitude()));
        savePref.setLong(String.valueOf(location.getLongitude()));
//        getAllPlumberApi();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(getResources().getString(R.string.gps_disable))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.turn_on),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                dialog.cancel();
                            }
                        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    private void PlaceDetailsJSONParser(String place_id, String place_name) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        RequestBody formBody = formBuilder.build();

        StringBuilder sb = new StringBuilder(PLACES_DETAIL_API_BASE + TYPE_DETAIL + OUT_JSON);
        sb.append("?key=" + Utils.API_KEY);
        sb.append("&placeid=" + place_id);

        GetAsync mAsync = new GetAsync(context, sb.toString(), formBody) {
            @Override
            public void getValueParse(String result) {

                loading_layout.setVisibility(View.GONE);
                main_layout.setVisibility(View.VISIBLE);

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObj = new JSONObject(result);
                        JSONObject resultJsonObj = jsonObj.getJSONObject("result");
                        JSONObject geometryJsonObj = resultJsonObj.getJSONObject("geometry");
                        JSONObject locationJsonObj = geometryJsonObj.getJSONObject("location");
                        selectedlat = locationJsonObj.getString("lat");
                        selectedlong = locationJsonObj.getString("lng");
                        LatLng latlng = new LatLng(Double.parseDouble(selectedlat), Double.parseDouble(selectedlong));
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, currentZoom));

                        request_service.setVisibility(View.GONE);
                        done.setVisibility(View.VISIBLE);

                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {

                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute(sb.toString());
    }

}
