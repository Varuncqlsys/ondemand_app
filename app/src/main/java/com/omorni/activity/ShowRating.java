package com.omorni.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
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
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowRating extends BaseActivity {
    private SavePref savePref;
    private LinearLayout rating_layout;
    private TextView order_number_rating, order_date_rating, seller_name, comment, close, date;
    private CircleImageView seller_image;
    private AppCompatRatingBar rating_rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(ShowRating.this);
        if (Utils.isConfigRtl(ShowRating.this)) {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_show_rating_seller_rtl);
            }// 2 means buyer
            else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_show_rating_rtl);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_show_rating_seller);
            }// 2 means buyer
            else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_show_rating);
            }
        }

        setToolbar();
        initialize();
        setRatingData();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    private void initialize() {

        rating_layout = (LinearLayout) findViewById(R.id.rating_layout);
        date = (TextView) findViewById(R.id.date);
        order_number_rating = (TextView) findViewById(R.id.order_number_rating);
        order_date_rating = (TextView) findViewById(R.id.order_date_rating);

        seller_image = (CircleImageView) findViewById(R.id.seller_image);
        seller_name = (TextView) findViewById(R.id.seller_name);
        close = (TextView) findViewById(R.id.close);
        rating_rating = (AppCompatRatingBar) findViewById(R.id.rating_rating);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            }
        });

    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        toolbar_title.setText(R.string.order_completed);
    }

    private void setRatingData() {
        rating_layout.setVisibility(View.VISIBLE);
        if (savePref.getUserType().equals("1"))
            Glide.with(ShowRating.this).load(getIntent().getStringExtra("user_image")).override(150, 150).centerCrop().placeholder(R.drawable.user_placeholder_seller).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);
        else
            Glide.with(ShowRating.this).load(getIntent().getStringExtra("user_image")).override(150, 150).centerCrop().placeholder(R.drawable.user_placeholder).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(seller_image);

        seller_name.setText(getIntent().getStringExtra("first_name") + " " + getIntent().getStringExtra("last_name"));
        rating_rating.setRating(Float.parseFloat(getIntent().getStringExtra("rating")));
        order_number_rating.setText(getResources().getString(R.string.order_no) + " " + getIntent().getStringExtra("order_id"));
        if (getIntent().getStringExtra("is_scheduled").equals("0")) {
            order_date_rating.setText(getResources().getString(R.string.service_date) + " " + Utils.convertTimeStampDate(Long.parseLong(getIntent().getStringExtra("created_date"))));
        } else {
            order_date_rating.setText(getResources().getString(R.string.service_date) + " " + getIntent().getStringExtra("start_date"));
        }

        date.setText(Utils.convertTimeStampDateTime(Long.parseLong(getIntent().getStringExtra("review_date"))));
    }
}
