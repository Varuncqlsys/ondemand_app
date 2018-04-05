package com.omorni.fragment;


import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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

import com.omorni.activity.SellerDetailActivity;
import com.omorni.activity.SellerListActivity;
import com.omorni.adapter.SearchLocationAdapter;
import com.omorni.model.AllPackageResponse;
import com.omorni.model.AllReviewsResponse;
import com.omorni.model.SellerData;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.ParameterClass;
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
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.RequestBody;


public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private TextView request_service, schedule_service, edt_pick_up_location;
    View view;
    MainActivity context;
    SavePref savePref;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    LatLng latLng;
    LocationManager locationManager;
    GoogleMap mGoogleMap;
    Marker currLocationMarker;
    ArrayList<SellerData> sellerDataArrayList;
    ArrayList<AllPackageResponse> allPackagesArrayList;
    private static final long MIN_TIME = 100;
    private static final float MIN_DISTANCE = 1000;
    private RelativeLayout loading_layout, search_layout;
    //    AutoCompleteTextView autocomplete_text;
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String PLACES_DETAIL_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_DETAIL = "/details";
    private ArrayList<String[]> resultList;
    private String selectedlat = "", selectedlong = "", selected_place_name = "";
    private boolean is_search = false, is_selected_place = true;
    private boolean is_animate = false;
    private float currentZoom = 12;
    private RecyclerView recycle;
    private EditText edt_search;
    SearchLocationAdapter deatilsAdapter;
    private ImageView search, cross;
    private String dummy_text = "";
    private String refresh_time = "";
    private ImageView loading_image_pin;
    private boolean show_pin = false;
    Timer timer = null;
    String refreshTime = "";
    private boolean isFirstTime = true;
    private LinearLayout request_schedule_layout;
    private TextView done;
    private Boolean isMap_move = true, setTimer = true;
    private static String API_KEY = "AIzaSyAf4-r9VPGbx7REk6556tUDFPMPDEPWvIM";
    private static int REQUEST_CODE = 999;
    private ImageView current_location;
    private String seller_category = "";
    private TextView loading_text;
    private String seller_category_type = "0";
    String selected_lat = "", selected_long = "";

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            savePref = new SavePref(getActivity());
            if (savePref.getUserType().equals("1"))
                view = inflater.inflate(R.layout.fragment_map_seller, container, false);
            else
                view = inflater.inflate(R.layout.fragment_map, container, false);

        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        /*View locationButton = ((View) view.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        layoutParams.setMargins(0, 200, 70, 0);

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/

        seller_category = getArguments().getString(ParameterClass.SELLER_CATEGORY);


        return view;
    }


    private void setUpUI() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    @Override
    public void onResume() {
        super.onResume();
//        MainActivity.toolbar_title.setText(R.string.plumbers_in_area);
//        MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.plum, 0, 0, 0);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = (MainActivity) getActivity();
        savePref = new SavePref(context);
        initialize(view);

        loading_image_pin.setVisibility(View.GONE);
        request_schedule_layout.setVisibility(View.VISIBLE);

        setUpUI();
    }

    private void initialize(View view) {
//        autocomplete_text = (AutoCompleteTextView) view.findViewById(R.id.edt_search);

        edt_search = (EditText) view.findViewById(R.id.edt_search);
        request_service = (TextView) view.findViewById(R.id.request_service);
        edt_pick_up_location = (TextView) view.findViewById(R.id.edt_pick_up_location);
        schedule_service = (TextView) view.findViewById(R.id.schedule_service);
        search_layout = (RelativeLayout) view.findViewById(R.id.search_layout);
        loading_layout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        recycle = (RecyclerView) view.findViewById(R.id.recycle);
        search = (ImageView) view.findViewById(R.id.search);
        cross = (ImageView) view.findViewById(R.id.cross);
        loading_image_pin = (ImageView) view.findViewById(R.id.pin);
        request_schedule_layout = (LinearLayout) view.findViewById(R.id.request_schedule_layout);
        done = (TextView) view.findViewById(R.id.done);
        current_location = (ImageView) view.findViewById(R.id.current_location);
        loading_text = (TextView) view.findViewById(R.id.loading_text);

        request_service.setOnClickListener(this);
        schedule_service.setOnClickListener(this);
        current_location.setOnClickListener(this);
        done.setOnClickListener(this);

        resultList = new ArrayList<String[]>();
        deatilsAdapter = new SearchLocationAdapter(getActivity(), resultList, MapFragment.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(linearLayoutManager);
        recycle.setAdapter(deatilsAdapter);

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request_schedule_layout.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                loading_image_pin.setVisibility(View.VISIBLE);
                edt_search.setText("");
                show_pin = true;

                getAllPlumberApi(savePref.getLat(), savePref.getLong(), true, true, false, true);
            }
        });

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

        /*edt_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                loading_image_pin.setVisibility(View.VISIBLE);
                return true;
            }
        });*/

        edt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_pin = true;
                edt_search.setEnabled(true);
                edt_search.setFocusable(true);
                isMap_move = true;

                if (loading_image_pin.getVisibility() == View.GONE) {
                    onCameraChange();
                    done.setVisibility(View.VISIBLE);
                    cross.setVisibility(View.VISIBLE);
                    search.setVisibility(View.GONE);
                }

                loading_image_pin.setVisibility(View.VISIBLE);
                Utils.showKeyboard(edt_search, getActivity());
                request_schedule_layout.setVisibility(View.GONE);


                if (currLocationMarker != null)
                    currLocationMarker.remove();

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
                    request_schedule_layout.setVisibility(View.GONE);
                } else {
                    show_pin = true;
                    loading_image_pin.setVisibility(View.VISIBLE);
                    request_schedule_layout.setVisibility(View.GONE);
                    done.setVisibility(View.GONE);
                    getAllPlumberApi(savePref.getLat(), savePref.getLong(), false, true, false, true);
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


        final ImageView imgView = (ImageView) view.findViewById(R.id.loading_image);

        if (savePref.getUserType().equals("1")) {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(imgView);
            Glide.with(this).load(R.raw.loading_blue).into(imageViewTarget);
        } else {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(imgView);
            Glide.with(this).load(R.raw.loading).into(imageViewTarget);
        }
    }

    private void setGooglemapSetting() {
        try {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        mGoogleMap.setInfoWindowAdapter(getInfoWindowAdapter());

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker.getSnippet().equals("false")) {
                    is_animate = false;
                    if (marker.isInfoWindowShown())
                        marker.hideInfoWindow();
                    else
                        marker.showInfoWindow();
                } else {
                    show_pin = true;

                    edt_search.setEnabled(true);
                    edt_search.setFocusable(true);
                    isMap_move = true;

                    loading_image_pin.setVisibility(View.VISIBLE);
                    Utils.showKeyboard(edt_search, getActivity());
                    request_schedule_layout.setVisibility(View.GONE);

                    done.setVisibility(View.VISIBLE);
                    cross.setVisibility(View.VISIBLE);
                    if (currLocationMarker != null) {
                        currLocationMarker.remove();
                        currLocationMarker = null;
                    }

                    onCameraChange();
                }
                return true;
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                for (int i = 0; i < sellerDataArrayList.size(); i++) {
                    if (sellerDataArrayList.get(i).getMarkerid().equals(marker.getId())) {
                        Intent intent = new Intent(context, SellerDetailActivity.class);
                        intent.putExtra("seller_id", sellerDataArrayList.get(i).getSellerId());
                        intent.putExtra("req_lat", sellerDataArrayList.get(i).getReq_lat());
                        intent.putExtra(ParameterClass.SELLER_CATEGORY, seller_category);
                        intent.putExtra("req_lng", sellerDataArrayList.get(i).getReq_lng());
                        intent.putExtra("omorni_procesing_fee", sellerDataArrayList.get(i).getOmorni_processing_fee());
                        intent.putExtra("vat_tax", sellerDataArrayList.get(i).getVat_tax());
                        intent.putExtra("service_now", true);
                        intent.putExtra("favourite", sellerDataArrayList.get(i).getFavourite());
                        context.startActivity(intent);
                    }
                }

            }
        });

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

    private void onCameraChange() {
        if (show_pin && isMap_move) {
            if (!is_search) {
                is_selected_place = true;
                final LatLng mPosition = mGoogleMap.getCameraPosition().target;
                final Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isMap_move) {
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
                                        if (addresses != null && isMap_move) {
                                            if (addresses.size() > 0) {
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
                                                request_schedule_layout.setVisibility(View.GONE);
                                                done.setVisibility(View.VISIBLE);
//                                                    if (is_animate) {
////                                                        mGoogleMap.clear();
//                                                        getAllPlumberApi(selectedlat, selectedlong, true, true, false);
//                                                    } else
//                                                        is_animate = true;
                                            }
                                        }

                                    }
                                });
                            }
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.request_service:

                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                }

                Intent intent = new Intent(getActivity(), SellerListActivity.class);
                intent.putParcelableArrayListExtra("seller_data", sellerDataArrayList);
                intent.putExtra("service_now", true);
                intent.putExtra("refreshTime", refreshTime);
                intent.putExtra("selected_lat", selected_lat);
                intent.putExtra("selected_long", selected_long);
                intent.putExtra(ParameterClass.SELLER_CATEGORY, seller_category);
                startActivityForResult(intent, REQUEST_CODE);

                ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.schedule_service:
//                is_selected_place = true;
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                }
                Bundle bundle = new Bundle();
                bundle.putString(ParameterClass.SELLER_CATEGORY, seller_category);
                ScheduleServiceFragment scheduleServiceFragment = new ScheduleServiceFragment();
                scheduleServiceFragment.setArguments(bundle);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();

//                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, scheduleServiceFragment);
                fragmentTransaction.commit();
//                ((MainActivity)getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;

            case R.id.done:
                show_pin = false;
                getAllPlumberApi(selectedlat, selectedlong, false, false, true, false);
                Utils.hideKeyboard(getActivity());
                request_schedule_layout.setVisibility(View.VISIBLE);
                done.setVisibility(View.GONE);
                cross.setVisibility(View.GONE);
                loading_image_pin.setVisibility(View.GONE);
//                edt_search.setEnabled(false);
                isMap_move = false;
                break;

            case R.id.current_location:
                LatLng coordinate = new LatLng(Double.parseDouble(savePref.getLat()), Double.parseDouble(savePref.getLong()));
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, currentZoom);
                mGoogleMap.animateCamera(location);
                break;
        }
    }

    /****
     * SocialLoginApi
     ****/
    private void getAllPlumberApi(final String lat, final String lng, final boolean is_animate_camera, final boolean show_loading, final boolean showSellerListDialog, final boolean from_refreshing) {
        selected_lat = lat;
        selected_long = lng;
        is_selected_place = true;

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

        if (!show_loading && !from_refreshing) {

            loading_layout.setVisibility(View.VISIBLE);
            search_layout.setVisibility(View.GONE);
        }

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.LATTITUDE, lat);
        formBuilder.add(AllOmorniParameters.LONGITUDE, lng);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        formBuilder.add(AllOmorniParameters.SELLER_CATEGORY, seller_category);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.NEAR_ME_URL, formBody) {
            @Override
            public void getValueParse(final String result) {
                Log.e("map", "result " + result);
                //mGoogleMap.clear();
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mGoogleMap.clear();
                                    if (currLocationMarker != null) {
                                        currLocationMarker.remove();
                                        currLocationMarker = null;
                                    }


//                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong, currentZoom));
//                if (search)
//                    mGoogleMap.addMarker(new MarkerOptions().position(latlong).title(selected_place_name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    if (result != null && !result.equalsIgnoreCase("")) {
                                        try {
                                            Log.e("result", "here " + result);
                                            JSONObject jsonObject = new JSONObject(result);
                                            JSONObject status = jsonObject.getJSONObject("status");
                                            refreshTime = status.getString("search_refresh_time");
                                            sellerDataArrayList = new ArrayList<SellerData>();
                                            if (status.getString("code").equalsIgnoreCase("1")) {
                                                //loading_image_pin.setVisibility(View.GONE);
//                            refresh_time = status.getString("search_refresh_time");

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

                                                    sellerData.setReq_lat(lat);
                                                    sellerData.setReq_lng(lng);
                                                    String lat = object1.getString("latitude");
                                                    String longi = object1.getString("longitude");
                                                    sellerData.setLatitude(lat);
                                                    sellerData.setLongitude(longi);
                                                    sellerData.setOn_duty(object1.getString("on_duty"));
                                                    sellerData.setDistance(object1.getString("distance"));
                                                    sellerData.setFavourite(object1.getString("favourite"));
                                                    sellerData.setRating(object1.getString("avgrating"));


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
                                                    sellerData.setVat_tax(object1.getString("vat_tax"));
                                                    sellerData.setTotal_rating_user(object1.getString("total_rating_user"));
                                                    sellerData.setSeller_category(object1.getString("category"));

                                                    JSONArray package_array = object1.getJSONArray("allpackage");

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
//                                sellerDataArrayList.add(sellerData);
                                                    LatLng sellerLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));

                                                    // Add a marker in Map.
                                                    Log.e("size", "list " + sellerDataArrayList.size());
                                                    currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(sellerLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.man_blue)).snippet("false"));
//                                mGoogleMap.setInfoWindowAdapter(getInfoWindowAdapter(sellerData.getFirst_name()+" "+sellerData.getLast_name(),sellerData.getRating(),sellerData.getTotal_rating_user(),sellerData.getLocation()));
                                                    sellerData.setMarkerid(currLocationMarker.getId());
                                                    sellerDataArrayList.add(sellerData);

                                                }

                                                if (showSellerListDialog) {
                                                    if (sellerDataArrayList.size() == 0) {
                                                        String category_message = "";
                                                        if (seller_category.equals(getResources().getString(R.string.plumber_id))) {
                                                            category_message = getResources().getString(R.string.no_plumbers_avaialable);
                                                        } else if (seller_category.equals(getResources().getString(R.string.electrician_id))) {
                                                            category_message = getResources().getString(R.string.no_electrician_avaialable);
                                                        } else if (seller_category.equals(getResources().getString(R.string.carpenter_id))) {
                                                            category_message = getResources().getString(R.string.no_carpeneter_avaialable);
                                                        } else if (seller_category.equals(getResources().getString(R.string.ac_id))) {
                                                            category_message = getResources().getString(R.string.no_ac_avaialable);
                                                        } else if (seller_category.equals(getResources().getString(R.string.satellite_id))) {
                                                            category_message = getResources().getString(R.string.no_satellite_avaialable);
                                                        } else if (seller_category.equals(getResources().getString(R.string.painter_id))) {
                                                            category_message = getResources().getString(R.string.no_painter_avaialable);
                                                        }
                                                        new android.support.v7.app.AlertDialog.Builder(context)
                                                                .setTitle(getResources().getString(R.string.sorry))
                                                                .setMessage(category_message)
                                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        // continue with delete
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                }

                                                loading_layout.setVisibility(View.GONE);
                                                search_layout.setVisibility(View.VISIBLE);

                                                LatLng selectedPlaceLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                                                if (!show_loading && is_animate_camera)
                                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceLatLng, currentZoom));
                                                if (!show_loading && !is_animate_camera) {
                                                    done.setVisibility(View.GONE);
                                                    loading_image_pin.setVisibility(View.GONE);
                                                    request_schedule_layout.setVisibility(View.VISIBLE);
                                                    if (Utils.isConfigRtl(getActivity()))
                                                        currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_arabic)).snippet("true"));
                                                    else
                                                        currLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(selectedPlaceLatLng).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.change_location_tip)).snippet("true"));
                                                }
                                                setGooglemapSetting();
                                                if (setTimer)
                                                    setTimer();
                                                setTimer = false;

                                            } else {
                                                loading_layout.setVisibility(View.GONE);
                                                search_layout.setVisibility(View.VISIBLE);
                                                Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
                                            }
                                        } catch (JSONException ex) {
                                            loading_layout.setVisibility(View.GONE);
                                            search_layout.setVisibility(View.VISIBLE);
                                            ex.printStackTrace();
                                        } catch (Exception ex) {
                                            loading_layout.setVisibility(View.GONE);
                                            search_layout.setVisibility(View.VISIBLE);
                                            ex.printStackTrace();
                                        }
                                    } else {
                                        loading_layout.setVisibility(View.GONE);
                                        search_layout.setVisibility(View.VISIBLE);
                                        Utils.showSnackBar(view, getResources().getString(R.string.internet_error), context);
                                    }
                                }
                            });
                        }
                    }
                };
                thread.start();
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();

    }


    private GoogleMap.InfoWindowAdapter getInfoWindowAdapter() {
        return new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(final Marker marker) {

                SellerData sellerData = null;
                for (int i = 0; i < sellerDataArrayList.size(); i++) {
                    if (sellerDataArrayList.get(i).getMarkerid().equals(marker.getId())) {
                        sellerData = sellerDataArrayList.get(i);
                    }
                }

                View viewInfoWindow = getActivity().getLayoutInflater().inflate(R.layout.marker_info_window_seller, null);

                TextView seller_name = (TextView) viewInfoWindow.findViewById(R.id.seller_name);
                TextView number_users = (TextView) viewInfoWindow.findViewById(R.id.number_users);
                TextView seller_address = (TextView) viewInfoWindow.findViewById(R.id.seller_address);
                AppCompatRatingBar ratingBar = (AppCompatRatingBar) viewInfoWindow.findViewById(R.id.rating);

                seller_name.setText(sellerData.getFirst_name() + " " + sellerData.getLast_name());
                number_users.setText("(" + sellerData.getTotal_rating_user() + ")");
                seller_address.setText(sellerData.getLocation());
                ratingBar.setRating(Float.parseFloat(sellerData.getRating()));


                return viewInfoWindow;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        };
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
            if (isFirstTime) {
                isFirstTime = false;
                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                savePref.setLat(String.valueOf(mLastLocation.getLatitude()));
                savePref.setLong(String.valueOf(mLastLocation.getLongitude()));
                if (ConnectivityReceiver.isConnected()) {
//                    mGoogleMap.clear();
                    if (selectedlat != "")
                        getAllPlumberApi(selectedlat, selectedlong, true, false, false, false);
                    else
                        getAllPlumberApi(savePref.getLat(), savePref.getLong(), true, false, true, false);
                } else {
                    Utils.showToast(getActivity(), getResources().getString(R.string.internet_error));
                }
            }
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
        android.support.v7.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
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
                search_layout.setVisibility(View.VISIBLE);

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

                        request_schedule_layout.setVisibility(View.GONE);
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
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
            }
        }.start();
    }

    public void SetSelectLocation(String select_location, int position) {
        Utils.hideKeyboard(getActivity());
        loading_text.setText(getActivity().getResources().getString(R.string.searching_place) + " \n" + select_location);
        loading_layout.setVisibility(View.VISIBLE);
        search_layout.setVisibility(View.GONE);

        is_search = true;
        is_selected_place = true;

        recycle.setVisibility(View.GONE);
        selected_place_name = resultList.get(position)[0];
        edt_search.setText(select_location);
        edt_search.setSelection(edt_search.getText().length());
        String place_id = resultList.get(position)[1];
        Utils.hideKeyboard(getActivity());
        PlaceDetailsJSONParser(place_id, select_location);
    }


    private void setTimer() {
        try {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (isMap_move) {
                                    if (selectedlat != "")
                                        getAllPlumberApi(selectedlat, selectedlong, true, true, false, true);
                                    else
                                        getAllPlumberApi(savePref.getLat(), savePref.getLong(), true, true, false, true);
                                } else {
                                    getAllPlumberApi(selectedlat, selectedlong, false, false, true, true);
                                }
                            }
                        });
                    } else {
                        timer.cancel();
                        timer.purge();
                        timer = null;
                    }
                }
            }, 1000 * Integer.parseInt(refreshTime), 1000 * Integer.parseInt(refreshTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean from_sellerlist = data.getBooleanExtra("from_sellerlist", false);

                if (from_sellerlist) {
                    if (!refreshTime.equals("")) {
                        if (isMap_move) {
                            if (selectedlat != "")
                                getAllPlumberApi(selectedlat, selectedlong, true, true, false, true);
                            else
                                getAllPlumberApi(savePref.getLat(), savePref.getLong(), true, true, false, true);
                        } else {
                            getAllPlumberApi(selectedlat, selectedlong, false, false, true, true);
                        }
                        setTimer();
                    }


                }
            }
        }
    }
}
