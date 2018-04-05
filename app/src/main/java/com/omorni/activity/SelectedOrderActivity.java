package com.omorni.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.omorni.model.OrderResponse;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectedOrderActivity extends BaseActivity implements View.OnClickListener {
    private SavePref savePref;
    private OrderResponse orderResponse;
    private TextView seller_name, number_users, sar_package_front, sar_package_back, sar_package_first, sar_package_second, languages, description;
    private CircleImageView seller_image;
    private AppCompatRatingBar rating;
    private RelativeLayout feature_layout, custome_layout;
    private TextView sar_package_custome;
    private LinearLayout rating_layout, activity_selected_order;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(SelectedOrderActivity.this);
        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_selected_order_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_selected_order);
        }
        initialize();
        setToolbar();
        setData();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_title.setText(orderResponse.getFirst_name() + " " + orderResponse.getLast_name());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
    }

    private void initialize() {
        orderResponse = getIntent().getParcelableExtra("selected_order");

        listView = (ListView) findViewById(R.id.listView);
        activity_selected_order = (LinearLayout) findViewById(R.id.activity_selected_order);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header;

        if (Utils.isConfigRtl(SelectedOrderActivity.this)) {
            if (savePref.getUserType().equals("1")) {
                header = (ViewGroup) inflater.inflate(R.layout.selected_order_header_seller_rtl, listView, false);
            } else {
                header = (ViewGroup) inflater.inflate(R.layout.selected_order_header_rtl, listView, false);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                header = (ViewGroup) inflater.inflate(R.layout.selected_order_header_seller, listView, false);
            } else {
                header = (ViewGroup) inflater.inflate(R.layout.selected_order_header, listView, false);
            }
        }


        listView.addHeaderView(header, null, false);

        seller_image = (CircleImageView) header.findViewById(R.id.seller_image);
        sar_package_custome = (TextView) header.findViewById(R.id.sar_package_custome);
        languages = (TextView) header.findViewById(R.id.languages);
        number_users = (TextView) header.findViewById(R.id.number_users);
        rating = (AppCompatRatingBar) header.findViewById(R.id.rating);
        seller_name = (TextView) header.findViewById(R.id.seller_name);
        sar_package_first = (TextView) header.findViewById(R.id.sar_package_first);
        sar_package_second = (TextView) header.findViewById(R.id.sar_package_second);
        description = (TextView) header.findViewById(R.id.description);
        sar_package_front = (TextView) header.findViewById(R.id.sar_package_front);
        sar_package_back = (TextView) header.findViewById(R.id.sar_package_back);
        feature_layout = (RelativeLayout) header.findViewById(R.id.feature_layout);
        custome_layout = (RelativeLayout) header.findViewById(R.id.custome_layout);
        rating_layout = (LinearLayout) header.findViewById(R.id.rating_layout);

        RatingReviewsAdapter adapter = new RatingReviewsAdapter(SelectedOrderActivity.this, orderResponse.getAllreviews());
        listView.setAdapter(adapter);

        feature_layout.setOnClickListener(this);
        custome_layout.setOnClickListener(this);
        rating_layout.setOnClickListener(this);

    }

    private void setData() {

        activity_selected_order.setVisibility(View.VISIBLE);

        // means buyer accepted purposal
        if (orderResponse.getRequest_type().equals("1")) {
            custome_layout.setVisibility(View.VISIBLE);
            feature_layout.setVisibility(View.GONE);
            sar_package_custome.setText(getResources().getString(R.string.sar) + " " + orderResponse.getPrice());
        }
        // means buyer requested service with package name
        else {
            custome_layout.setVisibility(View.GONE);
            feature_layout.setVisibility(View.VISIBLE);

            sar_package_front.setText(getResources().getString(R.string.sar) + " " + orderResponse.getNormal_charges());
            sar_package_back.setText(orderResponse.getName());
            sar_package_first.setText(getResources().getString(R.string.installation) + " " + orderResponse.getMain_hours() + " " + getResources().getString(R.string.hour));
            sar_package_second.setText(getResources().getString(R.string.additional) + " " + orderResponse.getAdditional_charges() + " " + getResources().getString(R.string.sar) + " / " + orderResponse.getExtra_hours() + " " + getResources().getString(R.string.hour));
        }

        if (savePref.getUserType().equals("1"))
            Glide.with(SelectedOrderActivity.this).load(orderResponse.getUser_image()).override(150, 150).centerCrop().placeholder(R.drawable.user_placeholder_seller).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        else
            Glide.with(SelectedOrderActivity.this).load(orderResponse.getUser_image()).override(150, 150).centerCrop().placeholder(R.drawable.user_placeholder).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);

        seller_name.setText(orderResponse.getFirst_name() + " " + orderResponse.getLast_name());
        rating.setRating(Float.parseFloat(orderResponse.getAvgrating()));
        number_users.setText("(" + orderResponse.getTotal_rating_user() + ")");
        description.setText(orderResponse.getDescription());
        languages.setText(orderResponse.getLanguage());

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.feature_layout:

                if (orderResponse.getStatus().equals("0")) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(SelectedOrderActivity.this);
                    alert.setTitle(getResources().getString(R.string.app_name)).setCancelable(false)
                            .setMessage(getResources().getString(R.string.wait_untill) + " " + orderResponse.getFirst_name() + " " + orderResponse.getLast_name() + " " + getResources().getString(R.string.accept_your_job))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } else if (orderResponse.getStatus().equals("1")) {
                    Intent intent2 = new Intent(SelectedOrderActivity.this, RequestSummaryActivity.class);
                    if (orderResponse.getIs_scheduled().equals("0"))
                        intent2.putExtra("service_now", false);
                    else
                        intent2.putExtra("service_now", true);
                    intent2.putExtra("from_my_order", true);
                    intent2.putExtra("start_date", orderResponse.getStart_date());
                    intent2.putExtra("start_time", orderResponse.getStart_time());
                    intent2.putExtra("is_scheduled", orderResponse.getIs_scheduled());
                    intent2.putExtra("req_id", orderResponse.getId());
                    intent2.putExtra("req_type", orderResponse.getRequest_type());
                    intent2.putExtra("processing_fee", orderResponse.getProcessing_fee());
                    intent2.putExtra("seller_data", orderResponse);
                    startActivity(intent2);
                } else if (orderResponse.getStatus().equals("2")) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(SelectedOrderActivity.this);
                    alert.setTitle(getResources().getString(R.string.app_name)).setCancelable(false)
                            .setMessage(orderResponse.getFirst_name() + " " + getResources().getString(R.string.already_rejected_job))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } else if (orderResponse.getStatus().equals("3") || orderResponse.getStatus().equals("4") || orderResponse.getStatus().equals("5") || orderResponse.getStatus().equals("6") || orderResponse.getStatus().equals("7")) {
                    Intent intent = new Intent(SelectedOrderActivity.this, OrderSummeryActivity.class);
                    intent.putExtra("from_my_order", true);
                    intent.putExtra("req_id", orderResponse.getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } else if (orderResponse.getStatus().equals("8")) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(SelectedOrderActivity.this);
                    alert.setTitle(getResources().getString(R.string.app_name)).setCancelable(false)
                            .setMessage(getResources().getString(R.string.you_cancel_job))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } else if (orderResponse.getStatus().equals("9")) {
                    Intent intent = new Intent(SelectedOrderActivity.this, OrderSummeryActivity.class);
                    intent.putExtra("from_my_order", true);
                    intent.putExtra("req_id", orderResponse.getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }

                /*if (orderResponse.getStatus().equals("1") || orderResponse.getStatus().equals("3") || orderResponse.getStatus().equals("4")) {
                    Intent intent = new Intent(SelectedOrderActivity.this, OrderSummeryActivity.class);
                    intent.putExtra("from_my_order", true);
                    intent.putExtra("req_id", orderResponse.getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }*/
                break;

            case R.id.custome_layout:
                if (orderResponse.getStatus().equals("0")) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(SelectedOrderActivity.this);
                    alert.setTitle(getResources().getString(R.string.app_name)).setCancelable(false)
                            .setMessage(getResources().getString(R.string.wait_untill) + " " + orderResponse.getFirst_name() + " " + orderResponse.getLast_name() + " " + getResources().getString(R.string.accept_your_job))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } else if (orderResponse.getStatus().equals("1")) {
                    Intent intent2 = new Intent(SelectedOrderActivity.this, RequestSummaryActivity.class);
                    if (orderResponse.getIs_scheduled().equals("0"))
                        intent2.putExtra("service_now", false);
                    else
                        intent2.putExtra("service_now", true);
                    intent2.putExtra("from_my_order", true);
                    intent2.putExtra("start_date", orderResponse.getStart_date());
                    intent2.putExtra("start_time", orderResponse.getStart_time());
                    intent2.putExtra("is_scheduled", orderResponse.getIs_scheduled());
                    intent2.putExtra("req_id", orderResponse.getId());
                    intent2.putExtra("req_type", orderResponse.getRequest_type());
                    intent2.putExtra("processing_fee", orderResponse.getProcessing_fee());
                    intent2.putExtra("seller_data", orderResponse);
                    startActivity(intent2);
                } else if (orderResponse.getStatus().equals("2")) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(SelectedOrderActivity.this);
                    alert.setTitle(getResources().getString(R.string.app_name)).setCancelable(false)
                            .setMessage(orderResponse.getFirst_name() + " " + getResources().getString(R.string.already_rejected_job))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } else if (orderResponse.getStatus().equals("3") || orderResponse.getStatus().equals("4") || orderResponse.getStatus().equals("5") || orderResponse.getStatus().equals("6") || orderResponse.getStatus().equals("7")) {
                    Intent intent = new Intent(SelectedOrderActivity.this, OrderSummeryActivity.class);
                    intent.putExtra("from_my_order", true);
                    intent.putExtra("req_id", orderResponse.getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }

                /*if (orderResponse.getStatus().equals("1") || orderResponse.getStatus().equals("3") || orderResponse.getStatus().equals("4")) {
                    Intent intent = new Intent(SelectedOrderActivity.this, OrderSummeryActivity.class);
                    intent.putExtra("from_my_order", true);
                    intent.putExtra("req_id", orderResponse.getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }*/
                break;

            case R.id.rating_layout:
                showRatingDialog();
                break;

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

    private void showRatingDialog() {
        Dialog dialog = new Dialog(SelectedOrderActivity.this);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.rating_list);
        dialog.show();

        ListView list = (ListView) dialog.findViewById(R.id.listView);
        RatingReviewsAdapter adapter = new RatingReviewsAdapter(SelectedOrderActivity.this, orderResponse.getAllreviews());
        list.setAdapter(adapter);

    }
}
