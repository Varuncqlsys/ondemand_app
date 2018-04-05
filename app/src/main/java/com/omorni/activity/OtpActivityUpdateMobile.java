package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.omorni.R;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class OtpActivityUpdateMobile extends BaseActivity implements View.OnClickListener {
    private EditText edit_otp;
    private Context context;
    private RelativeLayout main_view;
    private String user_id;
    private SavePref savePref;
    private Boolean resetBool = false;
    private Button resend_otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        initialize();
        setTimer();
    }

    private void initialize() {
        context = OtpActivityUpdateMobile.this;
        savePref = new SavePref(context);
        user_id = getIntent().getStringExtra("user_id");
        Button submit_btn = (Button) findViewById(R.id.submit_btn);
        resend_otp = (Button) findViewById(R.id.resend_otp);
        edit_otp = (EditText) findViewById(R.id.otp_number);
        main_view = (RelativeLayout) findViewById(R.id.main_view);
        submit_btn.setOnClickListener(this);
        resend_otp.setOnClickListener(this);
    }

    private void setTimer() {
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                resetBool = false;
                resend_otp.setText("00:" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                resetBool = true;
                resend_otp.setText(getResources().getString(R.string.resend_otp));
            }

        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_btn:
                if (ConnectivityReceiver.isConnected()) {
                    if (edit_otp.getText().toString().isEmpty()) {
                        Utils.showSnackBar(main_view, getResources().getString(R.string.error_otp), context);
                    } else {
                        verifyUserApi();
                    }
                } else {
                    Toast.makeText(context, getResources().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.resend_otp:
                if (ConnectivityReceiver.isConnected()) {
                    if (resetBool == true) {
                        resendOtpAPI();
                    }
                } else {
                    Toast.makeText(context, getResources().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void resendOtpAPI() {
        String app_language= "";
        if(SavePref.getString(context, "app_language", "en").equals("en"))
            app_language = "0";
        else
            app_language = "1";

        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, user_id);
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.RESEND_OTP_URL, formBody) {
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
                            Utils.showToast(context, status.getString("message"));
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(main_view, getResources().getString(R.string.internet_error), context);
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    private void verifyUserApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.OTP, edit_otp.getText().toString());
        formBuilder.add(AllOmorniParameters.mobile, getIntent().getStringExtra("mobile"));
        formBuilder.add(AllOmorniParameters.MOBILE_NO, getIntent().getStringExtra("mobile_no"));
        formBuilder.add(AllOmorniParameters.MOBILE_CODE, getIntent().getStringExtra("mobile_code"));
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.MOBILE_VERIFY_UPDATE, formBody) {
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
                            Utils.showToast(context, status.getString("message"));
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            savePref.setOnlyMobileNumber(getIntent().getStringExtra("mobile_no"));
                            savePref.setCodeOnly(getIntent().getStringExtra("mobile_code"));
                            savePref.setphone(getIntent().getStringExtra("mobile"));
                            finish();
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(main_view, getResources().getString(R.string.internet_error), context);
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

}
