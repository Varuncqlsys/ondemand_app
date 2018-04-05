package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

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
import com.omorni.adapter.SearchLocationAdapterScheduleActivity;
import com.omorni.model.SellerData;
import com.omorni.model.SellerDetailResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by V on 2/28/2017.
 */

public class ScheduleServiceMapActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    GoogleMap mGoogleMap;
    SavePref savePref;
    LatLng latLng;
    GoogleApiClient mGoogleApiClient;
    ScheduleServiceMapActivity context;
    private TextView schedule_service, date, time, edt_pick_up_location;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String from_time = "", to_time = "";
    private boolean is_from_time = false, is_to_time = false;
    private ArrayList<SellerData> sellerList;
    private RelativeLayout date_layout;
    private TextView selected_time_date;
    private ImageView set_location;
    private String selectedlat = "", selectedlong = "", selected_place_name = "";
    private RelativeLayout loading_layout;
    private RelativeLayout main_layout;

    private ImageView loading_image;
    private SellerDetailResponse sellerDetailResponse;
    CountDownTimer countDownTimer;
    SearchLocationAdapterScheduleActivity deatilsAdapter;
    private ArrayList<String[]> resultList;
    private boolean is_search = false;
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String PLACES_DETAIL_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_DETAIL = "/details";
    private boolean is_selected_place = true;
    private ImageView search, cross;
    private RecyclerView recycle;
    private float currentZoom = 12;
    private String dummy_text = "";
    private EditText edt_search;
    private boolean is_animate = false;
    private TextView done;
    private boolean show_pin = false, isMap_move = true, setTimer = true;
    private ImageView loading_image_pin;
    Marker currLocationMarker;
    Date current_date;
    private ImageView current_location;
    private TextView loading_text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        savePref = new SavePref(context);
        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_achedule_service_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_achedule_service);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.NOTIFICATION_ACCEPTED_JOB));
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
        toolbar_title.setText(R.string.set_schedule_time);
    }

    private void initialize() {
        sellerDetailResponse = getIntent().getParcelableExtra("selected_package");
        edt_search = (EditText) findViewById(R.id.edt_search);
        edt_pick_up_location = (TextView) findViewById(R.id.edt_pick_up_location);
        schedule_service = (TextView) findViewById(R.id.schedule_service);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        date_layout = (RelativeLayout) findViewById(R.id.date_layout);
        selected_time_date = (TextView) findViewById(R.id.selected_time_date);
        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        done = (TextView) findViewById(R.id.done);
        loading_text = (TextView) findViewById(R.id.loading_text);

        loading_image = (ImageView) findViewById(R.id.loading_image);
        set_location = (ImageView) findViewById(R.id.set_location);
        search = (ImageView) findViewById(R.id.search);
        cross = (ImageView) findViewById(R.id.cross);
        loading_image_pin = (ImageView) findViewById(R.id.pin);
        recycle = (RecyclerView) findViewById(R.id.recycle);
        current_location = (ImageView) findViewById(R.id.current_location);

        schedule_service.setOnClickListener(this);
        date.setOnClickListener(this);
        time.setOnClickListener(this);
        done.setOnClickListener(this);
        current_location.setOnClickListener(this);


        date_layout.setVisibility(View.VISIBLE);
        loading_layout.setVisibility(View.GONE);


        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        String formattedDate = formatter.format(c.getTime());
        try {
            current_date = (Date) formatter.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        resultList = new ArrayList<String[]>();
        deatilsAdapter = new SearchLocationAdapterScheduleActivity(ScheduleServiceMapActivity.this, resultList, true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ScheduleServiceMapActivity.this, LinearLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(linearLayoutManager);
        recycle.setAdapter(deatilsAdapter);


        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Utils.hideKeyboard(ScheduleServiceMapActivity.this);
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
                schedule_service.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                loading_image_pin.setVisibility(View.VISIBLE);
                edt_search.setText("");
                show_pin = true;
            }
        });

        edt_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    show_pin = true;
                    edt_search.setEnabled(true);
                    edt_search.setFocusable(true);
                    isMap_move = true;
                    schedule_service.setVisibility(View.GONE);

                    if (loading_image_pin.getVisibility() == View.GONE) {
                        onCameraChange();
                        done.setVisibility(View.VISIBLE);
                        cross.setVisibility(View.VISIBLE);
                        search.setVisibility(View.GONE);
                    }

                    loading_image_pin.setVisibility(View.VISIBLE);
                    Utils.showKeyboard(edt_search, ScheduleServiceMapActivity.this);

                    if (currLocationMarker != null)
                        currLocationMarker.remove();

                }
                return false;
            }
        });


        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                if (charSequence.toString().length() > 0) {
                    show_pin = true;
                    loading_image_pin.setVisibility(View.VISIBLE);
                    schedule_service.setVisibility(View.GONE);
                } else {
                    show_pin = true;
                    loading_image_pin.setVisibility(View.VISIBLE);
                    schedule_service.setVisibility(View.GONE);
                    done.setVisibility(View.GONE);
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

                    for (int i = 0; i < predsJsonArray.length(); i++) {
                        String[] placeId = new String[2];
                        placeId[0] = predsJsonArray.getJSONObject(i).getString("description");
                        placeId[1] = predsJsonArray.getJSONObject(i).getString("place_id");
                        resultList.add(placeId);
                        // String value=predsJsonArray.getJSONObject(i).getString("place_id");
                    }

                    notifyAdapter(show_dialog, input);
                    Log.e("results", "result  " + resultList);
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
                            new android.support.v7.app.AlertDialog.Builder(context)
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
        Utils.hideKeyboard(ScheduleServiceMapActivity.this);
        loading_text.setText(getResources().getString(R.string.searching_place) + " \n" + select_location);
        loading_layout.setVisibility(View.VISIBLE);
        main_layout.setVisibility(View.GONE);

        is_search = true;
        is_selected_place = true;

        recycle.setVisibility(View.GONE);
        selected_place_name = select_location;

        edt_search.setText(select_location);
        edt_search.setSelection(edt_search.getText().length());

        String place_id = resultList.get(position)[1];
        Utils.hideKeyboard(ScheduleServiceMapActivity.this);
        PlaceDetailsJSONParser(place_id, select_location);
    }

    private void PlaceDetailsJSONParser(String place_id, String place_name) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        RequestBody formBody = formBuilder.build();

        StringBuilder sb = new StringBuilder(PLACES_DETAIL_API_BASE + TYPE_DETAIL + OUT_JSON);
        sb.append("?key=" + Utils.API_KEY);
        sb.append("&placeid=" + place_id);

        GetAsync mAsync = new GetAsync(ScheduleServiceMapActivity.this, sb.toString(), formBody) {
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
                        LatLng latlong = new LatLng(Double.parseDouble(selectedlat), Double.parseDouble(selectedlong));
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, currentZoom));

                        schedule_service.setVisibility(View.GONE);
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

    private void setUpUI() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    private void setGoogleMapSetting() {
        try {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        LatLng latlong = new LatLng(Double.parseDouble(savePref.getLat()), Double.parseDouble(savePref.getLong()));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, currentZoom));

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                edt_search.setEnabled(true);
                edt_search.setFocusable(true);

                show_pin = true;
                isMap_move = true;
                loading_image_pin.setVisibility(View.VISIBLE);
                Utils.showKeyboard(edt_search, ScheduleServiceMapActivity.this);
                schedule_service.setVisibility(View.GONE);
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


        mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                onCameraChange();
            }
        });

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition pos) {
                currentZoom = pos.zoom;
            }
        });
    }

    private void onCameraChange() {
        if (show_pin && isMap_move) {
            if (!is_search) {
                is_selected_place = true;
                final LatLng mPosition = mGoogleMap.getCameraPosition().target;
                final Geocoder geocoder = new Geocoder(context, Locale.getDefault());

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
                                        selectedlat = String.valueOf(mPosition.latitude);
                                        selectedlong = String.valueOf(mPosition.longitude);

                                        Address address1 = addresses.get(0);
                                        String ad = "";
                                        ad = address1.getAddressLine(0);
                                        /*for (int i = 0; i < address1.getMaxAddressLineIndex(); i++) {
                                            ad = ad + " " + address1.getAddressLine(i);
                                        }
*/
                                        edt_pick_up_location.setText(ad);
                                        edt_search.setText(ad);
                                        edt_search.setSelection(edt_search.getText().length());

                                        schedule_service.setVisibility(View.GONE);
                                        done.setVisibility(View.VISIBLE);
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
                edt_pick_up_location.setText(selected_place_name);
            }
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        savePref.setUserOnlineForPayment(true);
        savePref.setSellerId(sellerDetailResponse.getId());
        if (Utils.isGpsOn(context)) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else {
            showGPSDisabledAlertToUser();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("status").equals("4")) {
                Utils.showToast(context, "Seller accepted your job. Please proceed to pay");
                Intent intent2 = new Intent(context, RequestSummaryActivity.class);
                intent2.putExtra("selected_package", getIntent().getStringExtra("package"));
                intent2.putExtra("service_now", true);
                intent2.putExtra("start_date", intent.getStringExtra("start_date"));
                intent2.putExtra("start_time", intent.getStringExtra("start_time"));
                intent2.putExtra("is_scheduled", intent.getStringExtra("is_scheduled"));
                intent2.putExtra("req_id", intent.getStringExtra("req_id"));
                intent2.putExtra("selected_seller", sellerDetailResponse);
                intent2.putExtra("from_sellerdetail", true);
                intent2.putExtra("req_type", intent.getStringExtra("req_type"));
                startActivity(intent2);
                main_layout.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            } else if (intent.getStringExtra("status").equals("5")) {
                Utils.showToast(context, getResources().getString(R.string.seller_rejected_job));
                finish();
            }
        }
    };

    protected synchronized void buildGoogleApiClient() {
//        Toast.makeText(context, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
            setGoogleMapSetting();

            set_location.setVisibility(View.GONE);
        } else {
            showGPSDisabledAlertToUser();
        }
    }


    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */


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
        Log.e("lat", "long " + latLng + " " + location.getLatitude() + " " + location.getLongitude());
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
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.date:
                openDatePicker();
                break;
            case R.id.time:
                openTimePickerStartDialog();
//                openTimePickerFrom();
                break;
            case R.id.schedule_service:
                if (selectedlat.equals("")) {
                    Utils.showSnackBar(view, getResources().getString(R.string.set_service_location), context);
                } else if (date.getText().toString().equals("")) {
                    Utils.showSnackBar(view, getResources().getString(R.string.select_date), context);
                } else if (time.getText().toString().equals("")) {
                    Utils.showSnackBar(view, getResources().getString(R.string.select_time), context);
                } else {

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date departure_date_selected = null;
                    try {
                        departure_date_selected = sdf.parse(date.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String start_time = from_time.replace(":", "");
                    String end_time = to_time.replace(":", "");
                    if (departure_date_selected.compareTo(current_date) == 0) {
                        Calendar c = Calendar.getInstance();

                        int minutes = c.get(Calendar.MINUTE);
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        String currenttime = String.valueOf(hour) + String.valueOf(minutes);

                        if (Integer.parseInt(start_time) < Integer.parseInt(currenttime)) {
                            Utils.showSnackBar(view, getResources().getString(R.string.start_time_error), context);
                        } else if (Integer.parseInt(start_time) > Integer.parseInt(end_time)) {
                            Utils.showSnackBar(view, getResources().getString(R.string.end_time_error), context);
                        } else {
                            checkSellerFreeApi();
                        }
                    } else {
                        if (Integer.parseInt(start_time) > Integer.parseInt(end_time)) {
                            Utils.showSnackBar(view, getResources().getString(R.string.end_time_error), context);
                        } else {
                            checkSellerFreeApi();
                        }
                    }
                }
                break;

            case R.id.done:

                Utils.hideKeyboard(ScheduleServiceMapActivity.this);
                schedule_service.setVisibility(View.VISIBLE);
                done.setVisibility(View.GONE);
                cross.setVisibility(View.GONE);
                loading_image_pin.setVisibility(View.GONE);
                edt_search.setEnabled(true);
                isMap_move = false;
                LatLng selectedPlaceLatLng = new LatLng(Double.parseDouble(selectedlat), Double.parseDouble(selectedlong));

                if (Utils.isConfigRtl(ScheduleServiceMapActivity.this))
                    currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_arabic)));
                else
                    currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_tip)));

                break;

            case R.id.current_location:
                LatLng coordinate = new LatLng(Double.parseDouble(savePref.getLat()), Double.parseDouble(savePref.getLong()));
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        coordinate, currentZoom);
                mGoogleMap.animateCamera(location);
                break;
        }
    }


    private void checkSellerFreeApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SELLER_ID, sellerDetailResponse.getId());
        formBuilder.add(AllOmorniParameters.START_DATE, date.getText().toString());
        formBuilder.add(AllOmorniParameters.START_TIME, from_time);
        formBuilder.add(AllOmorniParameters.IS_SCHEDULED, "1");
        formBuilder.add(AllOmorniParameters.CREATED_DATE, String.valueOf(System.currentTimeMillis() / 1000));
        formBuilder.add(AllOmorniParameters.SELER_PKG_ID, getIntent().getStringExtra("package_id"));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(ScheduleServiceMapActivity.this, AllOmorniApis.CHECK_SELLER_FREE, formBody) {
            @Override
            public void getValueParse(String result) {
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            if (jsonObject1.getString("on_duty").equals("1") && jsonObject1.getString("is_free").equals("0")) {
                                confirmOrderApi();
                            } else {
                                if (jsonObject1.getString("on_duty").equals("0")) {
                                    Utils.showToast(context, sellerDetailResponse.getFirst_name() + " " + getResources().getString(R.string.off_duty));
                                } else {
                                    Utils.showToast(context, sellerDetailResponse.getFirst_name() + " " + getResources().getString(R.string.not_free));
                                }
                            }
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
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
        mAsync.execute();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String month = String.valueOf(monthOfYear + 1);
        String day = String.valueOf(dayOfMonth);
        if (month.length() == 1)
            month = "0" + month;
        if (day.length() == 1)
            day = "0" + day;

        String date_text = day + "-" + month + "-" + String.valueOf(year);

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date selected_date = (Date) formatter.parse(date_text);

            Calendar c = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = df.format(c.getTime());

            Date current_date = (Date) formatter.parse(formattedDate);

            if (selected_date.compareTo(current_date) > 0) {
                date.setText(date_text);
            } else if (selected_date.compareTo(current_date) == 0) {
                date.setText(date_text);
            } else if (selected_date.compareTo(current_date) < 0) {
                Utils.showToast(context, getResources().getString(R.string.previous_date_error));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private void openDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(ScheduleServiceMapActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setThemeDark(false);
        dpd.vibrate(true);
        dpd.dismissOnPause(true);
        dpd.showYearPickerFirst(false);
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(context.getFragmentManager(), "Datepickerdialog");
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePref.setUserOnlineForPayment(false);
        savePref.setSellerId("");
    }

    private void openTimePickerStartDialog() {
        final Dialog dialog = new Dialog(ScheduleServiceMapActivity.this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.time_picker_dialog);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView ok = (TextView) dialog.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        TimePicker tp = (TimePicker) dialog.findViewById(R.id.tp);
        title.setText(getResources().getString(R.string.start_time));
        if (savePref.getUserType().equals("1")) {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimarySeller));
        } else {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        tp.setIs24HourView(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            from_time = String.format("%02d:%02d", tp.getHour(), tp.getMinute());
        else
            from_time = String.format("%02d:%02d", tp.getCurrentHour(), tp.getCurrentMinute());


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                openTimePickerEndDialog();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        dialog.show();

        is_from_time = true;
    }

    private void openTimePickerEndDialog() {
        final Dialog dialog = new Dialog(ScheduleServiceMapActivity.this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.time_picker_dialog);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView ok = (TextView) dialog.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        title.setText(getResources().getString(R.string.end_time));
        if (savePref.getUserType().equals("1")) {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimarySeller));
        } else {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        final TimePicker tp = (TimePicker) dialog.findViewById(R.id.tp);
        tp.setIs24HourView(true);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    to_time = String.format("%02d:%02d", tp.getHour(), tp.getMinute());
                } else
                    to_time = String.format("%02d:%02d", tp.getCurrentHour(), tp.getCurrentMinute());

                time.setText(from_time + "-" + to_time);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }



    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minuteOfDay, int second) {
       /* String hours = String.valueOf(hourOfDay);
        String minute = String.valueOf(minuteOfDay);
        if (hours.length() == 1)
            hours = "0" + hours;
        if (minute.length() == 1)
            minute = "0" + minute;
//        from_time = hours + ":" + minute;
//        time.setText(from_time);
        if (is_from_time) {
            openTimePickerTo();
            from_time = hours + ":" + minute;
            is_from_time = false;
        } else if (is_to_time) {
            to_time = hours + ":" + minute;
            is_to_time = false;
            time.setText(from_time + "-" + to_time);
        }*/

    }

    /*private void openTimePickerTo() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(ScheduleServiceMapActivity.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true
        );
        tpd.setThemeDark(false);
        tpd.vibrate(true);
        tpd.setTitle(getResources().getString(R.string.to));
        tpd.dismissOnPause(false);
        tpd.enableSeconds(false);
        tpd.setVersion(TimePickerDialog.Version.VERSION_1);
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
            }
        });
        tpd.show(getFragmentManager(), "Timepickerdialog");
        is_to_time = true;
    }*/

    private void confirmOrderApi() {
        main_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        loading_text.setText(getResources().getString(R.string.we_are_contacting)+" " + sellerDetailResponse.getFirst_name() + " " + sellerDetailResponse.getLast_name() + " \n "+getResources().getString(R.string.please_wait));

        if (savePref.getUserType().equals("1")) {
            loading_text.setTextColor(getResources().getColor(R.color.colorPrimarySeller));
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading_blue).into(imageViewTarget);

        } else {
            loading_text.setTextColor(getResources().getColor(R.color.colorPrimary));
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading).into(imageViewTarget);
        }

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.BUYER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.SELLER_ID, sellerDetailResponse.getId());
        formBuilder.add(AllOmorniParameters.SELER_PKG_ID, getIntent().getStringExtra("package_id"));
        formBuilder.add(AllOmorniParameters.REQUEST_LAT, selectedlat);
        formBuilder.add(AllOmorniParameters.REQUEST_LONG, selectedlong);
        formBuilder.add(AllOmorniParameters.CREATED_DATE, String.valueOf(System.currentTimeMillis() / 1000));
        formBuilder.add(AllOmorniParameters.START_DATE, date.getText().toString());
        formBuilder.add(AllOmorniParameters.START_TIME, from_time);
        formBuilder.add(AllOmorniParameters.END_TIME, to_time);
        formBuilder.add(AllOmorniParameters.IS_SCHEDULED, "1");
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(ScheduleServiceMapActivity.this, AllOmorniApis.ADD_REQUEST, formBody) {
            @Override
            public void getValueParse(String result) {

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            long req_time = Long.parseLong(jsonObject1.getString("request_waiting_time"));

                            /*countDownTimer = new CountDownTimer(req_time * 1000, 1000) {

                                public void onTick(long millisUntilFinished) {

                                }

                                public void onFinish() {
                                    Utils.showToast(context, "No response from seller. So please select any other seller");
                                    finish();
                                }

                            }.start();*/
                            Utils.showToast(context, status.getString("message"));

                        } else {
                            Utils.checkAuthToken(ScheduleServiceMapActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
                        }
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
        mAsync.execute();
    }
}
