package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

public class WithdrawActivity extends BaseActivity {
    private EditText edt_amount;
    private TextView total_balance, withdraw;
    private SavePref savePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeSeller);
        setContentView(R.layout.activity_withdraw);
        initialize();
        setToolbar();

        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_amount.getText().toString().length() == 0) {
                    Utils.showToast(WithdrawActivity.this, getResources().getString(R.string.enter_amount));
                } else if (Float.parseFloat(edt_amount.getText().toString()) < 10) {
                    Utils.showToast(WithdrawActivity.this, getResources().getString(R.string.minimum_10));
                } else if (Float.parseFloat(edt_amount.getText().toString()) > Float.parseFloat(getIntent().getStringExtra("total"))) {
                    Utils.showToast(WithdrawActivity.this, getResources().getString(R.string.insufficient_balance));
                } else {
                    if (ConnectivityReceiver.isConnected()) {
                        withdrawAmountApi();
                    } else {
                        Utils.showToast(WithdrawActivity.this, getResources().getString(R.string.internet_error));
                    }
                }
            }
        });

    }

    private void initialize() {
        savePref = new SavePref(WithdrawActivity.this);
        edt_amount = (EditText) findViewById(R.id.edt_amount);
        total_balance = (TextView) findViewById(R.id.total_balance);
        withdraw = (TextView) findViewById(R.id.withdraw);
        total_balance.setText(getIntent().getStringExtra("total") + " " + getResources().getString(R.string.sar));
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.seller_wallet);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void withdrawAmountApi() {
        final ProgressDialog progressDialog = new ProgressDialog(WithdrawActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.AMOUNT, edt_amount.getText().toString());

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(WithdrawActivity.this, AllOmorniApis.WITHDRAWL_AMOUNT, formBody) {
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
                Log.e("", "result " + result);
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            Utils.showToast(WithdrawActivity.this, status.getString("message"));
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("user_balance", jsonObject1.getString("user_balance"));
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Utils.checkAuthToken(WithdrawActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
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
