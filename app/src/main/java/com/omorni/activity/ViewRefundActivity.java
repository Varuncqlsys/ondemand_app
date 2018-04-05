package com.omorni.activity;

import android.app.ProgressDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.omorni.R;
import com.omorni.adapter.ViewRefundAdapter;
import com.omorni.model.ViewRefundCancelResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class ViewRefundActivity extends BaseActivity {
    RecyclerView recyclerView;
    SavePref savePref;
    ViewRefundAdapter adapter;
    ArrayList<ViewRefundCancelResponse> arrayList;
    private TextView error_layout;
    private SwipeRefreshLayout swipeRefreshLayout;
    LinearLayoutManager linearLayoutManager;
    int offset = 0, limit = 10, counts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        savePref = new SavePref(ViewRefundActivity.this);

        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_view_refund_seller);
        } else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_view_refund);
        }

        initialize();
        setToolbar();
        LoadmoreElements();

        if (ConnectivityReceiver.isConnected()) {
            viewRefundApi(offset, true);
        } else {
            Utils.showToast(ViewRefundActivity.this, getResources().getString(R.string.internet_error));
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                offset = 0;
                viewRefundApi(offset, false);
            }
        });
    }

    private void initialize() {
        error_layout = (TextView) findViewById(R.id.error_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        arrayList = new ArrayList<ViewRefundCancelResponse>();
        adapter = new ViewRefundAdapter(ViewRefundActivity.this, arrayList, getIntent().getStringExtra("type"));
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
         linearLayoutManager = new LinearLayoutManager(ViewRefundActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        if (getIntent().getStringExtra("type").equals("2"))
            toolbar_title.setText(R.string.view_refund);
        else
            toolbar_title.setText(R.string.view_cancel);
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
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void viewRefundApi(final int offset, final boolean show_progress) {
        final ProgressDialog progressDialog = new ProgressDialog(ViewRefundActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        if (show_progress)
            progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.TYPE, getIntent().getStringExtra("type"));
        formBuilder.add(AllOmorniParameters.OFFSET, String.valueOf(offset));
        formBuilder.add(AllOmorniParameters.LIMIT, String.valueOf(limit));

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(ViewRefundActivity.this, AllOmorniApis.VIEW_REFUND_CANCEL_LIST, formBody) {
            @Override
            public void getValueParse(String result) {
                if (show_progress) {
                    try {
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    } catch (final IllegalArgumentException e) {
                        // Handle or log or ignore
                    } catch (final Exception e) {
                        // Handle or log or ignore
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
                Log.e("resultt", "result " + result);
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {

                            counts = Integer.parseInt(status.getString("total_count"));
                            if (offset == 0) {
                                if (arrayList.size() > 0)
                                    arrayList.clear();
                            }

                            JSONArray jsonArray = jsonObject.getJSONArray("body");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                ViewRefundCancelResponse viewRefundCancelResponse = new ViewRefundCancelResponse();
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                viewRefundCancelResponse.setSeller_name(jsonObject1.getString("seller_name"));
                                viewRefundCancelResponse.setSeller_image(jsonObject1.getString("seller_image"));
                                viewRefundCancelResponse.setAmount(jsonObject1.getString("amount"));
                                viewRefundCancelResponse.setIs_scheduled(jsonObject1.getString("is_scheduled"));

                                viewRefundCancelResponse.setCreatedDate(jsonObject1.getString("time"));

                                viewRefundCancelResponse.setRating(jsonObject1.getString("rating"));
                                viewRefundCancelResponse.setStatus(jsonObject1.getString("status"));
                                viewRefundCancelResponse.setSeller_id(jsonObject1.getString("seller_id"));
                                arrayList.add(viewRefundCancelResponse);
                            }
                            if (arrayList.size() > 0) {
                                error_layout.setVisibility(View.GONE);
                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                            } else {
                                error_layout.setVisibility(View.VISIBLE);
                                swipeRefreshLayout.setVisibility(View.GONE);
                            }

                            if (arrayList.size() > limit)
                                recyclerView.scrollToPosition(linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1);

                            adapter.notifyDataSetChanged();

                        } else {
                            Utils.checkAuthToken(ViewRefundActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
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

    private void LoadmoreElements() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) //check for scroll down
                {
                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == arrayList.size() - 1) {
                        if (arrayList.size() == counts) {
                            Toast.makeText(ViewRefundActivity.this, getResources().getString(R.string.no_more_orders), Toast.LENGTH_SHORT).show();
                        } else {
                            swipeRefreshLayout.setRefreshing(true);
                            offset = offset + 1;
                            viewRefundApi(offset, false);
                        }
                    }

                }
            }
        });

    }
}
