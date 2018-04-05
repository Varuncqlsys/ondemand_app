package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.model.SellerAvailableJobsResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class AvailableJobDetail extends BaseActivity {
    TextView job_name, date, time, description, lipsun_text, username, number, order_status;
    RatingBar rating;
    ImageView imageview, video_view, audio_view;
    Button provide_quote, cancel_quote;
    ArrayList<SellerAvailableJobsResponse> available_arraylist;
    String selcetd_position;
    TextView title;
    String videoThumb = "";
    AvailableJobDetail context;
    SavePref savePref;
    Dialog dialog;
    boolean flag;
    ProgressDialog progressDialog;
    CircleImageView type_image, user_image, my_image;
    private TextView posted_date, my_name, quotation_date, quote_price, completion_date;
    private RelativeLayout quote_layout;
    View view;
    private LinearLayout order_sttatus_layoyut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeSeller);
        if (Utils.isConfigRtl(AvailableJobDetail.this)) {
            setContentView(R.layout.activity_available_job_detail_rtl);
        } else {
            setContentView(R.layout.activity_available_job_detail);
        }

        context = this;
        savePref = new SavePref(context);
        initialize();
        setToolbar();
        setData();
    }


    private void initialize() {
        available_arraylist = getIntent().getParcelableArrayListExtra("array");
        selcetd_position = getIntent().getStringExtra("selected_position");
        flag = getIntent().getBooleanExtra("flag", false);

        videoThumb = available_arraylist.get(Integer.parseInt(selcetd_position)).getPost_video();
        order_sttatus_layoyut = (LinearLayout) findViewById(R.id.order_sttatus_layoyut);
        type_image = (CircleImageView) findViewById(R.id.image);
        job_name = (TextView) findViewById(R.id.job_name);
        completion_date = (TextView) findViewById(R.id.completion_date);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        description = (TextView) findViewById(R.id.description);
        lipsun_text = (TextView) findViewById(R.id.lipsun_text);
        number = (TextView) findViewById(R.id.number);
        username = (TextView) findViewById(R.id.username);
        posted_date = (TextView) findViewById(R.id.posted_date);
        order_status = (TextView) findViewById(R.id.order_status);
        quote_layout = (RelativeLayout) findViewById(R.id.quote_layout);
        view = (View) findViewById(R.id.view);

        my_name = (TextView) findViewById(R.id.my_name);
        quotation_date = (TextView) findViewById(R.id.quotation_date);
        quote_price = (TextView) findViewById(R.id.quote_price);

        rating = (RatingBar) findViewById(R.id.rating);
        imageview = (ImageView) findViewById(R.id.image_view);
        video_view = (ImageView) findViewById(R.id.video_view);
        audio_view = (ImageView) findViewById(R.id.audio_view);
        provide_quote = (Button) findViewById(R.id.provide_quote);
        cancel_quote = (Button) findViewById(R.id.cancel_quote);
        user_image = (CircleImageView) findViewById(R.id.user_image);
        my_image = (CircleImageView) findViewById(R.id.my_image);
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);

        if (flag)
            toolbar_title.setText(R.string.update_quote);
        else
            toolbar_title.setText(R.string.job_detail);

    }

    private void setData() {
        if (savePref.getUserType().equals("1")) {
            if (available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_type().equals(getResources().getString(R.string.plumber_id))) {
                type_image.setImageDrawable(context.getResources().getDrawable(R.drawable.plumber_seller));
            } else if (available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_type().equals(getResources().getString(R.string.electrician_id))) {
                type_image.setImageDrawable(context.getResources().getDrawable(R.drawable.electrician_seller));
            } else if (available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_type().equals(getResources().getString(R.string.carpenter_id))) {
                type_image.setImageDrawable(context.getResources().getDrawable(R.drawable.carpenter_seller));
            } else if (available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_type().equals(getResources().getString(R.string.ac_id))) {
                type_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ac_seller));
            } else if (available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_type().equals(getResources().getString(R.string.satellite_id))) {
                type_image.setImageDrawable(context.getResources().getDrawable(R.drawable.satellite_seller));
            } else if (available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_type().equals(getResources().getString(R.string.painter_id))) {
                type_image.setImageDrawable(context.getResources().getDrawable(R.drawable.painter_seller));
            }
        }

        if (savePref.getUserType().equals("1")) {
            Glide.with(AvailableJobDetail.this).load(savePref.getUserImage())
                    .override(170, 170).centerCrop().placeholder(R.drawable.user_placeholder_seller)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(my_image);
        } else {
            Glide.with(AvailableJobDetail.this).load(savePref.getUserImage())
                    .override(170, 170).centerCrop().placeholder(R.drawable.user_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(my_image);
        }


        if (!available_arraylist.get(Integer.parseInt(selcetd_position)).getThumbnail().equals("")) {

            Glide.with(AvailableJobDetail.this)
                    .load(available_arraylist.get(Integer.parseInt(selcetd_position)).getThumbnail()).override(200, 200).centerCrop()
                    .placeholder(R.drawable.camera_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(video_view);

            Glide.with(AvailableJobDetail.this).load(available_arraylist.get(Integer.parseInt(selcetd_position)).getThumbnail())
                    .override(300, 300).centerCrop()
                    .placeholder(R.drawable.camera_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageview);

        }


        if (flag) {
            provide_quote.setVisibility(View.GONE);
            cancel_quote.setVisibility(View.VISIBLE);
            quote_layout.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);

            if (savePref.getUserType().equals("1")) {
                Glide.with(AvailableJobDetail.this).load(savePref.getUserImage())
                        .override(170, 170).centerCrop().placeholder(R.drawable.user_placeholder_seller)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT).into(my_image);
            } else {
                Glide.with(AvailableJobDetail.this).load(savePref.getUserImage())
                        .override(170, 170).centerCrop().placeholder(R.drawable.user_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT).into(my_image);
            }

            my_name.setText(savePref.getFirstname() + " " + savePref.getLastname());
            quote_price.setText(getResources().getString(R.string.sar) + " " + available_arraylist.get(Integer.parseInt(selcetd_position)).getPrice());
            if (available_arraylist.get(Integer.parseInt(selcetd_position)).getQuote_date() != null)
                quotation_date.setText(Utils.convertTimeStampDateTime(Long.parseLong(available_arraylist.get(Integer.parseInt(selcetd_position)).getQuote_date())));

        } else {
            provide_quote.setVisibility(View.VISIBLE);
            cancel_quote.setVisibility(View.GONE);
            quote_layout.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }

        Glide.with(AvailableJobDetail.this)
                .load(available_arraylist.get(Integer.parseInt(selcetd_position)).getBuyer_image()).override(110, 110).centerCrop().placeholder(R.drawable.user_placeholder_seller)
                .diskCacheStrategy(DiskCacheStrategy.NONE).into(user_image);
        lipsun_text.setText(available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_location());
        job_name.setText(available_arraylist.get(Integer.parseInt(selcetd_position)).getRequest_title());
        date.setText(available_arraylist.get(Integer.parseInt(selcetd_position)).getWork_date());
        time.setText(available_arraylist.get(Integer.parseInt(selcetd_position)).getWork_time());

        String desc = "";

        byte[] data = Base64.decode(available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_description(), Base64.DEFAULT);
        try {
            desc = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        description.setText(desc);

        username.setText(available_arraylist.get(Integer.parseInt(selcetd_position)).getFirst_name() + " " + available_arraylist.get(Integer.parseInt(selcetd_position)).getLast_name());

        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#FFD700"), PorterDuff.Mode.SRC_ATOP); // for filled stars
        stars.getDrawable(1).setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_ATOP); // for half filled stars
        stars.getDrawable(0).setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP); // for empty stars
        rating.setRating(Float.parseFloat(available_arraylist.get(Integer.parseInt(selcetd_position)).getAvgrating()));

        number.setText(available_arraylist.get(Integer.parseInt(selcetd_position)).getDistance());
        posted_date.setText(Utils.convertTimeStampDateTime(Long.parseLong(available_arraylist.get(Integer.parseInt(selcetd_position)).getCreated_date())));

        provide_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(AvailableJobDetail.this, R.style.Theme_Dialog);
                dialog.setContentView(R.layout.provide_quote_dialog);
                final EditText editText = (EditText) dialog.findViewById(R.id.amount);
                Button button = (Button) dialog.findViewById(R.id.submit);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editText.getText().toString().equals("")) {
                            Utils.showToast(context, getResources().getString(R.string.price_error));
                        } else {
                            getAvailableJobsSubmitApi(true, editText.getText().toString());
                        }
                    }
                });
                dialog.show();
            }
        });

        cancel_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAvailableJobsCancelApi(true);
            }
        });

        video_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoThumb.equals("")) {
                    Utils.showToast(context, getResources().getString(R.string.no_video_attached));
                } else {
                    Intent intent = new Intent(AvailableJobDetail.this, VideoPlayActivity.class);
                    intent.putExtra("url", videoThumb);
                    startActivity(intent);
                }
            }
        });

        audio_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (available_arraylist.get(Integer.parseInt(selcetd_position)).getPost_audio().equals("")) {
                    Utils.showToast(context, getResources().getString(R.string.no_audio_attached));
                } else {
                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(available_arraylist.get(Integer.parseInt(selcetd_position)).getPost_audio()));
                    startActivity(in);
                }
            }
        });

        if (flag) {
            if (available_arraylist.get(Integer.parseInt(selcetd_position)).getOrder_status().equals("4") || available_arraylist.get(Integer.parseInt(selcetd_position)).getOrder_status().equals("7")) {
                cancel_quote.setVisibility(View.GONE);
                provide_quote.setVisibility(View.GONE);
                order_sttatus_layoyut.setVisibility(View.VISIBLE);
                order_status.setText(getResources().getString(R.string.complete));
                completion_date.setText(Utils.convertTimeStampDateTime(Long.parseLong(available_arraylist.get(Integer.parseInt(selcetd_position)).getCompleted_date())));
            }
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


    private void getAvailableJobsSubmitApi(boolean showProgress, final String price) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        //if (showProgress)
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SELLER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.JOB_ID, available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_id());
        formBuilder.add(AllOmorniParameters.PRICE, price);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.Add_SELLER_QUOTE, formBody) {
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
                dialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    Log.e("result", "here " + result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            SellerAvailableJobsResponse sellerAvailableJobsResponse = available_arraylist.get(Integer.parseInt(selcetd_position));
                            sellerAvailableJobsResponse.setPrice(jsonObject1.getString("price"));
                            sellerAvailableJobsResponse.setQuote_date(jsonObject1.getString("created_date"));
                            sellerAvailableJobsResponse.setOrder_status("0");
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("selected_available_job", sellerAvailableJobsResponse);
                            returnIntent.putExtra("selected_position", selcetd_position);
                            setResult(RESULT_OK, returnIntent);
                            finish();

                            Utils.showToast(context, status.getString("message"));
                        } else {
                            Utils.checkAuthToken(AvailableJobDetail.this, status.getString("auth_token"), status.getString("message"), savePref);
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

    private void getAvailableJobsCancelApi(boolean showProgress) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        //if (showProgress)
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SELLER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.JOB_ID, available_arraylist.get(Integer.parseInt(selcetd_position)).getJob_id());

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
                //  dialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("selected_available_job", available_arraylist.get(Integer.parseInt(selcetd_position)));
                            returnIntent.putExtra("selected_position", selcetd_position);
                            setResult(RESULT_OK, returnIntent);
                            finish();

                            Utils.showToast(context, status.getString("message"));
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
