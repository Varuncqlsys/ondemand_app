package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;

import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.omorni.R;
import com.omorni.adapter.PostJobsDeatilsAdapter;
import com.omorni.model.PostJobsDetailsResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class PostedJobsDetailActivity extends BaseActivity implements View.OnClickListener {
    SavePref savePref;
    PostedJobsDetailActivity context;
    String job_id = "";
    int position = 0;
    ArrayList<PostJobsDetailsResponse> postedJobDetailsList;
    TextView location_text, job_name, number_quotes, description, workdate, worktime, titletext, loading;
    ImageView delete_post;
    String id = "", buyer_id = "", post_audio = "", post_video = "";
    RecyclerView recyclerView;
    CircleImageView image;
    ImageView imageview, video_view, audio_view, loading_image;
    ScrollView scroll_view;
    String work_date, work_time, req_lat, req_lng, job_location, processing_fee,vat_tax;
    private RelativeLayout loading_layout;
    private int selected_position;
    public static String selected_seller_id = "0";
    PostJobsDeatilsAdapter deatilsAdapter;
    private boolean is_quote_delete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        savePref = new SavePref(context);
        if (Utils.isConfigRtl(PostedJobsDetailActivity.this)) {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_posted_jobs_detail_seller_rtl);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_posted_jobs_detail_rtl);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_posted_jobs_detail_seller);
            } else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_posted_jobs_detail);
            }
        }
        setToolbar();
        initializeUI();
        is_quote_delete = getIntent().getBooleanExtra("from_push", false);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Utils.NOTIFICATION_BUYER_REQUESTED_SELLER_QUOTATION));
        getPostJobDetails(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        is_quote_delete = intent.getBooleanExtra("from_push", false);
    }

    private BroadcastReceiver
            mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("status").equals("4")) {
                Intent intent2 = new Intent(context, RequestSummaryActivity.class);
                intent2.putExtra("service_now", false);
                intent2.putExtra("start_date", intent.getStringExtra("start_date"));
                intent2.putExtra("start_time", intent.getStringExtra("start_time"));
                intent2.putExtra("is_scheduled", intent.getStringExtra("is_scheduled"));
                intent2.putExtra("req_id", intent.getStringExtra("req_id"));
                intent2.putExtra("req_type", intent.getStringExtra("req_type"));
                intent2.putExtra("processing_fee", processing_fee);
                intent2.putExtra("vat_tax",  intent.getStringExtra("vat_tax"));
                intent2.putExtra("seller_data", postedJobDetailsList.get(selected_position));
                startActivity(intent2);
                scroll_view.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            } else if (intent.getStringExtra("status").equals("6") || intent.getStringExtra("status").equals("111")) {
                loading_layout.setVisibility(View.GONE);
                scroll_view.setVisibility(View.VISIBLE);
                showNewSellerDialog(intent.getStringExtra("message"));
            }
        }
    };

    private void showNewSellerDialog(final String message) {
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_seller_rejected_job);

        TextView text = (TextView) dialog.findViewById(R.id.text_message);
        TextView text_ok = (TextView) dialog.findViewById(R.id.text_ok);
        text.setText(message);

        text_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        savePref.setBuyerRequestedSellerQuote(true);
        savePref.setRequestId(job_id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePref.setBuyerRequestedSellerQuote(false);
        savePref.setRequestId("");
    }

    private void initializeUI() {
        job_id = getIntent().getStringExtra("job_id");
        position = getIntent().getIntExtra("position", 0);

        scroll_view = (ScrollView) findViewById(R.id.scroll_view);
        image = (CircleImageView) findViewById(R.id.image);
        location_text = (TextView) findViewById(R.id.location_text);
        job_name = (TextView) findViewById(R.id.job_name);
        number_quotes = (TextView) findViewById(R.id.number_quotes);
        titletext = (TextView) findViewById(R.id.do_tv);
        workdate = (TextView) findViewById(R.id.date);
        worktime = (TextView) findViewById(R.id.time);
        description = (TextView) findViewById(R.id.dummy);
        delete_post = (ImageView) findViewById(R.id.delete);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        imageview = (ImageView) findViewById(R.id.image_view);
        video_view = (ImageView) findViewById(R.id.video_view);
        audio_view = (ImageView) findViewById(R.id.audio_view);

        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        loading_image = (ImageView) findViewById(R.id.loading_image);
        loading = (TextView) findViewById(R.id.loading);


        delete_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePostJobDetails(true);
            }
        });

        video_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post_video.equals("")) {
                    Utils.showToast(context, getResources().getString(R.string.sorry_no_video_attch));
                } else {
                    Intent intent = new Intent(PostedJobsDetailActivity.this, VideoPlayActivity.class);
                    intent.putExtra("url", post_video);
                    startActivity(intent);
                }
            }
        });

        audio_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post_audio.equals("")) {
                    Utils.showToast(context, getResources().getString(R.string.sorry_no_audio_attch));
                } else {
                    Log.e("here", "file " + post_audio);

//                    Intent intent = new Intent();
//                    intent.setAction(android.content.Intent.ACTION_VIEW);
//                    File file = new File(post_audio);
//                    intent.setPackage("com.google.android.music");
//                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
//                    startActivity(intent);

                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(post_audio));
                    startActivity(in);
                }
            }
        });

        if (savePref.getUserType().equals("1")) {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading_blue).into(imageViewTarget);
        } else {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loading_image);
            Glide.with(this).load(R.raw.loading).into(imageViewTarget);
        }
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.posted_job_details);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
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

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        Log.e("here ", "count " + postedJobDetailsList.size());
        returnIntent.putExtra("is_delete_quote", is_quote_delete);
        returnIntent.putExtra("is_delete_job", false);
        setResult(RESULT_OK, returnIntent);

        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    public void addRequst(final int position) {
        selected_position = position;
        loading_layout.setVisibility(View.VISIBLE);
        scroll_view.setVisibility(View.GONE);
        loading.setText(getResources().getString(R.string.we_are_contacting) + " " + postedJobDetailsList.get(position).getSeller_name() + " \n "+getResources().getString(R.string.please_wait));

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.BUYER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.SELLER_ID, postedJobDetailsList.get(position).getSeller_id());
        formBuilder.add(AllOmorniParameters.REQUEST_LAT, req_lat);
        formBuilder.add(AllOmorniParameters.REQUEST_LONG, req_lng);
        formBuilder.add(AllOmorniParameters.START_DATE, work_date);
        formBuilder.add(AllOmorniParameters.START_TIME, work_time);
        formBuilder.add(AllOmorniParameters.REQ_LOCATION, job_location);
        formBuilder.add(AllOmorniParameters.PRICE, postedJobDetailsList.get(position).getPrice());
        formBuilder.add(AllOmorniParameters.JOB_ID, postedJobDetailsList.get(position).getJob_id());
        formBuilder.add(AllOmorniParameters.CREATED_DATE, String.valueOf(System.currentTimeMillis() / 1000));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.ADD_REQUEST_POSTED_JOB, formBody) {
            @Override
            public void getValueParse(String result) {

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");

                        } else {
                            scroll_view.setVisibility(View.VISIBLE);
                            loading_layout.setVisibility(View.GONE);
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


    private void getPostJobDetails(boolean showProgress) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (showProgress)
            progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.JOB_ID, job_id);
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.JOB_DETAILS_LISTING, formBody) {
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
                        postedJobDetailsList = new ArrayList<PostJobsDetailsResponse>();
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            scroll_view.setVisibility(View.VISIBLE);

                            selected_seller_id = status.getString("selected_seller");
                            JSONObject body = jsonObject.getJSONObject("body");

                            if (savePref.getUserType().equals("1")) {
                                if (body.getString("job_type").equals(getResources().getString(R.string.plumber_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.plumber_seller));
//                                    job_name.setText(context.getResources().getString(R.string.plumber));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.electrician_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.electrician_seller));
//                                    job_name.setText(context.getResources().getString(R.string.electrician));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.carpenter_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.carpenter_seller));
//                                    job_name.setText(context.getResources().getString(R.string.carpenter));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.ac_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.ac_seller));
//                                    job_name.setText(context.getResources().getString(R.string.ac));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.satellite_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.satellite_seller));
//                                    job_name.setText(context.getResources().getString(R.string.satellite));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.painter_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.painter_seller));
//                                    job_name.setText(context.getResources().getString(R.string.painter));
                                }
                            } else {
                                if (body.getString("job_type").equals(getResources().getString(R.string.plumber_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.plumber_buyer));
//                                    job_name.setText(context.getResources().getString(R.string.plumber));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.electrician_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.electrician_buyer));
//                                    job_name.setText(context.getResources().getString(R.string.electrician));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.carpenter_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.carpenter_buyer));
//                                    job_name.setText(context.getResources().getString(R.string.carpenter));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.ac_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.ac_buyer));
//                                    job_name.setText(context.getResources().getString(R.string.ac));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.satellite_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.satellite_buyer));
//                                    job_name.setText(context.getResources().getString(R.string.satellite));
                                } else if (body.getString("job_type").equals(getResources().getString(R.string.painter_id))) {
                                    image.setImageDrawable(context.getResources().getDrawable(R.drawable.painter_buyer));
//                                    job_name.setText(context.getResources().getString(R.string.painter));
                                }
                            }

                            id = body.getString("id");
                            buyer_id = body.getString("buyer_id");
                            String request_title = body.getString("request_title");
//                            String job_type = body.getString("job_type");
                            String job_description = body.getString("job_description");

                            work_date = body.getString("work_date");
                            work_time = body.getString("work_time");
                            post_audio = body.getString("post_audio");
                            post_video = body.getString("post_video");
                            req_lat = body.getString("latitude");
                            req_lng = body.getString("longitude");
                            processing_fee = body.getString("processing_fee");
                            vat_tax = body.getString("vat_tax");
                            job_location = body.getString("job_location");
                            String thumbnail_url = body.getString("thumb_url");


                            job_name.setText(request_title);
//                            titletext.setText(request_title);

                            String desc = "";

                            byte[] data = Base64.decode(job_description, Base64.DEFAULT);
                            try {
                                desc = new String(data, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            description.setText(desc);
                            workdate.setText(work_date);
                            worktime.setText(work_time);
                            location_text.setText(job_location);
                            if (selected_seller_id.equals("0")) {
                                delete_post.setVisibility(View.VISIBLE);
                            } else {
                                delete_post.setVisibility(View.GONE);
                            }
                            if (!thumbnail_url.equals("")) {

                                Glide.with(PostedJobsDetailActivity.this)
                                        .load(thumbnail_url).override(200, 200).centerCrop()
                                        .placeholder(R.drawable.camera_placeholder)
                                        .diskCacheStrategy(DiskCacheStrategy.RESULT).into(video_view);

                                Glide.with(PostedJobsDetailActivity.this).load(thumbnail_url)
                                        .override(300, 300).centerCrop()
                                        .placeholder(R.drawable.camera_placeholder)
                                        .diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageview);
                            }

                            JSONArray jsonArray = body.getJSONArray("applied_user");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                PostJobsDetailsResponse postedJobDetailObject = new PostJobsDetailsResponse();

                                postedJobDetailObject.setId(object.getString("id"));
                                postedJobDetailObject.setSeller_id(object.getString("seller_id"));
                                postedJobDetailObject.setJob_id(object.getString("job_id"));
                                postedJobDetailObject.setPrice(object.getString("price"));
                                postedJobDetailObject.setCreated_date(object.getString("created_date"));
                                postedJobDetailObject.setStatus(object.getString("status"));
                                postedJobDetailObject.setSeller_name(object.getString("seller_name"));
                                postedJobDetailObject.setSeller_image(object.getString("seller_image"));
                                postedJobDetailObject.setTotal_rating_user(object.getString("total_rating_user"));
                                postedJobDetailObject.setAvgrating(object.getString("avgrating"));
                                postedJobDetailObject.setDescription(object.getString("description"));
                                JSONArray languageArray = object.getJSONArray("language");
                                String language = "";
                                if (languageArray.length() > 0) {
                                    for (int j = 0; j < languageArray.length(); j++) {
                                        JSONObject jsonObjectLanguage = languageArray.getJSONObject(i);
                                        language += jsonObjectLanguage.getString("language") + " " + jsonObjectLanguage.getString("language_level") + ",";
                                    }
                                    postedJobDetailObject.setLanguage(language.substring(0, language.length() - 1));
                                } else {
                                    postedJobDetailObject.setLanguage(language);
                                }
                                postedJobDetailsList.add(postedJobDetailObject);
                            }
                            if (postedJobDetailsList.size() > 1) {
                                number_quotes.setText(postedJobDetailsList.size() + " " + getResources().getString(R.string.quotes));
                            } else {
                                number_quotes.setText(postedJobDetailsList.size() + " " + getResources().getString(R.string.quote));
                            }

                            deatilsAdapter = new PostJobsDeatilsAdapter(context, postedJobDetailsList, work_date, work_time, getIntent().getStringExtra("job_status"));
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setAdapter(deatilsAdapter);
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
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
            }
        };
        mAsync.execute();
    }

    private void deletePostJobDetails(boolean showProgress) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (showProgress)
            progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.POST_ID, id);
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.DELETE_POST, formBody) {
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
                            Utils.showToast(context, context.getResources().getString(R.string.job_deleted));
                            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("position", position);
                            returnIntent.putExtra("is_delete_job", true);
                            setResult(RESULT_OK, returnIntent);
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
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
            }
        };
        mAsync.execute();
    }

    public void deleteJobQuote(String job_id, String seller_id, final int pos) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SELLER_ID, seller_id);
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.JOB_ID, job_id);
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.DELETE_SELLER_QUOTE, formBody) {
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
                            is_quote_delete = true;
                            postedJobDetailsList.remove(pos);
                            deatilsAdapter.notifyDataSetChanged();
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
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
            }
        };
        mAsync.execute();
    }


}
