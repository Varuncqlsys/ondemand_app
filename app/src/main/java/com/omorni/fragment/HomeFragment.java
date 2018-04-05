package com.omorni.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.omorni.R;

import com.omorni.activity.MainActivity;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ParameterClass;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private SavePref savePref;

    //    long millisUntilFinished = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        if (savePref.getUserType().equals("1")) {
            view = inflater.inflate(R.layout.fragment_home_seller, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_home, container, false);
        }

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Log.e("timeee", "zone " + cal.getTimeInMillis());
        initialize(view);
//        if (savePref.getUserType().equals("2"))
//            checkBecomeSellerApply();

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!isGpsOn())
            showGPSDisabledAlertToUser();
    }

    private boolean isGpsOn() {
        boolean isGpsOn = false;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGpsOn = false;

        } else {
            isGpsOn = true;
        }
        return isGpsOn;
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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

    private void initialize(View view) {

        RelativeLayout plumber_icon = (RelativeLayout) view.findViewById(R.id.plumber_icon);
        RelativeLayout electrician_icon = (RelativeLayout) view.findViewById(R.id.electrician_icon);
        RelativeLayout carpenter_icon = (RelativeLayout) view.findViewById(R.id.carpenter_icon);
        RelativeLayout ac_icon = (RelativeLayout) view.findViewById(R.id.ac_icon);
        RelativeLayout satellite_icon = (RelativeLayout) view.findViewById(R.id.satellite_icon);
        RelativeLayout painter_icon = (RelativeLayout) view.findViewById(R.id.painter_icon);

        plumber_icon.setOnClickListener(this);
        electrician_icon.setOnClickListener(this);
        carpenter_icon.setOnClickListener(this);
        ac_icon.setOnClickListener(this);
        satellite_icon.setOnClickListener(this);
        painter_icon.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar_title.setText(getResources().getString(R.string.select_service));
        MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        MainActivity.filter.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();

        switch (view.getId()) {
            case R.id.plumber_icon:
                args.putString(ParameterClass.SELLER_CATEGORY, getResources().getString(R.string.plumber_id));
                mapFragment.setArguments(args);
                fragmentTransaction.replace(R.id.container, mapFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                MainActivity.toolbar_title.setText(R.string.plumbers_in_area);
                MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_1, 0, 0, 0);
                break;
            case R.id.electrician_icon:
                args.putString(ParameterClass.SELLER_CATEGORY, getResources().getString(R.string.electrician_id));
                mapFragment.setArguments(args);
                fragmentTransaction.replace(R.id.container, mapFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                MainActivity.toolbar_title.setText(R.string.electrician_in_area);
                MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_2, 0, 0, 0);
                break;
            case R.id.carpenter_icon:
                args.putString(ParameterClass.SELLER_CATEGORY, getResources().getString(R.string.carpenter_id));
                mapFragment.setArguments(args);
                fragmentTransaction.replace(R.id.container, mapFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                MainActivity.toolbar_title.setText(R.string.carpenter_in_area);
                MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_3, 0, 0, 0);
                break;
            case R.id.ac_icon:
                args.putString(ParameterClass.SELLER_CATEGORY, getResources().getString(R.string.ac_id));
                mapFragment.setArguments(args);
                fragmentTransaction.replace(R.id.container, mapFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                MainActivity.toolbar_title.setText(R.string.ac_in_area);
                MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_4, 0, 0, 0);
                break;
            case R.id.satellite_icon:
                args.putString(ParameterClass.SELLER_CATEGORY, getResources().getString(R.string.satellite_id));
                mapFragment.setArguments(args);
                fragmentTransaction.replace(R.id.container, mapFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                MainActivity.toolbar_title.setText(R.string.satellite_in_area);
                MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_5, 0, 0, 0);
                break;
            case R.id.painter_icon:
                args.putString(ParameterClass.SELLER_CATEGORY, getResources().getString(R.string.painter_id));
                mapFragment.setArguments(args);
                fragmentTransaction.replace(R.id.container, mapFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                MainActivity.toolbar_title.setText(R.string.painter_in_area);
                MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_6, 0, 0, 0);
                break;
        }
    }

    private void checkBecomeSellerApply() {
//        final ProgressDialog progressDialog = Utils.initializeProgress(getActivity());
//        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.SELLER_VERIFY, formBody) {
            @Override
            public void getValueParse(String result) {
//                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {

                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            String is_approved = jsonObjectBody.getString("is_approved");
                            if (is_approved.equals("1")) {
                                final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                                alert.setTitle("Congratulations !").setCancelable(false)
                                        .setMessage("Your request to become an Omorni seller is now approved. \n" +
                                                "Please logout and login again to start promoting your services")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })

                                        .show();
//                                Utils.showToast(getActivity(), "Your request to become a seller has been approved");
                            }

                        } else {
                            Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
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
