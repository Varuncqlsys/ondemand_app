package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.Intent;

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

public class SelectedFavSellerPackageActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView toolbar_title, seller_name, number_users, languages, description, sar_package_front, sar_package_back, sar_package_first, sar_package_second, sar_package_third, service_date;
    private Context context;
    private AppCompatRatingBar rating;
    private CircleImageView seller_image;
    private SellerDetailResponse sellerDetailResponse;
    private String selected_package;
    SavePref savePref;
    private TextView post_job, schedule_service, request_service, loading;
    private RelativeLayout main_layout, loading_layout;
    private ImageView loading_image;
    private String seller_package_id;
    private LinearLayout rating_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        savePref = new SavePref(SelectedFavSellerPackageActivity.this);
        if (Utils.isConfigRtl(SelectedFavSellerPackageActivity.this)) {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_choose_service_rtl);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_choose_service_buyer_rtl);
            }

        } else {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_choose_service);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_choose_service_buyer);
            }

        }

        initialize();
        setToolbar();
        setDataFromSellerDetail();

    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.choose_service);
    }

    private void initialize() {
        context = SelectedFavSellerPackageActivity.this;
//        omorni_procesing_fee = getIntent().getStringExtra("omorni_procesing_fee");
        sellerDetailResponse = getIntent().getParcelableExtra("selected_package");
        selected_package = getIntent().getStringExtra("package");

        seller_name = (TextView) findViewById(R.id.seller_name);
        number_users = (TextView) findViewById(R.id.number_users);
        languages = (TextView) findViewById(R.id.languages);
        description = (TextView) findViewById(R.id.description);
        sar_package_front = (TextView) findViewById(R.id.sar_package_front);
        sar_package_back = (TextView) findViewById(R.id.sar_package_back);
        sar_package_first = (TextView) findViewById(R.id.sar_package_first);
        sar_package_second = (TextView) findViewById(R.id.sar_package_second);
        sar_package_third = (TextView) findViewById(R.id.sar_package_third);
        service_date = (TextView) findViewById(R.id.service_date);

        post_job = (TextView) findViewById(R.id.post_job);
        schedule_service = (TextView) findViewById(R.id.schedule_service);
        request_service = (TextView) findViewById(R.id.request_service);

        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        loading = (TextView) findViewById(R.id.loading);
        loading_image = (ImageView) findViewById(R.id.loading_image);
        rating_layout = (LinearLayout) findViewById(R.id.rating_layout);


        rating = (AppCompatRatingBar) findViewById(R.id.rating);
        seller_image = (CircleImageView) findViewById(R.id.seller_image);

        post_job.setOnClickListener(this);
        schedule_service.setOnClickListener(this);
        request_service.setOnClickListener(this);
        rating_layout.setOnClickListener(this);

    }

    private void setDataFromSellerDetail() {
        seller_name.setText(sellerDetailResponse.getFirst_name() + " " + sellerDetailResponse.getLast_name());
        rating.setRating(Float.parseFloat(sellerDetailResponse.getAvgrating()));
        number_users.setText("(" + sellerDetailResponse.getTotalrating_user() + ")");
        description.setText(sellerDetailResponse.getJob_description());
        languages.setText(sellerDetailResponse.getLanguage());

        Glide.with(context).load(sellerDetailResponse.getUser_image()).override(150, 150).centerCrop().error(R.drawable.user_placeholder).diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        if (selected_package.equals("50")) {
            seller_package_id = sellerDetailResponse.getAllpackage().get(0).getId();
            sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(0).getNormal_charges());
            sar_package_back.setText(sellerDetailResponse.getAllpackage().get(0).getPackage_name());
//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.basic_package_upto) + " " + sellerDetailResponse.getAllpackage().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(0).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(0).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerDetailResponse.getAllpackage().get(0).getDescription());
        } else if (selected_package.equals("80")) {
            seller_package_id = sellerDetailResponse.getAllpackage().get(1).getId();
            sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(1).getNormal_charges());
            sar_package_back.setText(sellerDetailResponse.getAllpackage().get(1).getPackage_name());

//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.standard_package_upto) + " " + sellerDetailResponse.getAllpackage().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(1).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(1).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerDetailResponse.getAllpackage().get(1).getDescription());
        } else if (selected_package.equals("120")) {
            seller_package_id = sellerDetailResponse.getAllpackage().get(2).getId();
            sar_package_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(2).getNormal_charges());
            sar_package_back.setText(sellerDetailResponse.getAllpackage().get(2).getPackage_name());

//            sar_package_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_first.setText(getResources().getString(R.string.premium_package_upto) + " " + sellerDetailResponse.getAllpackage().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(2).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(2).getExtra_hours() + " " + getResources().getString(R.string.hour));
            sar_package_third.setText(sellerDetailResponse.getAllpackage().get(2).getDescription());
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public void onClick(View view) {
        Intent in;
        switch (view.getId()) {
            case R.id.post_job:
                in = new Intent(SelectedFavSellerPackageActivity.this, AddJobActivity.class);
                in.putExtra("from_fav", true);
                startActivity(in);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.request_service:
                checkSellerFreeApi();
//                showDialog();
                break;
            case R.id.schedule_service:
                in = new Intent(SelectedFavSellerPackageActivity.this, ScheduleServiceMapActivity.class);
                in.putExtra("selected_package", sellerDetailResponse);
                in.putExtra("package_id", seller_package_id);
                in.putExtra("package", selected_package);
                startActivity(in);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
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

        formBuilder.add(AllOmorniParameters.SELLER_ID, sellerDetailResponse.getId());
        formBuilder.add(AllOmorniParameters.CREATED_DATE, String.valueOf(System.currentTimeMillis() / 1000));
        formBuilder.add(AllOmorniParameters.IS_SCHEDULED, "0");
        formBuilder.add(AllOmorniParameters.SELER_PKG_ID, seller_package_id);

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(SelectedFavSellerPackageActivity.this, AllOmorniApis.CHECK_SELLER_FREE, formBody) {
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
                                Intent in = new Intent(SelectedFavSellerPackageActivity.this, MapRequestNowActivity.class);
                                in.putExtra("selected_package", sellerDetailResponse);
                                in.putExtra("package_id", seller_package_id);
                                in.putExtra("package", selected_package);
                                startActivity(in);
                                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            } else {
                                if (jsonObject1.getString("on_duty").equals("0")) {
                                    Utils.showToast(context, sellerDetailResponse.getFirst_name() + " is off duty right now");
                                } else {
                                    Utils.showToast(context, sellerDetailResponse.getFirst_name() + " is not free right now.");
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

    private void showRatingDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.rating_list);
        dialog.show();
        ListView list = (ListView) dialog.findViewById(R.id.listView);
        RatingReviewsAdapter adapter = new RatingReviewsAdapter(SelectedFavSellerPackageActivity.this, sellerDetailResponse.getAllreviews());
        list.setAdapter(adapter);
    }
}
