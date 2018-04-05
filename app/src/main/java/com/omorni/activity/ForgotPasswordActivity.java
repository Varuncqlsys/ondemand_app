package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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

public class ForgotPasswordActivity extends BaseActivity implements View.OnClickListener {
    private EditText edit_email;
    private Context context;
    private RelativeLayout main_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isConfigRtl(ForgotPasswordActivity.this)) {
            setContentView(R.layout.activity_forgot_rtl);
        }else{
            setContentView(R.layout.activity_forgot);
        }
        initialize();
    }

    private void initialize() {
        context = ForgotPasswordActivity.this;
        Button submit_btn = (Button) findViewById(R.id.submit_btn);
        edit_email = (EditText) findViewById(R.id.edit_email);
        main_view = (RelativeLayout) findViewById(R.id.main_view);
        submit_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_btn:
                if (ConnectivityReceiver.isConnected()) {
                    if (edit_email.getText().toString().isEmpty()) {
                        Utils.showSnackBar(view, getString(R.string.error_email), context);
                    } else if (!Utils.isValidEmail(edit_email.getText().toString())) {
                        Utils.showSnackBar(view, getString(R.string.error_valid_email), context);
                    } else {
                        forgotPasswordApi(edit_email.getText().toString());
                    }
                } else {
                    Toast.makeText(context, getResources().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void forgotPasswordApi(String email) {
        String app_language= "";
        if(SavePref.getString(context, "app_language", "en").equals("en"))
            app_language = "0";
        else
            app_language = "1";

        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.EMAIL, email);
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.FORGOT_PASSWORD_URL, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            Utils.showToast(context, status.getString("message"));
                            Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordOtpActivity.class);
                            intent.putExtra("email", jsonObject1.getString("email"));
                            intent.putExtra("otp", jsonObject1.getString("otp"));
                            startActivity(intent);
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
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
