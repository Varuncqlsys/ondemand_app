package com.omorni.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.omorni.R;
import com.omorni.activity.LoginActivity;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.GPSTracker;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by test on 12/28/2016.
 */

public class UpdateLocationScheduler extends BroadcastReceiver {
    Context contextValue;
    SavePref savePref;

    @Override
    public void onReceive(Context context, Intent intentw) {
        contextValue = context;
        savePref = new SavePref(contextValue);
        if (!savePref.getUserId().equals("")) {
            GPSTracker gpsTracker = new GPSTracker(context);
            if (gpsTracker.canGetLocation()) {
                savePref.setLat(String.valueOf(gpsTracker.getLatitude()));
                savePref.setLong(String.valueOf(gpsTracker.getLongitude()));
                updateLocation(String.valueOf(gpsTracker.getLatitude()), String.valueOf(gpsTracker.getLongitude()));
            }
        }
    }
    //Method to get the Location

    /****
     * UpdateLocation
     ****/
    private void updateLocation(final String lat, final String lng) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.LATTITUDE, lat);
        formBuilder.add(AllOmorniParameters.LONGITUDE, lng);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(contextValue, AllOmorniApis.SET_UPDATED_LAT_LONG, formBody) {
            @Override
            public void getValueParse(String result) {
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
//                            Util.showToast(contextValue, lat + " " + lng);
                        } else {
                            String token = status.getString("auth_token");
                            if (token.equals("0")) {
                                savePref.clearPreferences();
                                Intent in = new Intent(contextValue, LoginActivity.class);
                                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                contextValue.startActivity(in);
                            }
                            Utils.showToast(contextValue, status.getString("message"));
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
