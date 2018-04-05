package com.omorni.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.utils.SavePref;

public class DeclinedJobActivity extends BaseActivity {
    private TextView back, message;
    private SavePref savePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeSeller);
        setContentView(R.layout.activity_declined_job);
        initialize();
        setToolbar();
    }

    private void initialize() {
        savePref = new SavePref(DeclinedJobActivity.this);
        back = (TextView) findViewById(R.id.back);
        message = (TextView) findViewById(R.id.message);
//        message.setText(getIntent().getStringExtra("message"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeclinedJobActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.down_from_top, R.anim.down_from_top);
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
        toolbar_title.setText(getResources().getString(R.string.accepting_request));
        toolbar_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(DeclinedJobActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.down_from_top, R.anim.down_from_top);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
