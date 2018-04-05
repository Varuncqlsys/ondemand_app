package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.model.PayPalResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class PostCommentBySellerActivity extends BaseActivity {
    private TextView order_number, service_date_value, buyer_name, post_comment;
    CircleImageView buyer_image;
    RatingBar rating;
    EditText edit_comment;
    private Toolbar toolbar;
    private TextView toolbar_title, service_date_text;
    PayPalResponse payPalResponse;
    SavePref savePref;
    String rating_value = "0.0";
    private LinearLayout rating_layout, main_layout;
    private TextView order_number_rating, service_date_value_rating, buyer_name_rating, comment_rating, close;
    private AppCompatRatingBar rating_rating;
    private CircleImageView buyer_image_rating;
    private TextView date, no_thanks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeSeller);
        if (Utils.isConfigRtl(PostCommentBySellerActivity.this)) {
            setContentView(R.layout.activity_post_comment_by_seller_rtl);
        } else {
            setContentView(R.layout.activity_post_comment_by_seller);
        }
        setToolbar();
        initialize();
        getRatingApi();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.order_completed);
    }

    private void initialize() {
        savePref = new SavePref(PostCommentBySellerActivity.this);
        payPalResponse = getIntent().getParcelableExtra("summary");
        order_number = (TextView) findViewById(R.id.order_number);
        service_date_value = (TextView) findViewById(R.id.service_date_value);
        buyer_name = (TextView) findViewById(R.id.buyer_name);
        service_date_text = (TextView) findViewById(R.id.service_date_text);
        buyer_image = (CircleImageView) findViewById(R.id.buyer_image);
        rating = (RatingBar) findViewById(R.id.rating);
        edit_comment = (EditText) findViewById(R.id.edit_comment);
        post_comment = (TextView) findViewById(R.id.post_comment);
        date = (TextView) findViewById(R.id.date);
        no_thanks = (TextView) findViewById(R.id.no_thanks);
        order_number_rating = (TextView) findViewById(R.id.order_number_rating);
        service_date_value_rating = (TextView) findViewById(R.id.service_date_value_rating);
        buyer_name_rating = (TextView) findViewById(R.id.buyer_name_rating);
        comment_rating = (TextView) findViewById(R.id.comment);
        close = (TextView) findViewById(R.id.close);
        rating_rating = (AppCompatRatingBar) findViewById(R.id.rating_rating);
        buyer_image_rating = (CircleImageView) findViewById(R.id.buyer_image_rating);


        rating_layout = (LinearLayout) findViewById(R.id.rating_layout);
        main_layout = (LinearLayout) findViewById(R.id.main_layout);

        no_thanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noThanksApi();
            }
        });

        post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityReceiver.isConnected()) {
                   /* if (edit_comment.getText().toString().length() == 0) {
                        Utils.showToast(PostCommentBySellerActivity.this, getResources().getString(R.string.enter_comment));
                    } else if (rating_value.equals("")) {
                        Utils.showToast(PostCommentBySellerActivity.this, getResources().getString(R.string.select_rating_for_buyer));
                    }
                    else {*/
                    postCommentApi();
//                    }
                } else {
                    Toast.makeText(PostCommentBySellerActivity.this, getResources().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                rating_value = String.valueOf(v);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void noThanksApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(PostCommentBySellerActivity.this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.FROM_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.TO_ID, payPalResponse.getBuyer_id());
        formBuilder.add(AllOmorniParameters.REQUEST_ID, payPalResponse.getRequest_id());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(PostCommentBySellerActivity.this, AllOmorniApis.NO_THANKS, formBody) {
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
                            Intent intent = new Intent(PostCommentBySellerActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.down_from_top, R.anim.down_from_top);
                        } else {
                            Utils.showToast(PostCommentBySellerActivity.this, status.getString("message"));

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

    private void postCommentApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(PostCommentBySellerActivity.this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.FROM_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.TO_ID, payPalResponse.getBuyer_id());
        formBuilder.add(AllOmorniParameters.REQUEST_ID, payPalResponse.getRequest_id());
        formBuilder.add(AllOmorniParameters.RATING, rating_value);
        formBuilder.add(AllOmorniParameters.COMMENT, edit_comment.getText().toString());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(PostCommentBySellerActivity.this, AllOmorniApis.ADD_REVIEW, formBody) {
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

                            Utils.showToast(PostCommentBySellerActivity.this, status.getString("message"));
                            Intent intent = new Intent(PostCommentBySellerActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.down_from_top, R.anim.down_from_top);

                        } else {
                            Utils.showToast(PostCommentBySellerActivity.this, status.getString("message"));

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

    private void getRatingApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(PostCommentBySellerActivity.this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.OPPONENT_ID, payPalResponse.getBuyer_id());
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.REQUEST_ID, payPalResponse.getRequest_id());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(PostCommentBySellerActivity.this, AllOmorniApis.GET_RATING, formBody) {
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
                            Log.e("see", "rating " + result);
                            if (jsonObject1.getString("is_ratted").equals("0")) {
                                main_layout.setVisibility(View.VISIBLE);
                                rating_layout.setVisibility(View.GONE);
                                setDataPostRating();
                            } else {
                                main_layout.setVisibility(View.GONE);
                                rating_layout.setVisibility(View.VISIBLE);
                                String seller_image = jsonObject1.getString("user_image");
                                String seller_name = jsonObject1.getString("user_name");
                                String rating = jsonObject1.getString("rating");
                                String comment = jsonObject1.getString("comment");
                                String req_id = jsonObject1.getString("request_id");
                                String rating_date = jsonObject1.getString("review_date");
                                String order_id = jsonObject1.getString("order_id");
                                setRatingData(seller_image, seller_name, rating, comment, req_id, rating_date, order_id);
                            }
                        } else {
                            Utils.checkAuthToken(PostCommentBySellerActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
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
        Intent intent = new Intent(PostCommentBySellerActivity.this, OrderSummerySellerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("from_my_order", true);
        intent.putExtra("req_id", payPalResponse.getRequest_id());
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    private void setRatingData(String image, String name, String rating, String comment, String req_id, String rating_rate, String order_id) {
        Glide.with(PostCommentBySellerActivity.this).load(image).override(150, 150).centerCrop().error(R.drawable.user_placeholder).diskCacheStrategy(DiskCacheStrategy.RESULT).into(buyer_image_rating);
        buyer_name_rating.setText(name);
        comment_rating.setText(comment);
        rating_rating.setRating(Float.parseFloat(rating));
        order_number_rating.setText(getResources().getString(R.string.order_no) + " " + order_id);
        if (payPalResponse.getIs_scheduled().equals("0")) {
            service_date_value_rating.setText(getResources().getString(R.string.date_colon) + " " + Utils.convertTimeStampDate(Long.parseLong(payPalResponse.getCreated_date())));
        } else {
            service_date_value_rating.setText(getResources().getString(R.string.date_colon) + " " + payPalResponse.getStart_date());
        }

        date.setText(Utils.convertTimeStampDateTime(Long.parseLong(rating_rate)));
    }

    private void setDataPostRating() {
        Glide.with(PostCommentBySellerActivity.this).load(payPalResponse.getUser_image()).override(150, 150).centerCrop().error(R.drawable.user_placeholder_seller).diskCacheStrategy(DiskCacheStrategy.RESULT).into(buyer_image);
        buyer_name.setText(payPalResponse.getBuyer_name());
        order_number.setText(getResources().getString(R.string.order_no) + " " + payPalResponse.getOrder_id());

        if (payPalResponse.getIs_scheduled().equals("0")) {
            service_date_value.setText(Utils.convertTimeStampDate(Long.parseLong(payPalResponse.getCreated_date())));
        } else {
            service_date_value.setText(payPalResponse.getStart_date());
        }

    }
}
