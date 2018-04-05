package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {
    private EditText old_password, new_password, confirm_password;
    private Context context;
    private SavePref savePref;
    private LinearLayout old_password_layout;
    private String send_old_password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = ChangePasswordActivity.this;
        savePref = new SavePref(context);
        // 1 means seller
        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_change_password_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_change_password);
        }

        initialize();
        setToolbar();
    }

    private void initialize() {
        old_password = (EditText) findViewById(R.id.old_password);
        new_password = (EditText) findViewById(R.id.new_password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        old_password_layout = (LinearLayout) findViewById(R.id.old_password_layout);

        if (savePref.getPassword().equals("")) {
            old_password_layout.setVisibility(View.GONE);
            send_old_password = "1";
        } else {
            old_password_layout.setVisibility(View.VISIBLE);
            send_old_password = "0";
        }
        Button submit_btn = (Button) findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(this);
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.change_password);

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
                        if (savePref.getPassword().equals("")) {
                            changePasswordApi(old_password.getText().toString(), new_password.getText().toString());
                        } else {
                            if (old_password.getText().toString().isEmpty())
                                Utils.showSnackBar(view, getResources().getString(R.string.error_old_password), context);
                            else
                                changePasswordApi(old_password.getText().toString(), new_password.getText().toString());
                        }
                    }
                } else {
                    Utils.showSnackBar(view, getResources().getString(R.string.internet_error), context);
                }
                break;
        }
    }

    private void changePasswordApi(String old_password, String new_password) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.NEW_PASSWORD, new_password);
        formBuilder.add(AllOmorniParameters.OLD_PASSWORD, old_password);
        formBuilder.add(AllOmorniParameters.CHECK_PASS, send_old_password);

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.UPDATE_PASSWORD_URL, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            savePref.setPassword("123456");
                            Utils.showToast(context, status.getString("message"));
                            finish();
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
