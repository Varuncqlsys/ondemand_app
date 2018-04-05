package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class UpdateNewPasswordActivity extends BaseActivity implements View.OnClickListener {
    private EditText new_password, confirm_password;
    private Context context;
    private String email, otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        initialize();
    }

    private void initialize() {
        context = UpdateNewPasswordActivity.this;
        email = getIntent().getStringExtra("email");
        otp = getIntent().getStringExtra("otp");
        new_password = (EditText) findViewById(R.id.new_password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        Button submit_btn = (Button) findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(this);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_btn:
                if (ConnectivityReceiver.isConnected()) {
                    if (new_password.getText().toString().isEmpty()) {
                        Utils.showSnackBar(view, getResources().getString(R.string.error_new_password), context);
                    } else if (confirm_password.getText().toString().isEmpty()) {
                        Utils.showSnackBar(view, getResources().getString(R.string.error_confirm_password), context);
                    } else if (!new_password.getText().toString().equals(confirm_password.getText().toString())) {
                        Utils.showSnackBar(view, getResources().getString(R.string.error_match_confirm_password), context);
                    } else {
                        updatePasswordAPi(new_password.getText().toString());
                    }
                } else {
                    Utils.showSnackBar(view, getResources().getString(R.string.internet_error), context);
                }
                break;
        }
    }

    private void updatePasswordAPi(String new_password) {
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
        formBuilder.add(AllOmorniParameters.EMAIL, email);
        formBuilder.add(AllOmorniParameters.OTP, otp);
        formBuilder.add(AllOmorniParameters.NEW_PASSWORD_UPDATE, new_password);
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(this, AllOmorniApis.UPDATE_NEW_PASSWORD_URL, formBody) {
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
                            Intent intent = new Intent(UpdateNewPasswordActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
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
}
