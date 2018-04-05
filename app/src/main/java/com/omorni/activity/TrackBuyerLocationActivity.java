package com.omorni.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.omorni.R;

import com.omorni.model.PayPalResponse;
import com.omorni.model.SellerData;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.DirectionParser;
import com.omorni.parser.GetAsync;
import com.omorni.utils.LatLngInterpolator;
import com.omorni.utils.MarkerAnimation;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;


import okhttp3.FormBody;
import okhttp3.RequestBody;

public class TrackBuyerLocationActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Toolbar toolbar;
    private TextView toolbar_title, buyer_name, distance, arrived;
    private LinearLayout check_in;
    private ImageView chat, loading_image;
    private GoogleMap mMap;
    private float currentZoom = 13;
    private SavePref savePref;
    Marker marker = null;
    PayPalResponse payPalResponse;
    String emp_lat, emp_long;
    BitmapDescriptor icon;
    Timer timer;
    LatLng latLongDestination;
    Location locationDestination;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LinearLayout navigation;
    private HashMap<String, String> distance_map;
    private boolean is_draw_route = true;
    private TextView navigate_to_text, loading_text;
    private RelativeLayout loading_layout, main_layout;
    ProgressDialog pro = null;
    private LinearLayout button_layout;
    Context context;
    String refund_option = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeSeller);
        context = TrackBuyerLocationActivity.this;
        if (Utils.isConfigRtl(TrackBuyerLocationActivity.this)) {
            setContentView(R.layout.activity_track_buyer_rtl);
        } else {
            setContentView(R.layout.activity_track_buyer);
        }

        setToolbar();
        initialize();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.NOTIFICATION_SELLER_CHECKIN_SCREEN));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    }


    private BroadcastReceiver
            mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent1) {

            PayPalResponse payPalResponse = intent1.getParcelableExtra("summary");
            main_layout.setVisibility(View.VISIBLE);
            loading_layout.setVisibility(View.GONE);
            if (payPalResponse.getJob_started().equals("0")) {
                showRejectDialog(intent1.getStringExtra("message"), payPalResponse.getBuyer_mobile(), payPalResponse.getBuyer_name());
            } else {
                Intent intent = new Intent(TrackBuyerLocationActivity.this, SellerWorkInProgressActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("summary", payPalResponse);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }

        }
    };

    private void showRejectDialog(final String message, final String mobile, final String buyer_name) {
        final Dialog dialog = new Dialog(TrackBuyerLocationActivity.this, R.style.Theme_Dialog);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setContentView(R.layout.dialog_show_reject_checkin_seller);

        TextView text_message = (TextView) dialog.findViewById(R.id.text_message);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView try_again = (TextView) dialog.findViewById(R.id.try_again);
        TextView call_him = (TextView) dialog.findViewById(R.id.call_him);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        ImageView back = (ImageView) dialog.findViewById(R.id.back);

        text_message.setText(message);
        title.setText(buyer_name);

        call_him.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancelAll();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + "+" + mobile));
                startActivity(intent);
            }
        });

        try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancelAll();
                chckingRequest();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancelAll();
                openCancelDialog(dialog);

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancelAll();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void openCancelDialog(final Dialog dialog1) {

        final Dialog dialog = new Dialog(TrackBuyerLocationActivity.this, R.style.Theme_Dialog);

        dialog.setContentView(R.layout.order_cancel_by_seller);

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        final ImageView first_option = (ImageView) dialog.findViewById(R.id.first_option);
        final ImageView second_option = (ImageView) dialog.findViewById(R.id.second_option);
        final ImageView third_option = (ImageView) dialog.findViewById(R.id.third_option);
        final TextView refund = (TextView) dialog.findViewById(R.id.refund);
        final EditText edit_other = (EditText) dialog.findViewById(R.id.edit_other);
        final ImageView cross = (ImageView) dialog.findViewById(R.id.cross);


        first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
        second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
        third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));


        first_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refund_option = "1";
                edit_other.setVisibility(View.GONE);

                first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected_seller));
                second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));

            }
        });

        second_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refund_option = "2";
                edit_other.setVisibility(View.GONE);

                second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected_seller));
                first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));

            }
        });

        third_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refund_option = "3";
                edit_other.setVisibility(View.VISIBLE);

                third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected_seller));
                second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));

            }
        });

        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
                if (refund_option.equals("0")) {
                    Utils.showToast(context, context.getResources().getString(R.string.select_option));
                } else if (refund_option.equals("3")) {
                    if (edit_other.getText().toString().length() == 0) {
                        Utils.showToast(context, context.getResources().getString(R.string.mention_reason_cancel));
                    } else {
                        // hit api
                        cancelJob(edit_other.getText().toString());
                    }
                } else {
                    String message = "";
                    // hit api
                    if (refund_option.equals("1"))
                        message = context.getResources().getString(R.string.not_accept_checkin);
                    else if (refund_option.equals("2"))
                        message = context.getResources().getString(R.string.not_answer_call);
                    cancelJob(message);

                    dialog.dismiss();
                }
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        savePref.setSellerOnCheckin(true);
        savePref.setRequestId(payPalResponse.getRequest_id());
        mGoogleApiClient.connect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);

            }
        }

        /*mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition pos) {
                currentZoom = pos.zoom;
            }
        });*/

        icon = BitmapDescriptorFactory.fromResource(R.drawable.man_blue);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                marker.showInfoWindow();
                return true;
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition pos) {
                currentZoom = pos.zoom;
            }
        });

    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.en_route);
    }

    private void initialize() {
        pro = new ProgressDialog(TrackBuyerLocationActivity.this);
        pro.setCancelable(false);
        pro.setMessage(getResources().getString(R.string.loading));
        pro.show();

        savePref = new SavePref(TrackBuyerLocationActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        payPalResponse = getIntent().getParcelableExtra("summary");


        check_in = (LinearLayout) findViewById(R.id.check_in);
        buyer_name = (TextView) findViewById(R.id.buyer_name);
        distance = (TextView) findViewById(R.id.distance);
        arrived = (TextView) findViewById(R.id.arrived);
        navigate_to_text = (TextView) findViewById(R.id.navigate_to_text);
        chat = (ImageView) findViewById(R.id.chat);
        navigation = (LinearLayout) findViewById(R.id.navigation);
        button_layout = (LinearLayout) findViewById(R.id.button_layout);

        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        loading_text = (TextView) findViewById(R.id.loading_text);
        loading_image = (ImageView) findViewById(R.id.loading_image);

        buyer_name.setText(payPalResponse.getBuyer_name());

        latLongDestination = new LatLng(Double.parseDouble(payPalResponse.getReq_la()), Double.parseDouble(payPalResponse.getReq_lng()));
        locationDestination = new Location("Destination");
        locationDestination.setLatitude(latLongDestination.latitude);
        locationDestination.setLongitude(latLongDestination.longitude);

        loading_text.setText(getResources().getString(R.string.wait_untill) + " " + payPalResponse.getBuyer_name() + " " + getResources().getString(R.string.accept_checkin));
        check_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chckingRequest();
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TrackBuyerLocationActivity.this, ChattingActivity.class);
                intent.putExtra("thread_id", payPalResponse.getThread_id());
                intent.putExtra("req_id", payPalResponse.getRequest_id());
                intent.putExtra("opponent_id", payPalResponse.getBuyer_id());
                intent.putExtra("opponent_image", payPalResponse.getUser_image());
                intent.putExtra("opponent_name", payPalResponse.getBuyer_name());
                intent.putExtra("job_status", "0");
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });

        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + savePref.getLat() + "," + savePref.getLong() + "&daddr=" + payPalResponse.getReq_la() + "," + payPalResponse.getReq_lng()));
                startActivity(intent);
            }
        });

        final ImageView imgView = (ImageView) findViewById(R.id.loading_image);
        if (savePref.getUserType().equals("1")) {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(imgView);
            Glide.with(this).load(R.raw.loading_blue).into(imageViewTarget);
        } else {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(imgView);
            Glide.with(this).load(R.raw.loading).into(imageViewTarget);
        }

        boolean is_from_push = getIntent().getBooleanExtra("from_push", false);
        if (is_from_push) {
            if (payPalResponse.getJob_started().equals("0")) {
                main_layout.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
                showRejectDialog(getIntent().getStringExtra("message"), payPalResponse.getBuyer_mobile(), payPalResponse.getBuyer_name());
            } else {
                main_layout.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
                Intent intent = new Intent(TrackBuyerLocationActivity.this, SellerWorkInProgressActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("summary", payPalResponse);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
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
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


            /*if (marker != null) {
                MarkerAnimation.animateMarkerToICS(marker, sellerLatLng, new LatLngInterpolator.Spherical());
            }
            else {

                LatLng sellerLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng reqLocationLatLng = new LatLng((Double.parseDouble(payPalResponse.getReq_la())), Double.parseDouble(payPalResponse.getReq_lng()));
                String url = getDirectionsUrl(sellerLatLng, reqLocationLatLng);

                FetchUrl FetchUrl = new FetchUrl();
                FetchUrl.execute(url);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sellerLatLng, currentZoom));


                String distance_value = String.valueOf(new DecimalFormat("##.###").format(calculateDistance(sellerLatLng)));
                distance.setText(distance_value + " km away");

                if (distance_value.equals("0")) {
                    arrived.setText("Arrived");
                } else {
                    arrived.setText("On the way");
                }
                markerOptions = new MarkerOptions().position(sellerLatLng).title(savePref.getFirstname()).icon(icon);
                marker = mMap.addMarker(markerOptions);
                marker.setPosition(sellerLatLng);
//                marker.setDraggable(true);
            }*/

            //If everything went fine lets get latitude and longitude
//            currentLatitude = String.valueOf(location.getLatitude());
//            currentLongitude = String.valueOf(location.getLongitude());
//            savePref.setLat(currentLatitude);
//            savePref.setLong(currentLongitude);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }


    @Override
    public void onLocationChanged(Location location) {
//        Utils.showToast(TrackBuyerLocationActivity.this, String.valueOf(location.getLatitude()));

        LatLng sellerLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        savePref.setLat(String.valueOf(location.getLatitude()));
        savePref.setLong(String.valueOf(location.getLongitude()));

        if (marker != null) {
//            Utils.showToast(TrackBuyerLocationActivity.this, String.valueOf(location.getLatitude()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sellerLatLng, currentZoom));
            MarkerAnimation.animateMarkerToICS(marker, sellerLatLng, new LatLngInterpolator.Spherical());
            String url = getDirectionsUrl(sellerLatLng, latLongDestination);
            FetchUrl FetchUrl = new FetchUrl();
            FetchUrl.execute(url);

        } else {
//            is_draw_route = true;

            String url = getDirectionsUrl(sellerLatLng, latLongDestination);
            FetchUrl FetchUrl = new FetchUrl();
            FetchUrl.execute(url);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sellerLatLng, 13));

            MarkerOptions markerOptions = new MarkerOptions().position(latLongDestination).title(payPalResponse.getReq_location()).icon(BitmapDescriptorFactory.fromResource(R.drawable.home));
            Marker marker1 = mMap.addMarker(markerOptions);
            marker1.setPosition(latLongDestination);
            marker1.showInfoWindow();

            MarkerOptions markerOptions1 = new MarkerOptions().position(sellerLatLng).title(savePref.getFirstname() + " " + savePref.getLastname()).icon(icon);
            marker = mMap.addMarker(markerOptions1);
            marker.setPosition(sellerLatLng);
        }
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);

                DirectionParser parser = new DirectionParser();
                if (is_draw_route) {
                    routes = parser.parse(jObject);
                }
                distance_map = new HashMap<String, String>();
                distance_map = parser.getDistance(jObject);

            } catch (Exception e) {

                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            if (is_draw_route) {
                button_layout.setVisibility(View.VISIBLE);
                if (pro != null) {
                    if (pro.isShowing())
                        pro.dismiss();
                }
                if (distance_map.get("value") != null) {
                    if (Integer.parseInt(distance_map.get("value")) < 50) {
                        navigate_to_text.setText(getResources().getString(R.string.navigate_to) + "  " + payPalResponse.getBuyer_name());
                        distance.setText(getResources().getString(R.string.o_m_away));
                        arrived.setText(getResources().getString(R.string.arrived));
                    } else {
                        navigate_to_text.setText(getResources().getString(R.string.navigate_to) + "  " + payPalResponse.getBuyer_name());
                        distance.setText(distance_map.get("distance") + " " + getResources().getString(R.string.away));
                        arrived.setText(getResources().getString(R.string.on_the_way));
                    }
                } else {
                    Utils.showToast(TrackBuyerLocationActivity.this, getResources().getString(R.string.api_limit_over));
                }

                ArrayList<LatLng> points;
                PolylineOptions lineOptions = null;

                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(5);
                    lineOptions.color(Color.RED);
                }

                // Drawing polyline in the Google Map for the i-th route
                if (lineOptions != null) {
                    mMap.addPolyline(lineOptions);
                } else {
                    Log.d("onPostExecute", "without Polylines drawn");
                }
            } else {

                if (distance_map.get("value") != null) {
                    if (Integer.parseInt(distance_map.get("value")) < 50) {
                        navigate_to_text.setText(getResources().getString(R.string.navigate_to) + "  " + payPalResponse.getBuyer_name());
                        distance.setText(getResources().getString(R.string.o_m_away));
                        arrived.setText(getResources().getString(R.string.arrived));
                    } else {
                        navigate_to_text.setText(getResources().getString(R.string.navigate_to) + "  " + payPalResponse.getBuyer_name());
                        distance.setText(distance_map.get("distance") + " " + getResources().getString(R.string.away));
                        arrived.setText(getResources().getString(R.string.on_the_way));
                    }
                } else {
                    Utils.showToast(TrackBuyerLocationActivity.this, getResources().getString(R.string.api_limit_over));
                }
            }

            is_draw_route = false;
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" + Utils.API_KEY;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        Log.e("url", "here " + url);
        return url;
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePref.setSellerOnCheckin(false);
        savePref.setRequestId("");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void updateLocation(final String lat, final String lng) {

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.LATTITUDE, lat);
        formBuilder.add(AllOmorniParameters.LONGITUDE, lng);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(TrackBuyerLocationActivity.this, AllOmorniApis.SET_UPDATED_LAT_LONG, formBody) {
            @Override
            public void getValueParse(String result) {
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {

//                            Utils.showToast(TrackBuyerLocationActivity.this, lat + " " + lng);
                        } else {
                            String token = status.getString("auth_token");
                            if (token.equals("0")) {
                                savePref.clearPreferences();
                                Intent in = new Intent(TrackBuyerLocationActivity.this, LoginActivity.class);
                                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(in);
                            }
                            Utils.showToast(TrackBuyerLocationActivity.this, status.getString("message"));
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


    private void chckingRequest() {

        main_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        Log.e("checkin", "data " + payPalResponse.getRequest_id() + " " + payPalResponse.getSeller_id() + " " + payPalResponse.getBuyer_id() + " " + String.valueOf(System.currentTimeMillis() / 1000));
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.REQUEST_ID, payPalResponse.getRequest_id());
        formBuilder.add(AllOmorniParameters.SELLER_ID, payPalResponse.getSeller_id());
        formBuilder.add(AllOmorniParameters.BUYER_ID, payPalResponse.getBuyer_id());
        formBuilder.add(AllOmorniParameters.CHECK_IN_TIME, String.valueOf(System.currentTimeMillis() / 1000));
        Log.e("current", "time " + System.currentTimeMillis() / 1000);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(TrackBuyerLocationActivity.this, AllOmorniApis.CHECK_IN, formBody) {
            @Override
            public void getValueParse(String result) {
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {

                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");

                        } else {
                            Utils.showToast(TrackBuyerLocationActivity.this, status.getString("message"));
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

    private void cancelJob(String message) {
        final ProgressDialog progressDialog = new ProgressDialog(TrackBuyerLocationActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.REQUEST_ID, payPalResponse.getRequest_id());
        formBuilder.add(AllOmorniParameters.SELLER_ID, payPalResponse.getSeller_id());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.TEXT_MSG, message);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(TrackBuyerLocationActivity.this, AllOmorniApis.SELLER_CANCEL_CHECKIN, formBody) {
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
                            Utils.showToast(TrackBuyerLocationActivity.this, getResources().getString(R.string.seller_cancel));
                            Intent intent = new Intent(TrackBuyerLocationActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

                        } else {
                            Utils.showToast(TrackBuyerLocationActivity.this, status.getString("message"));
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
