package com.omorni.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.omorni.R;
import com.omorni.activity.EnterPasswordActivity;
import com.omorni.activity.MainActivity;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.LocaleHelper;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;


/**
 * Created by user on 10/27/2016.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    SavePref savePref;

    @Override
    public void onTokenRefresh() {
        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
        savePref = new SavePref(getApplicationContext());
        SavePref.setString(getApplicationContext(), "token", token);

        if (!savePref.getUserId().equals(""))
            uploadDeviceToken();
    }


    private void uploadDeviceToken() {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.DEVICE_TYPE, "0");
        formBuilder.add(AllOmorniParameters.DEVICE_TOKEN, savePref.getString(getApplicationContext(), "token", ""));
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(getApplicationContext(), AllOmorniApis.UPDATE_DEVICE_TOKEN, formBody) {
            @Override
            public void getValueParse(String result) {

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            Log.e("updateee", "token " + result);
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
