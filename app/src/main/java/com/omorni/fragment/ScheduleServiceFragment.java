package com.omorni.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.omorni.activity.MainActivity;
import com.omorni.activity.SellerListActivity;
import com.omorni.adapter.SearchLocationAdapter;
import com.omorni.model.AllPackageResponse;
import com.omorni.model.AllReviewsResponse;
import com.omorni.model.SellerData;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ParameterClass;
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

public class ScheduleServiceFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    View view;
    GoogleMap mGoogleMap;
    SavePref savePref;
    LatLng latLng;
    GoogleApiClient mGoogleApiClient;
    MainActivity context;
    private TextView schedule_service, date, time;
    private String from_time = "", to_time = "";
    private boolean is_from_time = false, is_to_time = false;
    private RelativeLayout date_layout, loading_layout;
    private TextView selected_time_date, edt_pick_up_location;
    private EditText edt_search;
    private ImageView loading_image, set_location;
    private String selectedlat = "", selectedlong = "", selected_place_name = "";
    ArrayList<SellerData> sellerDataArrayList;
    ArrayList<AllPackageResponse> allPackagesArrayList;
    private ImageView search, cross;
    private RecyclerView recycle;
    SearchLocationAdapter deatilsAdapter;
    private ArrayList<String[]> resultList;
    private boolean is_search = false;
    private boolean is_animate = false;
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String PLACES_DETAIL_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_DETAIL = "/details";
    private boolean is_selected_place = true;
    private float currentZoom = 12;
    private String dummy_text = "";
    private String refresh_time = "";
    private ImageView loading_image_pin;
    private boolean show_pin = false, isMap_move = true;
    private TextView done;
    Marker currLocationMarker;
    Date current_date;
    private ImageView current_location;
    private String seller_category = "";
    private TextView loading_text;
    private static String API_KEY = "AIzaSyAf4-r9VPGbx7REk6556tUDFPMPDEPWvIM";
    String selected_lat = "", selected_long = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.e("schedule ", "createview");
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_achedule_service, container, false);
        } catch (InflateException e) {
        }


        seller_category = getArguments().getString(ParameterClass.SELLER_CATEGORY);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (MainActivity) getActivity();
        savePref = new SavePref(context);
        Log.e("schedule ", "activity");
        initialize(view);
        setUpUI();

        if (seller_category.equals(getResources().getString(R.string.plumber_id))) {
            MainActivity.toolbar_title.setText(R.string.plumbering_service);
            MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_1, 0, 0, 0);
        } else if (seller_category.equals(getResources().getString(R.string.electrician_id))) {
            MainActivity.toolbar_title.setText(R.string.electrician_service);
            MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_2, 0, 0, 0);
        } else if (seller_category.equals(getResources().getString(R.string.carpenter_id))) {
            MainActivity.toolbar_title.setText(R.string.carpenter_service);
            MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_3, 0, 0, 0);
        } else if (seller_category.equals(getResources().getString(R.string.ac_id))) {
            MainActivity.toolbar_title.setText(R.string.ac_service);
            MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_4, 0, 0, 0);
        } else if (seller_category.equals(getResources().getString(R.string.satellite_id))) {
            MainActivity.toolbar_title.setText(R.string.satellite_service);
            MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_5, 0, 0, 0);
        } else if (seller_category.equals(getResources().getString(R.string.painter_id))) {
            MainActivity.toolbar_title.setText(R.string.painter_service);
            MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_6, 0, 0, 0);

        }
    }

    private void initialize(View view) {
        edt_search = (EditText) view.findViewById(R.id.edt_search);
        edt_pick_up_location = (TextView) view.findViewById(R.id.edt_pick_up_location);
        schedule_service = (TextView) view.findViewById(R.id.schedule_service);
        date = (TextView) view.findViewById(R.id.date);
        time = (TextView) view.findViewById(R.id.time);
        done = (TextView) view.findViewById(R.id.done);
        date_layout = (RelativeLayout) view.findViewById(R.id.date_layout);
        loading_layout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        selected_time_date = (TextView) view.findViewById(R.id.selected_time_date);
        loading_image = (ImageView) view.findViewById(R.id.loading_image);
        set_location = (ImageView) view.findViewById(R.id.set_location);
        loading_image_pin = (ImageView) view.findViewById(R.id.pin);
        current_location = (ImageView) view.findViewById(R.id.current_location);

        loading_text = (TextView) view.findViewById(R.id.loading_text);

        search = (ImageView) view.findViewById(R.id.search);
        cross = (ImageView) view.findViewById(R.id.cross);
        recycle = (RecyclerView) view.findViewById(R.id.recycle);
        if (savePref.getUserType().equals("1")) {
            schedule_service.setBackground(getResources().getDrawable(R.drawable.green_requesty_seller_drawable));
        } else {
            schedule_service.setBackground(getResources().getDrawable(R.drawable.green_requesty_drawable));
        }

        schedule_service.setOnClickListener(this);
        date.setOnClickListener(this);
        time.setOnClickListener(this);
        done.setOnClickListener(this);
        current_location.setOnClickListener(this);

        date_layout.setVisibility(View.VISIBLE);
        loading_layout.setVisibility(View.GONE);

        resultList = new ArrayList<String[]>();
        deatilsAdapter = new SearchLocationAdapter(getActivity(), resultList, ScheduleServiceFragment.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(linearLayoutManager);
        recycle.setAdapter(deatilsAdapter);

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

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        String formattedDate = formatter.format(c.getTime());

        try {
            current_date = (Date) formatter.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (savePref.getUserType().equals("1")) {
            selected_time_date.setTextColor(getResources().getColor(R.color.colorPrimarySeller));
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading_blue).into(imageViewTarget);

        } else {
            selected_time_date.setTextColor(getResources().getColor(R.color.colorPrimary));
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading).into(imageViewTarget);
        }


        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Utils.hideKeyboard(getActivity());
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
                    Utils.showKeyboard(edt_search, getActivity());

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

                Log.e("results", "result  " + resultList);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
                    sb.append("&radius=2000");
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
                        Log.e("results", "results " + predsJsonArray.getJSONObject(i).getString("description") + "  " + predsJsonArray.getJSONObject(i).getString("place_id"));
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deatilsAdapter.notifyDataSetChanged();
                            if (resultList.size() == 0 && show_dialog) {
                                new android.support.v7.app.AlertDialog.Builder(context)
                                        .setTitle("Sorry")
                                        .setMessage("No result found for " + input)
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
            }
        }.start();
    }

    public void SetSelectLocation(String select_location, int position) {
        Utils.hideKeyboard(getActivity());
        if (savePref.getUserType().equals("1"))
            loading_text.setTextColor(ContextCompat.getColor(context, R.color.colorPrimarySeller));
        else
            loading_text.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        loading_text.setText(getActivity().getResources().getString(R.string.searching_place) + " \n" + select_location);
        loading_layout.setVisibility(View.VISIBLE);
        date_layout.setVisibility(View.GONE);

        is_search = true;
        is_selected_place = true;

        recycle.setVisibility(View.GONE);
        selected_place_name = select_location;
        edt_search.setText(select_location);
        edt_search.setSelection(edt_search.getText().length());

        String place_id = resultList.get(position)[1];
        Utils.hideKeyboard(getActivity());
        PlaceDetailsJSONParser(place_id, select_location);
    }

    private void setUpUI() {
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
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
                Utils.showKeyboard(edt_search, getActivity());
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
                Log.e("zoom", "zoom " + currentZoom);
            }
        });

    }

    private void onCameraChange() {
        if (show_pin && isMap_move) {
            if (!is_search) {
                is_selected_place = true;
                final LatLng mPosition = mGoogleMap.getCameraPosition().target;
                final Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

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

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
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
                                            }*/

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
    public void onResume() {
        super.onResume();

        if (Utils.isGpsOn(getActivity())) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else {
            showGPSDisabledAlertToUser();
        }
    }


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
            LatLng latlong = new LatLng(Double.parseDouble(savePref.getLat()), Double.parseDouble(savePref.getLong()));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlong, currentZoom);
            mGoogleMap.moveCamera(cameraUpdate);
            set_location.setVisibility(View.GONE);
        } else {
            showGPSDisabledAlertToUser();
        }
    }


    private void PlaceDetailsJSONParser(String place_id, String place_name) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        RequestBody formBody = formBuilder.build();

        StringBuilder sb = new StringBuilder(PLACES_DETAIL_API_BASE + TYPE_DETAIL + OUT_JSON);
        sb.append("?key=" + Utils.API_KEY);
        sb.append("&placeid=" + place_id);

        GetAsync mAsync = new GetAsync(getActivity(), sb.toString(), formBody) {
            @Override
            public void getValueParse(String result) {
                loading_layout.setVisibility(View.GONE);
                date_layout.setVisibility(View.VISIBLE);

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
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(getActivity().getResources().getString(R.string.gps_disable))
                .setCancelable(false)
                .setPositiveButton(getActivity().getResources().getString(R.string.turn_on),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                dialog.cancel();
                            }
                        });
        alertDialogBuilder.setNegativeButton(getActivity().getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        android.support.v7.app.AlertDialog alert = alertDialogBuilder.create();
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
                loading_image_pin.setVisibility(View.GONE);
                if (selectedlat.equals("")) {
                    Utils.showSnackBar(view, getActivity().getResources().getString(R.string.select_service_location), getActivity());
                } else if (date.getText().toString().equals("")) {
                    Utils.showSnackBar(view, getResources().getString(R.string.select_date), getActivity());
                } else if (time.getText().toString().equals("")) {
                    Utils.showSnackBar(view, getResources().getString(R.string.select_time), getActivity());
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
                            Utils.showSnackBar(view, getActivity().getResources().getString(R.string.start_time_error), context);
                        } else if (Integer.parseInt(start_time) > Integer.parseInt(end_time)) {
                            Utils.showSnackBar(view, getActivity().getResources().getString(R.string.end_time_error), getActivity());
                        } else {
                            getAllPlumberScheduleApi(selectedlat, selectedlong);
                        }
                    } else {
                        if (Integer.parseInt(start_time) > Integer.parseInt(end_time)) {
                            Utils.showSnackBar(view, getActivity().getResources().getString(R.string.end_time_error), getActivity());
                        } else {
                            getAllPlumberScheduleApi(selectedlat, selectedlong);
                        }
                    }
                }
                break;

            case R.id.done:
                show_pin = false;
                Utils.hideKeyboard(getActivity());
                schedule_service.setVisibility(View.VISIBLE);
                done.setVisibility(View.GONE);
                cross.setVisibility(View.GONE);
                loading_image_pin.setVisibility(View.GONE);
                isMap_move = false;

                LatLng selectedPlaceLatLng = new LatLng(Double.parseDouble(selectedlat), Double.parseDouble(selectedlong));
                if (Utils.isConfigRtl(getActivity()))
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


    private void getAllPlumberScheduleApi(final String lat, final String lng) {
        selected_lat = lat;
        selected_long = lng;
        if (savePref.getUserType().equals("1"))
            loading_text.setTextColor(ContextCompat.getColor(context, R.color.colorPrimarySeller));
        else
            loading_text.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        if (seller_category.equals(getResources().getString(R.string.plumber_id))) {
            loading_text.setText(getResources().getString(R.string.searching_plumbers));
        } else if (seller_category.equals(getResources().getString(R.string.electrician_id))) {
            loading_text.setText(getResources().getString(R.string.searching_electrician));
        } else if (seller_category.equals(getResources().getString(R.string.carpenter_id))) {
            loading_text.setText(getResources().getString(R.string.searching_carpeneter));
        } else if (seller_category.equals(getResources().getString(R.string.ac_id))) {
            loading_text.setText(getResources().getString(R.string.searching_ac));
        } else if (seller_category.equals(getResources().getString(R.string.satellite_id))) {
            loading_text.setText(getResources().getString(R.string.searching_satellite));
        } else if (seller_category.equals(getResources().getString(R.string.painter_id))) {
            loading_text.setText(getResources().getString(R.string.searching_painter));
        }

        selected_time_date.setText(getActivity().getResources().getString(R.string.on) + " " + date.getText().toString() + " " + getActivity().getResources().getString(R.string.on) + " " + from_time + " " + getActivity().getResources().getString(R.string.and) + " " + to_time);

        date_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.LATTITUDE, lat);
        formBuilder.add(AllOmorniParameters.LONGITUDE, lng);
        formBuilder.add(AllOmorniParameters.START_DATE, date.getText().toString());
        formBuilder.add(AllOmorniParameters.START_TIME, from_time);
        formBuilder.add(AllOmorniParameters.END_TIME, to_time);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.SELLER_CATEGORY, seller_category);

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.NEAR_ME_SCHEDULE, formBody) {
            @Override
            public void getValueParse(String result) {

                if (result != null && !result.equalsIgnoreCase("")) {

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        sellerDataArrayList = new ArrayList<SellerData>();
                        if (status.getString("code").equalsIgnoreCase("1")) {
//                            refresh_time = status.getString("search_refresh_time");
                            try {
                                mGoogleMap.setMyLocationEnabled(true);
                                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
//                                mGoogleMap.animateCamera(cameraUpdate);
                            } catch (SecurityException ex) {
                                Log.e("exception", "exception " + ex.getMessage());
                                ex.printStackTrace();
                            }
                            JSONArray object = jsonObject.getJSONArray("body");
                            for (int i = 0; i < object.length(); i++) {
                                JSONObject object1 = object.getJSONObject(i);
                                SellerData sellerData = new SellerData();
                                allPackagesArrayList = new ArrayList<>();
                                ArrayList<AllReviewsResponse> arrayListAllReviews = new ArrayList<AllReviewsResponse>();
                                sellerData.setSellerId(object1.getString("id"));
                                sellerData.setFirst_name(object1.getString("first_name"));
                                sellerData.setLast_name(object1.getString("last_name"));
                                sellerData.setEmail(object1.getString("email"));
                                sellerData.setMobile(object1.getString("mobile"));
                                sellerData.setLocation(object1.getString("location"));
                                sellerData.setJob_description(object1.getString("job_description"));
                                sellerData.setService_title(object1.getString("service_title"));
                                sellerData.setUser_image(object1.getString("user_image"));
                                sellerData.setIs_verify(object1.getString("is_verify"));
                                sellerData.setUser_type(object1.getString("user_type"));
                                sellerData.setStatus(object1.getString("status"));
                                sellerData.setCreated_date(object1.getString("created_date"));
                                sellerData.setReq_lat(selectedlat);
                                sellerData.setReq_lng(selectedlong);
                                String lat = object1.getString("latitude");
                                String longi = object1.getString("longitude");
                                sellerData.setLatitude(lat);
                                sellerData.setLongitude(longi);
                                sellerData.setOn_duty(object1.getString("on_duty"));
                                sellerData.setDistance(object1.getString("distance"));
                                sellerData.setFavourite(object1.getString("favourite"));
                                sellerData.setRating(object1.getString("avgrating"));
                                sellerData.setVat_tax(object1.getString("vat_tax"));

                                JSONArray jsonArray = object1.getJSONArray("language");
                                String language = "";

                                if (jsonArray.length() > 0) {
                                    for (int j = 0; j < jsonArray.length(); j++) {
                                        JSONObject jsonObjectLanguage = jsonArray.getJSONObject(j);
                                        language += jsonObjectLanguage.getString("language") + " " + jsonObjectLanguage.getString("language_level") + ",";
                                    }
                                    sellerData.setLanguage(language.substring(0, language.length() - 1));
                                } else {
                                    sellerData.setLanguage(language);
                                }
                                sellerData.setOmorni_processing_fee(object1.getString("omorni_processing_fee"));
                                sellerData.setTotal_rating_user(object1.getString("total_rating_user"));
                                sellerData.setSeller_category(object1.getString("category"));

                                JSONArray arrayAllreviews = object1.getJSONArray("allreviews");
                                if (arrayAllreviews.length() > 0) {
                                    for (int j = 0; j < arrayAllreviews.length(); j++) {
                                        AllReviewsResponse allReviewsResponse = new AllReviewsResponse();
                                        JSONObject jsonObject1 = arrayAllreviews.getJSONObject(j);
                                        allReviewsResponse.setFirst_name(jsonObject1.getString("first_name"));
                                        allReviewsResponse.setLast_name(jsonObject1.getString("last_name"));
                                        allReviewsResponse.setRating(jsonObject1.getString("rating"));
                                        allReviewsResponse.setComment(jsonObject1.getString("comment"));
                                        allReviewsResponse.setUser_image(jsonObject1.getString("user_image"));
                                        allReviewsResponse.setRating_date(jsonObject1.getString("review_date"));
                                        arrayListAllReviews.add(allReviewsResponse);
                                    }
                                }

                                sellerData.setAllreviews(arrayListAllReviews);

                                JSONArray package_array = object1.getJSONArray("allpackage");

                                for (int j = 0; j < package_array.length(); j++) {
                                    AllPackageResponse allpackages = new AllPackageResponse();
                                    JSONObject package_obj = package_array.getJSONObject(j);
                                    allpackages.setId(package_obj.getString("id"));
                                    allpackages.setNormal_charges(package_obj.getString("normal_charges"));
                                    allpackages.setAdditional_charges(package_obj.getString("additional_charges"));
                                    allpackages.setStatus(package_obj.getString("status"));
                                    allpackages.setPackage_name(package_obj.getString("package_name"));
                                    allpackages.setPackage_description(package_obj.getString("package_description"));
                                    allpackages.setMain_hours(package_obj.getString("main_hours"));
                                    allpackages.setExtra_hours(package_obj.getString("extra_hours"));
                                    allpackages.setPackage_status(package_obj.getString("package_status"));
                                    allpackages.setDescription(package_obj.getString("description"));
                                    allPackagesArrayList.add(allpackages);
                                }
                                sellerData.setAllPackages(allPackagesArrayList);
                                sellerDataArrayList.add(sellerData);

                                // Add a marker in Map.
//                                LatLng sellerLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
//                                mGoogleMap.addMarker(new MarkerOptions().position(sellerLatLng).title(sellerData.getFirst_name() + ", " + sellerData.getLocation()));
                            }

                            Intent intent = new Intent(getActivity(), SellerListActivity.class);
                            intent.putParcelableArrayListExtra("seller_data", sellerDataArrayList);
                            intent.putExtra("service_now", false);
                            intent.putExtra(ParameterClass.SELLER_CATEGORY, seller_category);
                            intent.putExtra("date", date.getText().toString());
                            intent.putExtra("time", time.getText().toString());
                            intent.putExtra("selected_lat", selected_lat);
                            intent.putExtra("selected_long", selected_long);
                            intent.putExtra("start_time", from_time);
                            intent.putExtra("end_time", to_time);
                            startActivity(intent);
                            ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            date_layout.setVisibility(View.VISIBLE);
                            loading_layout.setVisibility(View.GONE);
                        } else {
                            Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(view, getResources().getString(R.string.internet_error), context);
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
                Utils.showToast(getActivity(), getActivity().getResources().getString(R.string.previous_date_error));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void openDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(ScheduleServiceFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setThemeDark(false);
        dpd.vibrate(true);

        dpd.dismissOnPause(true);
        dpd.showYearPickerFirst(false);
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }

    private void openTimePickerStartDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.time_picker_dialog);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView ok = (TextView) dialog.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        final TimePicker tp = (TimePicker) dialog.findViewById(R.id.tp);
        title.setText(getActivity().getResources().getString(R.string.start_time));
        if (savePref.getUserType().equals("1")) {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimarySeller));
        } else {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        tp.setIs24HourView(true);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    from_time = String.format("%02d:%02d", tp.getHour(), tp.getMinute());
                else
                    from_time = String.format("%02d:%02d", tp.getCurrentHour(), tp.getCurrentMinute());

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
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.time_picker_dialog);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView ok = (TextView) dialog.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        title.setText(getActivity().getResources().getString(R.string.end_time));

        if (savePref.getUserType().equals("1")) {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimarySeller));
        } else {
            title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        final TimePicker tp = (TimePicker) dialog.findViewById(R.id.tp);
        tp.setIs24HourView(true);

        /*tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hours, int minute) {
                to_time = hours + ":" + minute;
            }
        });*/

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

    /*private void openTimePickerFrom() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(ScheduleServiceFragment.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        tpd.setThemeDark(false);
        tpd.vibrate(true);
        tpd.setTitle(getResources().getString(R.string.from));
        tpd.dismissOnPause(false);
        tpd.enableSeconds(false);
        tpd.setVersion(TimePickerDialog.Version.VERSION_1);
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
            }
        });
        tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
        is_from_time = true;
    }*/


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

   /* private void openTimePickerTo() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(ScheduleServiceFragment.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true
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
        tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
        is_to_time = true;
    }*/
}
