package com.omorni.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.adapter.RatingReviewsAdapter;
import com.omorni.model.AllPackageResponse;
import com.omorni.model.AllReviewsResponse;
import com.omorni.model.SellerDetailResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class FavSellerDetailActivity extends BaseActivity implements View.OnClickListener {
    private ListView listView;
    private RatingReviewsAdapter adapter;
    private Toolbar toolbar;
    private TextView toolbar_title, seller_name, number_users, languages, description, sar_50_first, sar_50_second, sar_50_front, sar_50_back,
            sar_80_front, sar_80_back, sar_80_first, sar_80_second, sar_120_front, sar_120_back, sar_120_first, sar_120_second, rating_title;
    private Context context;
    private String seller_id = "", omorni_procesing_fee = "",vat_tax="";
    private CircleImageView seller_image;
    AppCompatRatingBar rating;
    private LinearLayout rating_layout;
    private SellerDetailResponse sellerDetailResponse;
    SavePref savepref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savepref = new SavePref(FavSellerDetailActivity.this);
        if (savepref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_seller_detail_seller);
        } else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_seller_detail);
        }

        initialize();
        setToolbar();
        SellerDetailAPi();
    }

    private void initialize() {
        context = FavSellerDetailActivity.this;
        seller_id = getIntent().getStringExtra("seller_id");
        omorni_procesing_fee = getIntent().getStringExtra("omorni_procesing_fee");
        vat_tax = getIntent().getStringExtra("vat_tax");
        savepref = new SavePref(context);
        listView = (ListView) findViewById(R.id.listView);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header;
        if (Utils.isConfigRtl(FavSellerDetailActivity.this)) {
            if (savepref.getUserType().equals("1")) {
                header = (ViewGroup) inflater.inflate(R.layout.seller_detail_header_seller_rtl, listView, false);
            } else {
                header = (ViewGroup) inflater.inflate(R.layout.seller_detail_header_rtl, listView, false);
            }
        } else {
            if (savepref.getUserType().equals("1")) {
                header = (ViewGroup) inflater.inflate(R.layout.seller_detail_header_seller, listView, false);
            } else {
                header = (ViewGroup) inflater.inflate(R.layout.seller_detail_header, listView, false);
            }
        }

        listView.addHeaderView(header, null, false);

        seller_image = (CircleImageView) header.findViewById(R.id.seller_image);
        seller_name = (TextView) header.findViewById(R.id.seller_name);
        rating = (AppCompatRatingBar) header.findViewById(R.id.rating);
        number_users = (TextView) header.findViewById(R.id.number_users);
        languages = (TextView) header.findViewById(R.id.languages);
        description = (TextView) header.findViewById(R.id.description);
        sar_50_first = (TextView) header.findViewById(R.id.sar_50_first);
        sar_50_second = (TextView) header.findViewById(R.id.sar_50_second);
        sar_50_front = (TextView) header.findViewById(R.id.sar_50_front);
        sar_50_back = (TextView) header.findViewById(R.id.sar_50_back);
        sar_80_front = (TextView) header.findViewById(R.id.sar_80_front);
        sar_80_back = (TextView) header.findViewById(R.id.sar_80_back);
        sar_80_first = (TextView) header.findViewById(R.id.sar_80_first);
        sar_80_second = (TextView) header.findViewById(R.id.sar_80_second);
        sar_120_front = (TextView) header.findViewById(R.id.sar_120_front);
        sar_120_back = (TextView) header.findViewById(R.id.sar_120_back);
        sar_120_first = (TextView) header.findViewById(R.id.sar_120_first);
        sar_120_second = (TextView) header.findViewById(R.id.sar_120_second);
        rating_title = (TextView) header.findViewById(R.id.rating_title);
        rating_layout = (LinearLayout) header.findViewById(R.id.rating_layout);

        RelativeLayout sar_50_layout = (RelativeLayout) header.findViewById(R.id.sar_50_layout);
        RelativeLayout sar_80_layout = (RelativeLayout) header.findViewById(R.id.sar_80_layout);
        RelativeLayout sar_120_layout = (RelativeLayout) header.findViewById(R.id.sar_120_layout);

        sar_50_layout.setOnClickListener(this);
        sar_80_layout.setOnClickListener(this);
        sar_120_layout.setOnClickListener(this);
        rating_layout.setOnClickListener(this);
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
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
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sar_50_layout:
                if (sellerDetailResponse.getAllpackage().size() > 0) {
                    Intent intent = new Intent(context, SelectedFavSellerPackageActivity.class);
                    intent.putExtra("selected_package", sellerDetailResponse);
                    intent.putExtra("package", "50");
                    intent.putExtra("from_sellerdetail", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } else {
                    Utils.showToast(context, sellerDetailResponse.getFirst_name() + " " + context.getResources().getString(R.string.no_basic));
                }
                break;
            case R.id.sar_80_layout:
                if (sellerDetailResponse.getAllpackage().size() > 1) {
                    Intent intent1 = new Intent(context, SelectedFavSellerPackageActivity.class);
                    intent1.putExtra("selected_package", sellerDetailResponse);
                    intent1.putExtra("package", "80");
                    intent1.putExtra("from_sellerdetail", true);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } else {
                    Utils.showToast(context, sellerDetailResponse.getFirst_name() + " " + context.getResources().getString(R.string.no_standard));
                }

                break;
            case R.id.sar_120_layout:
                if (sellerDetailResponse.getAllpackage().size() > 2) {
                    Intent intent2 = new Intent(context, SelectedFavSellerPackageActivity.class);
                    intent2.putExtra("selected_package", sellerDetailResponse);
                    intent2.putExtra("package", "120");
                    intent2.putExtra("from_sellerdetail", true);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } else {
                    Utils.showToast(context, sellerDetailResponse.getFirst_name() + " " + context.getResources().getString(R.string.no_premium));
                }
                break;

            case R.id.rating_layout:
                showRatingDialog();
                break;
        }

    }

    private void SellerDetailAPi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.SELLER_ID, seller_id);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.SELLER_DETAIL, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            listView.setVisibility(View.VISIBLE);
                            sellerDetailResponse = new SellerDetailResponse();
                            ArrayList<AllReviewsResponse> arrayListAllReviews = new ArrayList<AllReviewsResponse>();
                            ArrayList<AllPackageResponse> arrayListAllPackages = new ArrayList<AllPackageResponse>();
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            sellerDetailResponse.setId(jsonObjectBody.getString("id"));
                            sellerDetailResponse.setFirst_name(jsonObjectBody.getString("first_name"));
                            sellerDetailResponse.setLast_name(jsonObjectBody.getString("last_name"));
                            sellerDetailResponse.setLocation(jsonObjectBody.getString("location"));
                            sellerDetailResponse.setUser_image(jsonObjectBody.getString("user_image"));
                            sellerDetailResponse.setEmail(jsonObjectBody.getString("email"));
                            sellerDetailResponse.setMobile(jsonObjectBody.getString("mobile"));
                            sellerDetailResponse.setJob_description(jsonObjectBody.getString("job_description"));

                            JSONArray jsonArray = jsonObjectBody.getJSONArray("language");

                            String language = "";
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObjectLanguage = jsonArray.getJSONObject(i);
                                    language += jsonObjectLanguage.getString("language") + " " + jsonObjectLanguage.getString("language_level") + ",";
                                }
                                sellerDetailResponse.setLanguage(language.substring(0, language.length() - 1));
                            } else {
                                sellerDetailResponse.setLanguage(language);
                            }

                            sellerDetailResponse.setTotalrating_user(jsonObjectBody.getString("total_rating_user"));
                            sellerDetailResponse.setAvgrating(jsonObjectBody.getString("avgrating"));
                            sellerDetailResponse.setSeller_category(jsonObjectBody.getString("category"));
                            sellerDetailResponse.setOmorni_procesing_fee(omorni_procesing_fee);
                            sellerDetailResponse.setVat_tax(vat_tax);

                            JSONArray arrayAllreviews = jsonObjectBody.getJSONArray("allreviews");
                            if (arrayAllreviews.length() > 0) {
                                for (int i = 0; i < arrayAllreviews.length(); i++) {
                                    AllReviewsResponse allReviewsResponse = new AllReviewsResponse();
                                    JSONObject jsonObject1 = arrayAllreviews.getJSONObject(i);
                                    allReviewsResponse.setFirst_name(jsonObject1.getString("first_name"));
                                    allReviewsResponse.setLast_name(jsonObject1.getString("last_name"));
                                    allReviewsResponse.setRating(jsonObject1.getString("rating"));
                                    allReviewsResponse.setComment(jsonObject1.getString("comment"));
                                    allReviewsResponse.setUser_image(jsonObject1.getString("user_image"));
                                    allReviewsResponse.setRating_date(jsonObject1.getString("review_date"));
                                    arrayListAllReviews.add(allReviewsResponse);
                                }
                            }

                            sellerDetailResponse.setAllreviews(arrayListAllReviews);
                            JSONArray arrayAllpackage = jsonObjectBody.getJSONArray("allpackage");
                            if (arrayAllpackage.length() > 0) {
                                for (int i = 0; i < arrayAllpackage.length(); i++) {
                                    AllPackageResponse allPackageResponse = new AllPackageResponse();
                                    JSONObject jsonObject1 = arrayAllpackage.getJSONObject(i);
                                    allPackageResponse.setId(jsonObject1.getString("id"));
                                    allPackageResponse.setNormal_charges(jsonObject1.getString("normal_charges"));
                                    allPackageResponse.setAdditional_charges(jsonObject1.getString("additional_charges"));
                                    allPackageResponse.setStatus(jsonObject1.getString("status"));
                                    allPackageResponse.setPackage_name(jsonObject1.getString("package_name"));
                                    allPackageResponse.setPackage_description(jsonObject1.getString("package_description"));
                                    allPackageResponse.setMain_hours(jsonObject1.getString("main_hours"));
                                    allPackageResponse.setExtra_hours(jsonObject1.getString("extra_hours"));
                                    allPackageResponse.setPackage_status(jsonObject1.getString("package_status"));
                                    allPackageResponse.setDescription(jsonObject1.getString("description"));
                                    arrayListAllPackages.add(allPackageResponse);
                                }
                            }
                            toolbar_title.setText(sellerDetailResponse.getFirst_name() + " " + sellerDetailResponse.getLast_name());

                            if (jsonObjectBody.getString("category").equals(getResources().getString(R.string.plumber_id))) {
                                toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_1, 0, 0, 0);
                            } else if (jsonObjectBody.getString("category").equals(getResources().getString(R.string.electrician_id))) {
                                toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_2, 0, 0, 0);
                            } else if (jsonObjectBody.getString("category").equals(getResources().getString(R.string.carpenter_id))) {
                                toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_3, 0, 0, 0);
                            } else if (jsonObjectBody.getString("category").equals(getResources().getString(R.string.ac_id))) {
                                toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_4, 0, 0, 0);
                            } else if (jsonObjectBody.getString("category").equals(getResources().getString(R.string.satellite_id))) {
                                toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_5, 0, 0, 0);
                            } else if (jsonObjectBody.getString("category").equals(getResources().getString(R.string.painter_id))) {
                                toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_6, 0, 0, 0);
                            }

                            sellerDetailResponse.setAllpackage(arrayListAllPackages);
                            setData(sellerDetailResponse);
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showToast(context, getResources().getString(R.string.internet_error));
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    private void setData(SellerDetailResponse sellerDetailResponse) {
        adapter = new RatingReviewsAdapter(FavSellerDetailActivity.this, sellerDetailResponse.getAllreviews());
        listView.setAdapter(adapter);

        if (savepref.getUserType().equals("1"))
            Glide.with(context).load(sellerDetailResponse.getUser_image()).override(130, 130).centerCrop().placeholder(R.drawable.user_placeholder_seller).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        else
            Glide.with(context).load(sellerDetailResponse.getUser_image()).override(130, 130).centerCrop().placeholder(R.drawable.user_placeholder).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);

        seller_name.setText(sellerDetailResponse.getFirst_name() + " " + sellerDetailResponse.getLast_name());
        rating.setRating(Float.parseFloat(sellerDetailResponse.getAvgrating()));
        number_users.setText("(" + sellerDetailResponse.getTotalrating_user() + ")");
        languages.setText(sellerDetailResponse.getLanguage());
        description.setText(sellerDetailResponse.getJob_description());

        if (sellerDetailResponse.getAllpackage().size() > 0) {
            sar_50_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(0).getNormal_charges());
            sar_50_back.setText(sellerDetailResponse.getAllpackage().get(0).getPackage_name());

//            sar_50_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_50_first.setText(getResources().getString(R.string.basic_package_upto) + " " + sellerDetailResponse.getAllpackage().get(0).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_50_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(0).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(0).getExtra_hours() + " " + getResources().getString(R.string.hour));

            sar_80_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(1).getNormal_charges());
            sar_80_back.setText(sellerDetailResponse.getAllpackage().get(1).getPackage_name());

//            sar_80_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_80_first.setText(getResources().getString(R.string.standard_package_upto) + " " + sellerDetailResponse.getAllpackage().get(1).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_80_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(1).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(1).getExtra_hours() + " " + getResources().getString(R.string.hour));

            sar_120_front.setText(getResources().getString(R.string.sar) + " " + sellerDetailResponse.getAllpackage().get(2).getNormal_charges());
            sar_120_back.setText(sellerDetailResponse.getAllpackage().get(2).getPackage_name());

//            sar_120_first.setText(getResources().getString(R.string.installation) + " " + sellerDetailResponse.getAllpackage().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_120_first.setText(getResources().getString(R.string.premium_package_upto) + " " + sellerDetailResponse.getAllpackage().get(2).getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_120_second.setText(getResources().getString(R.string.additional) + " " + sellerDetailResponse.getAllpackage().get(2).getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + sellerDetailResponse.getAllpackage().get(2).getExtra_hours() + " " + getResources().getString(R.string.hour));
        }
    }

    private void showRatingDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.rating_list);
        dialog.show();
        ListView list = (ListView) dialog.findViewById(R.id.listView);
        RatingReviewsAdapter adapter = new RatingReviewsAdapter(FavSellerDetailActivity.this, sellerDetailResponse.getAllreviews());
        list.setAdapter(adapter);
    }
}
