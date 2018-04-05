package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.omorni.R;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ContextWrapper;
import com.omorni.utils.LocaleHelper;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class EnterPasswordActivity extends BaseActivity {
    private EditText password_edittext, conf_password_edittext;
    private RelativeLayout main_layout;
    private Context context;
    private SavePref savePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isConfigRtl(EnterPasswordActivity.this)) {
            setContentView(R.layout.activity_enter_password_rtl);
        } else {
            setContentView(R.layout.activity_enter_password);
        }

        initialize();
    }

    private void initialize() {
        context = EnterPasswordActivity.this;
        savePref = new SavePref(context);
        password_edittext = (EditText) findViewById(R.id.password_edittext);
        conf_password_edittext = (EditText) findViewById(R.id.conf_password_edittext);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout1);
        Button submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password_edittext.getText().toString().isEmpty()) {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.error_password), context);
                } else if (conf_password_edittext.getText().toString().isEmpty()) {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.error_confirm_password), context);
                } else if (!conf_password_edittext.getText().toString().equals(password_edittext.getText().toString())) {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.error_match_confirm_password), context);
                } else {
                    enterPasswordApi(password_edittext.getText().toString());
                }
            }
        });
    }

    private void enterPasswordApi(String password) {
        String app_language= "";
        if(SavePref.getString(context, "app_language", "en").equals("en"))
            app_language = "0";
        else
            app_language = "1";

        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, getIntent().getStringExtra("user_id"));
        formBuilder.add(AllOmorniParameters.PASSWORD, password);
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.SIGNUP_PASSWORD, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
//                            if (checkbox.isChecked()) {
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            savePref.setUserId(jsonObjectBody.getString("id"));
                            savePref.setFirstname(jsonObjectBody.getString("first_name"));
                            savePref.setLastname(jsonObjectBody.getString("last_name"));
                            savePref.setEmail(jsonObjectBody.getString("email"));
                            savePref.setphone(jsonObjectBody.getString("mobile"));
                            savePref.setLat(jsonObjectBody.getString("latitude"));
                            savePref.setLong(jsonObjectBody.getString("longitude"));
                            savePref.setPassword(jsonObjectBody.getString("password"));
                            savePref.setSellerToggle("0");
                            savePref.setOnlyMobileNumber(jsonObjectBody.getString("mobile_no"));
                            savePref.setCodeOnly(jsonObjectBody.getString("mobile_code"));
                            savePref.setAuthToken(jsonObjectBody.getString("auth_token"));
                            savePref.setIsSocialLogin("0");

                            if (jsonObjectBody.getString("is_approved").equals("1")) {
                                savePref.setUserType("1");
                            } else {
                                savePref.setUserType("2");
                            }

//                            Utils.setStopScheduler(context);
//                            Utils.setScheduler(context);
                            ContextWrapper.wrap(context, SavePref.getString(context, "app_language", "ar"));
//                            LocaleHelper.setLocale(context, SavePref.getString(context, "app_language", "ar"));
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("message", status.getString("message"));
                            intent.putExtra("from_signup", true);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
//                            }
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.internet_error), context);
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }
}
