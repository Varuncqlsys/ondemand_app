package com.omorni.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.utils.SavePref;

/**
 * Created by test on 6/23/2017.
 */

public class DisclaimerActivity extends BaseActivity {
    DisclaimerActivity context;
    SavePref savePref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = DisclaimerActivity.this;
        savePref = new SavePref(DisclaimerActivity.this);

        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.disclaimer_layout_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.disclaimer_layout);
        }
        setToolbar();


        final TextView htmlTextView = (TextView) findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            htmlTextView.setText(Html.fromHtml(getIntent().getStringExtra("disclaimer"), Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM));
        else
            htmlTextView.setText(Html.fromHtml(getIntent().getStringExtra("disclaimer")));


//        final WebView webView = (WebView) findViewById(R.id.webView);
//        webView.loadDataWithBaseURL(null, getIntent().getStringExtra("disclaimer"), "text/html", "utf-8", null);
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setDisplayZoomControls(false);


    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);

        toolbar_title.setText(getResources().getString(R.string.disclaimer));
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
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
}
