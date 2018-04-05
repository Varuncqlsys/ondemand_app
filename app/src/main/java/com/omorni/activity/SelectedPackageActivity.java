package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.adapter.RatingReviewsAdapter;
import com.omorni.model.SellerData;
import com.omorni.model.SellerDetailResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class SelectedPackageActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView toolbar_title, seller_name, number_users, languages, description, sar_package_front, sar_package_back, sar_package_first, sar_package_second, sar_package_third, service_date;
    private Context context;
    private AppCompatRatingBar rating;
    private CircleImageView seller_image;
    private SellerDetailResponse sellerDetailResponse;
    private SellerData sellerData;
    private String selected_package, omorni_procesing_fee = "";
    private boolean from_sellerdetail = false;
    private boolean service_now = false;
    private RelativeLayout loading_layout;
    private LinearLayout main_layout;
    private TextView loading;
    private ImageView loading_image;
    SavePref savePref;
    private String seller_id = "", seller_package_id = "", req_lat = "", req_lng = "";
    Dialog dialog;
    ProgressDialog progressDialog;
    boolean push_screen = false;
    String request_id = "";
    private LinearLayout rating_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(SelectedPackageActivity.this);

        if (Utils.isConfigRtl(SelectedPackageActivity.this)) {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_selected_package_seller_rtl);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_selected_package_rtl);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_selected_package_seller);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_selected_package);
            }
        }

        initialize();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.NOTIFICATION_ACCEPTED_JOB));
        if (from_sellerdetail)
            setDataFromSellerDetail();
        else
            setDataFromSellerList();
        setToolbar();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(seller_name.getText().toString());
    }

    private void initialize() {
        context = SelectedPackageActivity.this;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        from_sellerdetail = getIntent().getBooleanExtra("from_sellerdetail", false);
        service_now = getIntent().getBooleanExtra("service_now", false);
        omorni_procesing_fee = getIntent().getStringExtra("omorni_procesing_fee");
        push_screen = getIntent().getBooleanExtra("push_screen", false);

        if (from_sellerdetail)
            sellerDetailResponse = getIntent().getParcelableExtra("selected_package");
        else
            sellerData = getIntent().getParcelableExtra("selected_package");

        selected_package = getIntent().getStringExtra("package");

        if (push_screen) {
            request_id = getIntent().getStringExtra("req_id");
            final AlertDialog.Builder alert = new AlertDialog.Builder(SelectedPackageActivity.this);
            alert.setTitle(R.string.app_name).setCancelable(false)
                    .setMessage(getIntent().getStringExtra("message"))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
        seller_name = (TextView) findViewById(R.id.seller_name);
        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        main_layout = (LinearLayout) findViewById(R.id.main_layout);
        loading = (TextView) findViewById(R.id.loading);
        loading_image = (ImageView) findViewById(R.id.loading_image);
        number_users = (TextView) findViewById(R.id.number_users);
        languages = (TextView) findViewById(R.id.languages);
        description = (TextView) findViewById(R.id.description);
        sar_package_front = (TextView) findViewById(R.id.sar_package_front);
        sar_package_back = (TextView) findViewById(R.id.sar_package_back);
        sar_package_first = (TextView) findViewById(R.id.sar_package_first);
        sar_package_second = (TextView) findViewById(R.id.sar_package_second);
        sar_package_third = (TextView) findViewById(R.id.sar_package_third);
        service_date = (TextView) findViewById(R.id.service_date);
        rating = (AppCompatRatingBar) findViewById(R.id.rating);
        seller_image = (CircleImageView) findViewById(R.id.seller_image);
        rating_layout = (LinearLayout) findViewById(R.id.rating_layout);

        TextView confirm = (TextView) findViewById(R.id.confirm);
        TextView cancel = (TextView) findViewById(R.id.cancel);

        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
        rating_layout.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("status").equals("4")) {
//                Utils.showToast(context, "Seller accepted your job. Please proceed to pay");
                Intent intent2 = new Intent(context, RequestSummaryActivity.class);
                intent2.putExtra("selected_package", selected_package);
                intent2.putExtra("service_now", service_now);
                intent2.putExtra("start_date", intent.getStringExtra("start_date"));
                intent2.putExtra("start_time", intent.getStringExtra("start_time"));
                intent2.putExtra("is_scheduled", intent.getStringExtra("is_scheduled"));
                intent2.putExtra("req_id", intent.getStringExtra("req_id"));
                intent2.putExtra("req_type", intent.getStringExtra("req_type"));

                if (from_sellerdetail) {
                    intent2.putExtra("selected_seller", sellerDetailResponse);
                    intent2.putExtra("from_sellerdetail", from_sellerdetail);
                } else {
                    intent2.putExtra("selected_seller", sellerData);
                    intent2.putExtra("from_sellerdetail", from_sellerdetail);
                }
                startActivity(intent2);
                main_layout.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                finish();
            } else if (intent.getStringExtra("status").equals("5")) {
                // Utils.showToast(context, "Seller rejected your job. So check for another seller");
                // finish();
                main_layout.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
                showNewSellerDialog(intent.getStringExtra("req_id"), (SellerData) intent.getParcelableExtra("seller_data"), intent.getStringExtra("message"), "5");
            } else if (intent.getStringExtra("status").equals("6")) {
                main_layout.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
                showNewSellerDialog("", null, intent.getStringExtra("message"), "6");
            }// if seller not accept or not reject job in request time and push recived by buyer
            else if (intent.getStringExtra("status").equals("111")) {
                showNewSellerDialog("", null, intent.getStringExtra("message"), "111");
            }
        }
    };

    private void setDataFromSellerDetail() {
        seller_id = sellerDetailResponse.getId();
        req_lat = sellerDetailResponse.getReq_lat();
        req_lng = sellerDetailResponse.getReq_lng();

        seller_name.setText(sellerDetailResponse.getFirst_name() + " " + sellerDetailResponse.getLast_name());
        rating.setRating(Float.parseFloat(sellerDetailResponse.getAvgrating()));
        number_users.setText("(" + sellerDetailResponse.getTotalrating_user() + ")");
        description.setText(sellerDetailResponse.getJob_description());
        languages.setText(sellerDetailResponse.getLanguage());
        if (service_now)
            service_date.setText(getResources().getString(R.string.service_date) + " " + getResources().getString(R.string.now));
        else
            service_date.setText(getResources().getString(R.string.service_date) + " " + getResources().getString(R.string.on) + " " + getIntent().getStringExtra("date") + " " + getResources().getString(R.string.between) + " " + getIntent().getStringExtra("start_time") + " " + getResources().getString(R.string.and) + " " + getIntent().getStringExtra("end_time"));
//        service_date.setText("Service Date : " + getIntent().getStringExtra("date") + " - " + getIntent().getStringExtra("time"));
        if (savePref.getUserType().equals("1"))
            Glide.with(context).load(sellerDetailResponse.getUser_image()).placeholder(R.drawable.user_placeholder_seller).override(150, 150).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        else
            Glide.with(context).load(sellerDetailResponse.getUser_image()).placeholder(R.drawable.user_placeholder).override(150, 150).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        if (selected_package.equals("50")) {
            seller_package_id = sellerDetailResponse.getAllpackage().get(0).getId();

            if (Utils.isConfigRtl(SelectedPackageActivity.this))
                sar_package_front.setText(sellerDetailResponse.getAllpackage().get(0).getNormal_charges() + " " + getResources().getString(R.string.sar));
            else
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(0).getNormal_charges());


            sar_package_back.setText(sellerDetailResponse.getAllpackage().get(0).getPackage_name());
//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.basic_package_upto) + " " + sellerDetailResponse.getAllpackage().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(0).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(0).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerDetailResponse.getAllpackage().get(0).getDescription());
        } else if (selected_package.equals("80")) {
            seller_package_id = sellerDetailResponse.getAllpackage().get(1).getId();

            if (Utils.isConfigRtl(SelectedPackageActivity.this))
                sar_package_front.setText(sellerDetailResponse.getAllpackage().get(1).getNormal_charges() + " " + getResources().getString(R.string.sar));
            else
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(1).getNormal_charges());

            sar_package_back.setText(sellerDetailResponse.getAllpackage().get(1).getPackage_name());

//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.standard_package_upto) + " " + sellerDetailResponse.getAllpackage().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(1).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(1).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerDetailResponse.getAllpackage().get(1).getDescription());
        } else if (selected_package.equals("120")) {
            seller_package_id = sellerDetailResponse.getAllpackage().get(2).getId();

            if (Utils.isConfigRtl(SelectedPackageActivity.this))
                sar_package_front.setText(sellerDetailResponse.getAllpackage().get(2).getNormal_charges() + " " + getResources().getString(R.string.sar));
            else
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(2).getNormal_charges());

            sar_package_back.setText(sellerDetailResponse.getAllpackage().get(2).getPackage_name());

//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.premium_package_upto) + " " + sellerDetailResponse.getAllpackage().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(2).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(2).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerDetailResponse.getAllpackage().get(2).getDescription());
        }
    }

    private void setDataFromSellerList() {
        seller_id = sellerData.getSellerId();
        req_lat = sellerData.getReq_lat();
        req_lng = sellerData.getReq_lng();
        seller_name.setText(sellerData.getFirst_name() + " " + sellerData.getLast_name());
        rating.setRating(Float.parseFloat(sellerData.getRating()));
        number_users.setText("(" + sellerData.getTotal_rating_user() + ")");
        description.setText(sellerData.getJob_description());
        languages.setText(sellerData.getLanguage());

        if (service_now)
            service_date.setText(getResources().getString(R.string.service_date) + " " + getResources().getString(R.string.now));
        else
            service_date.setText(getResources().getString(R.string.service_date) + " " + getIntent().getStringExtra("date") + " - " + getIntent().getStringExtra("time"));
        if (savePref.getUserType().equals("1"))
            Glide.with(context).load(sellerData.getUser_image()).override(150, 150).centerCrop().error(R.drawable.user_placeholder_seller).diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        else
            Glide.with(context).load(sellerData.getUser_image()).override(150, 150).centerCrop().error(R.drawable.user_placeholder).diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        if (selected_package.equals("50")) {
            seller_package_id = sellerData.getAllPackages().get(0).getId();

            if (Utils.isConfigRtl(SelectedPackageActivity.this))
                sar_package_front.setText(sellerData.getAllPackages().get(0).getNormal_charges() + " " + getResources().getString(R.string.sar));
            else
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerData.getAllPackages().get(0).getNormal_charges());

            sar_package_back.setText(sellerData.getAllPackages().get(0).getPackage_name());
//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerData.getAllPackages().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.basic_package_upto) + " " + sellerData.getAllPackages().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerData.getAllPackages().get(0).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerData.getAllPackages().get(0).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerData.getAllPackages().get(0).getDescription());
        } else if (selected_package.equals("80")) {
            seller_package_id = sellerData.getAllPackages().get(1).getId();

            if (Utils.isConfigRtl(SelectedPackageActivity.this))
                sar_package_front.setText(sellerData.getAllPackages().get(1).getNormal_charges() + " " + getResources().getString(R.string.sar));
            else
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerData.getAllPackages().get(1).getNormal_charges());

            sar_package_back.setText(sellerData.getAllPackages().get(1).getPackage_name());

//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerData.getAllPackages().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.standard_package_upto) + " " + sellerData.getAllPackages().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerData.getAllPackages().get(1).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerData.getAllPackages().get(1).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerData.getAllPackages().get(1).getDescription());
        } else if (selected_package.equals("120")) {
            seller_package_id = sellerData.getAllPackages().get(2).getId();

            if (Utils.isConfigRtl(SelectedPackageActivity.this))
                sar_package_front.setText(sellerData.getAllPackages().get(2).getNormal_charges() + " " + getResources().getString(R.string.sar));
            else
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerData.getAllPackages().get(2).getNormal_charges());

            sar_package_back.setText(sellerData.getAllPackages().get(2).getPackage_name());

//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerData.getAllPackages().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.premium_package_upto) + " " + sellerData.getAllPackages().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerData.getAllPackages().get(2).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerData.getAllPackages().get(2).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerData.getAllPackages().get(2).getDescription());
        }

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirm:
                if (push_screen)
                    acceptNewSeller(request_id, seller_id, AllOmorniApis.ACCEPT_NEW_SELLER, sellerData.getFirst_name());
                else
                    checkSellerFreeApi();
//                    confirmOrderApi();
//                showDialog();
                break;
            case R.id.cancel:
                if (push_screen)
                    acceptNewSeller(request_id, seller_id, AllOmorniApis.REJECT_NEW_SELLER, sellerData.getFirst_name());
                else
                    finish();
                break;
            case R.id.rating_layout:
                showRatingDialog();
                break;
        }
    }


    private void checkSellerFreeApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SELLER_ID, seller_id);
        formBuilder.add(AllOmorniParameters.SELER_PKG_ID, seller_package_id);
        formBuilder.add(AllOmorniParameters.CREATED_DATE, String.valueOf(System.currentTimeMillis() / 1000));
        if (!service_now) {
            formBuilder.add(AllOmorniParameters.START_DATE, getIntent().getStringExtra("date"));
            formBuilder.add(AllOmorniParameters.START_TIME, getIntent().getStringExtra("start_time"));
            formBuilder.add(AllOmorniParameters.END_TIME, getIntent().getStringExtra("end_time"));
            formBuilder.add(AllOmorniParameters.IS_SCHEDULED, "1");
        } else {
            formBuilder.add(AllOmorniParameters.IS_SCHEDULED, "0");

        }

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(SelectedPackageActivity.this, AllOmorniApis.CHECK_SELLER_FREE, formBody) {
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
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            if (jsonObject1.getString("on_duty").equals("1") && jsonObject1.getString("is_free").equals("0")) {
                                confirmOrderApi();
                            } else {
                                if (from_sellerdetail) {
                                    if (jsonObject1.getString("on_duty").equals("0")) {
                                        Utils.showToast(context, sellerDetailResponse.getFirst_name() + " " + getResources().getString(R.string.off_duty));
                                    } else {
                                        Utils.showToast(context, sellerDetailResponse.getFirst_name() + " " + getResources().getString(R.string.not_free));
                                    }
                                } else {
                                    if (jsonObject1.getString("on_duty").equals("0")) {
                                        Utils.showToast(context, sellerData.getFirst_name() + " " + getResources().getString(R.string.off_duty));
                                    } else {
                                        Utils.showToast(context, sellerData.getFirst_name() + " " + getResources().getString(R.string.not_free));
                                    }
                                }
                            }
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

    private void confirmOrderApi() {
        Log.e("package", "id " + seller_package_id);
        main_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        loading.setText(getResources().getString(R.string.we_are_contacting) + " " + seller_name.getText().toString() + " \n " + getResources().getString(R.string.please_wait));
        if (savePref.getUserType().equals("1")) {
            Glide.with(SelectedPackageActivity.this)
                    .load("android.resource://" + getPackageName() + "/" + R.drawable.loading_blue)
                    .asGif()
                    .animate(R.drawable.loading_blue)
                    .crossFade()
                    .fitCenter()
                    .into(loading_image);
        } else {
            Glide.with(SelectedPackageActivity.this)
                    .load("android.resource://" + getPackageName() + "/" + R.drawable.loading)
                    .asGif()
                    .animate(R.drawable.loading)
                    .crossFade()
                    .fitCenter()
                    .into(loading_image);
        }
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.BUYER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.SELLER_ID, seller_id);
        formBuilder.add(AllOmorniParameters.SELER_PKG_ID, seller_package_id);
        formBuilder.add(AllOmorniParameters.REQUEST_LAT, req_lat);
        formBuilder.add(AllOmorniParameters.REQUEST_LONG, req_lng);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        if (!service_now) {
            formBuilder.add(AllOmorniParameters.START_DATE, getIntent().getStringExtra("date"));
            formBuilder.add(AllOmorniParameters.START_TIME, getIntent().getStringExtra("start_time"));
            formBuilder.add(AllOmorniParameters.END_TIME, getIntent().getStringExtra("end_time"));
            formBuilder.add(AllOmorniParameters.IS_SCHEDULED, "1");
        } else {
            formBuilder.add(AllOmorniParameters.IS_SCHEDULED, "0");
            formBuilder.add(AllOmorniParameters.CREATED_DATE, String.valueOf(System.currentTimeMillis() / 1000));
        }
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(SelectedPackageActivity.this, AllOmorniApis.ADD_REQUEST, formBody) {
            @Override
            public void getValueParse(String result) {

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            long req_time = Long.parseLong(jsonObject1.getString("request_waiting_time"));

                            /*countDownTimer = new CountDownTimer(req_time * 1000, 1000) {

                                public void onTick(long millisUntilFinished) {

                                }

                                public void onFinish() {
                                    Utils.showToast(context, "No response from seller. So please select any other seller");
                                    finish();
                                }

                            }.start();*/
                            Utils.showToast(context, status.getString("message"));
                        } else {
                            main_layout.setVisibility(View.VISIBLE);
                            loading_layout.setVisibility(View.GONE);
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    main_layout.setVisibility(View.VISIBLE);
                    loading_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void retry() {
            }
        };
        mAsync.execute();
    }


    @Override
    protected void onResume() {
        super.onResume();
        savePref.setUserOnlineForPayment(true);
        if (from_sellerdetail)
            savePref.setSellerId(sellerDetailResponse.getId());
        else
            savePref.setSellerId(sellerData.getSellerId());

    }

    @Override
    protected void onPause() {
        super.onPause();
        savePref.setUserOnlineForPayment(false);
        savePref.setSellerId("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }


    private void showNewSellerDialog(final String request_id, final SellerData sellerData, String text_messaage, final String status) {
        dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setCanceledOnTouchOutside(false);

        if (Utils.isConfigRtl(SelectedPackageActivity.this)) {
            if (savePref.getUserType().equals("1"))
                dialog.setContentView(R.layout.dialog_show_another_seller_seller_rtl);
            else
                dialog.setContentView(R.layout.dialog_show_another_seller_rtl);
        } else {
            if (savePref.getUserType().equals("1"))
                dialog.setContentView(R.layout.dialog_show_another_seller_seller);
            else
                dialog.setContentView(R.layout.dialog_show_another_seller);
        }

        TextView text_message = (TextView) dialog.findViewById(R.id.text_message);
        TextView text_ok = (TextView) dialog.findViewById(R.id.text_ok);
        TextView seller_name = (TextView) dialog.findViewById(R.id.seller_name);
        ImageView seller_image = (ImageView) dialog.findViewById(R.id.seller_image);
        AppCompatRatingBar ratingBar = (AppCompatRatingBar) dialog.findViewById(R.id.rating);
        TextView view_proposal = (TextView) dialog.findViewById(R.id.view_proposal);
        TextView decline = (TextView) dialog.findViewById(R.id.decline);
        LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.linear_layout);
        TextView languages = (TextView) dialog.findViewById(R.id.languages);
        TextView description = (TextView) dialog.findViewById(R.id.description);
        TextView sar_package_front = (TextView) dialog.findViewById(R.id.sar_package_front);
        TextView sar_package_back = (TextView) dialog.findViewById(R.id.sar_package_back);
        TextView sar_package_first = (TextView) dialog.findViewById(R.id.sar_package_first);
        TextView sar_package_second = (TextView) dialog.findViewById(R.id.sar_package_second);

        text_message.setText(text_messaage);
        if (status.equalsIgnoreCase("6") || status.equalsIgnoreCase("111")) {
            linearLayout.setVisibility(View.GONE);
            text_ok.setVisibility(View.VISIBLE);
        } else if (status.equalsIgnoreCase("5")) {
            languages.setText(sellerData.getLanguage());
            if (savePref.getUserType().equals("1"))
                Glide.with(context).load(sellerData.getUser_image()).override(150, 150).centerCrop().error(R.drawable.user_placeholder_seller).diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
            else
                Glide.with(context).load(sellerData.getUser_image()).override(150, 150).centerCrop().error(R.drawable.user_placeholder).diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
            seller_name.setText(sellerData.getFirst_name());
            ratingBar.setRating(Float.parseFloat(sellerData.getRating()));
            description.setText(sellerData.getJob_description());
            text_ok.setVisibility(View.GONE);

            view_proposal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    acceptNewSeller(request_id, sellerData.getSellerId(), AllOmorniApis.ACCEPT_NEW_SELLER, sellerData.getFirst_name());
                }
            });

            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    acceptNewSeller(request_id, sellerData.getSellerId(), AllOmorniApis.REJECT_NEW_SELLER, sellerData.getFirst_name());
                }
            });

            if (selected_package.equals("50")) {
                seller_package_id = sellerData.getAllPackages().get(0).getId();
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerData.getAllPackages().get(0).getNormal_charges());
                sar_package_back.setText(sellerData.getAllPackages().get(0).getPackage_name());
//                sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerData.getAllPackages().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
                sar_package_first.setText(getResources().getString(R.string.basic_package_upto) + " " + sellerData.getAllPackages().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
                sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerData.getAllPackages().get(0).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerData.getAllPackages().get(0).getExtra_hours() + " " + getResources().getString(R.string.hour));

            } else if (selected_package.equals("80")) {
                seller_package_id = sellerData.getAllPackages().get(1).getId();
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerData.getAllPackages().get(1).getNormal_charges());
                sar_package_back.setText(sellerData.getAllPackages().get(1).getPackage_name());

//                sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerData.getAllPackages().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
                sar_package_first.setText(getResources().getString(R.string.standard_package_upto) + " " + sellerData.getAllPackages().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
                sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerData.getAllPackages().get(1).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerData.getAllPackages().get(1).getExtra_hours() + " " + getResources().getString(R.string.hour));

            } else if (selected_package.equals("120")) {
                seller_package_id = sellerData.getAllPackages().get(2).getId();
                sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerData.getAllPackages().get(2).getNormal_charges());
                sar_package_back.setText(sellerData.getAllPackages().get(2).getPackage_name());
//                sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerData.getAllPackages().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
                sar_package_first.setText(getResources().getString(R.string.premium_package_upto) + " " + sellerData.getAllPackages().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
                sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerData.getAllPackages().get(2).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerData.getAllPackages().get(2).getExtra_hours() + " " + getResources().getString(R.string.hour));
            }
        }

        text_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (status.equals("6")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            }
        });

        dialog.show();
    }


    private void acceptNewSeller(String request_id, String seller_id, final String url, String seller_name) {
        if (url.equalsIgnoreCase(AllOmorniApis.ACCEPT_NEW_SELLER)) {
            main_layout.setVisibility(View.GONE);
            loading_layout.setVisibility(View.VISIBLE);
        } else {
            progressDialog.show();
        }

        loading.setText(getResources().getString(R.string.we_are_contacting) + " " + seller_name + " \n " + getResources().getString(R.string.please_wait));
        if (savePref.getUserType().equals("1")) {
            Glide.with(SelectedPackageActivity.this)
                    .load("android.resource://" + getPackageName() + "/" + R.drawable.loading_blue)
                    .asGif()
                    .animate(R.drawable.loading_blue)
                    .crossFade()
                    .fitCenter()
                    .into(loading_image);
        } else {
            Glide.with(SelectedPackageActivity.this)
                    .load("android.resource://" + getPackageName() + "/" + R.drawable.loading)
                    .asGif()
                    .animate(R.drawable.loading)
                    .crossFade()
                    .fitCenter()
                    .into(loading_image);
        }
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.REQUEST_ID, request_id);
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.SELLER_ID, seller_id);
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(SelectedPackageActivity.this, url, formBody) {
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
                            if (url.equalsIgnoreCase(AllOmorniApis.ACCEPT_NEW_SELLER)) {
                                JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                                long req_time = Long.parseLong(jsonObject1.getString("request_waiting_time"));

                                /*countDownTimer = new CountDownTimer(req_time * 1000, 1000) {

                                    public void onTick(long millisUntilFinished) {

                                    }

                                    public void onFinish() {
                                        Utils.showToast(context, "No response from seller. So please select any other seller");
                                        finish();
                                    }

                                }.start();*/
                            } else {
                                Intent intent1 = new Intent(SelectedPackageActivity.this, MainActivity.class);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent1);
                            }

                            Utils.showToast(context, status.getString("message"));
//                            JSONArray body = jsonObject.getJSONArray("body");
//                            for (int i = 0; i < body.length(); i++) {
//                                JSONObject body_obj = body.getJSONObject(i);
//                            }

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

    private void showRatingDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.rating_list);
        dialog.show();
        RatingReviewsAdapter adapter;
        ListView list = (ListView) dialog.findViewById(R.id.listView);
        if (from_sellerdetail)
            adapter = new RatingReviewsAdapter(SelectedPackageActivity.this, sellerDetailResponse.getAllreviews());
        else
            adapter = new RatingReviewsAdapter(SelectedPackageActivity.this, sellerData.getAllreviews());
        list.setAdapter(adapter);
    }
}
