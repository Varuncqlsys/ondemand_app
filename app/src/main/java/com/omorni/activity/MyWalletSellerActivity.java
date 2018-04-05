package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

/**
 * Created by test on 6/24/2017.
 */

public class MyWalletSellerActivity extends BaseActivity implements View.OnClickListener {

    MyWalletSellerActivity context;
    SavePref savePref;
    TextView total;
    TextView addViewHistoryButton, withdrawButton;
    private boolean is_bank_account_added = false;
    private LinearLayout bottom_layout;
    private String total_balance = "";
    private String account_exist = "0";
    private SwipeRefreshLayout swipe_refresh_layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        savePref = new SavePref(context);
        setTheme(R.style.AppThemeSeller);
        setContentView(R.layout.my_wallet_seller);
        setUpUI();
        setToolbar();
        if (ConnectivityReceiver.isConnected()) {
            getMyBalanceSeller(true);
        } else {
            Utils.showToast(MyWalletSellerActivity.this, getResources().getString(R.string.internet_error));
        }
    }

    private void setUpUI() {
        total = (TextView) findViewById(R.id.total);
        addViewHistoryButton = (TextView) findViewById(R.id.add_view_history);
        withdrawButton = (TextView) findViewById(R.id.withdraw);
        bottom_layout = (LinearLayout) findViewById(R.id.bottom_layout);
        swipe_refresh_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectivityReceiver.isConnected()) {
                    swipe_refresh_layout.setRefreshing(true);
                    getMyBalanceSeller(false);
                } else {
                    Utils.showToast(MyWalletSellerActivity.this, getResources().getString(R.string.internet_error));
                }

            }
        });
        addViewHistoryButton.setOnClickListener(this);
        withdrawButton.setOnClickListener(this);
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
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.add_view_history:
                intent = new Intent(MyWalletSellerActivity.this, WithdrawHistoryActivity.class);
                startActivity(intent);
                break;

            case R.id.withdraw:
                if (is_bank_account_added) {
                    intent = new Intent(MyWalletSellerActivity.this, WithdrawActivity.class);
                    intent.putExtra("total", total_balance);
                    startActivityForResult(intent, 203);
                } else {
                    Utils.showToast(MyWalletSellerActivity.this, getResources().getString(R.string.add_bank_details));
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_my_wallet, menu);
        return true;
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

            case R.id.add_account_seller:

                Intent intent2 = new Intent(MyWalletSellerActivity.this, AddAccountActivity.class);
                startActivityForResult(intent2, 199);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem add_account_buyer = menu.findItem(R.id.add_account_buyer);
        MenuItem add_account_seller = menu.findItem(R.id.add_account_seller);
        add_account_buyer.setVisible(false);
        return true;
    }


    private void getMyBalanceSeller(final boolean show_progress) {
        final ProgressDialog progressDialog = new ProgressDialog(MyWalletSellerActivity.this);
        if (show_progress) {
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(MyWalletSellerActivity.this, AllOmorniApis.GET_ONLY_BALANCE, formBody) {
            @Override
            public void getValueParse(String result) {
                bottom_layout.setVisibility(View.VISIBLE);
                if (show_progress) {
                    progressDialog.dismiss();
                }
                swipe_refresh_layout.setRefreshing(false);
                Log.e("resultt", "result " + result);
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            total_balance = jsonObjectBody.getString("user_balance");
                            total.setText(total_balance + " " + getResources().getString(R.string.sar));
                            if (jsonObjectBody.getString("account_exist").equals("1")) {
                                is_bank_account_added = true;
                                withdrawButton.setBackground(getResources().getDrawable(R.drawable.blue_rounded_button));
                            } else {
                                is_bank_account_added = false;
                                withdrawButton.setBackground(getResources().getDrawable(R.drawable.light_gray_rounded_button));
                            }

                        } else {
                            Utils.checkAuthToken(MyWalletSellerActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 199) {
                account_exist = data.getStringExtra("account_exist");
                if (account_exist.equals("1")) {
                    is_bank_account_added = true;
                    withdrawButton.setBackground(getResources().getDrawable(R.drawable.blue_rounded_button));
                }
            } else if (requestCode == 203) {
                total_balance = data.getStringExtra("user_balance");
                total.setText(total_balance + " " + getResources().getString(R.string.sar));
            }
        }
    }
}
