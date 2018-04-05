package com.omorni.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.omorni.R;
import com.omorni.activity.AddJobActivity;
import com.omorni.activity.MainActivity;
import com.omorni.adapter.PostJobsAdapter;
import com.omorni.model.PostedJobsResponse;
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


public class PostJobsFragment extends Fragment {
    private SavePref savePref;
    private RecyclerView recyclerView;
    private Context context;
    View view;
    private TextView error_layout;
    private PostJobsAdapter adapter;
    FloatingActionButton add_btn;
    ArrayList<PostedJobsResponse> postJobModelArrayList;
    private SearchView search;
    private static int REQUEST_CODE_ADD_JOB = 900;
    private static int REQUEST_CODE_JOB_DETAIL = 171;
    int offset = 0, limit = 7, counts = 0;
    private SwipeRefreshLayout swipe_refresh_layout;
    LinearLayoutManager linearLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_post_jobs, container, false);
        initialize();
        setQueryListener();
        LoadmoreElements();


        if (ConnectivityReceiver.isConnected()) {
            get_postjob(offset, true);
        }

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh_layout.setRefreshing(true);
                search.setQuery("", false);
                search.clearFocus();
                offset = 0;
                get_postjob(offset, false);
            }
        });


        return view;
    }

    private void initialize() {
        context = (MainActivity) getActivity();
        savePref = new SavePref(getActivity());
        error_layout = (TextView) view.findViewById(R.id.error_layout);
        add_btn = (FloatingActionButton) view.findViewById(R.id.add_btn);
        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        postJobModelArrayList = new ArrayList<>();
        search = (SearchView) view.findViewById(R.id.search);
        search.setQueryHint(getResources().getString(R.string.search));
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), AddJobActivity.class);
                startActivityForResult(in, REQUEST_CODE_ADD_JOB);
                ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar_title.setText(getResources().getString(R.string.posted_jobs));
        MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        MainActivity.filter.setVisibility(View.GONE);
    }

    private void get_postjob(final int offset, final boolean showProgress) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);

        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (showProgress)
            progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.BUYER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.OFFSET, String.valueOf(offset));
        formBuilder.add(AllOmorniParameters.LIMIT, String.valueOf(limit));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.BUYER_POSTED_JOBS, formBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("statuss", "heree " + result);

                if (showProgress) {
                    try {
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    } catch (final IllegalArgumentException e) {
                        // Handle or log or ignore
                    } catch (final Exception e) {
                        // Handle or log or ignore
                    }
                }
                swipe_refresh_layout.setRefreshing(false);
                if (result != null && !result.equalsIgnoreCase("")) {

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            counts = Integer.parseInt(status.getString("total_count"));
                            if (offset == 0) {
                                if (postJobModelArrayList.size() > 0)
                                    postJobModelArrayList.clear();
                            }

                            JSONArray body = jsonObject.getJSONArray("body");
                            for (int i = 0; i < body.length(); i++) {
                                PostedJobsResponse postJobModel = new PostedJobsResponse();

                                JSONObject body_obj = body.getJSONObject(i);
                                postJobModel.setBuyer_id(body_obj.getString("buyer_id"));
                                postJobModel.setId(body_obj.getString("id"));
                                postJobModel.setRequest_title(body_obj.getString("request_title"));
                                postJobModel.setJob_type(body_obj.getString("job_type"));
                                postJobModel.setJob_description(body_obj.getString("job_description"));
                                postJobModel.setWork_date(body_obj.getString("work_date"));
                                postJobModel.setWork_time(body_obj.getString("work_time"));
                                postJobModel.setPost_audio(body_obj.getString("post_audio"));
                                postJobModel.setPost_video(body_obj.getString("post_video"));
                                postJobModel.setLatitude(body_obj.getString("latitude"));
                                postJobModel.setLongitude(body_obj.getString("longitude"));
                                postJobModel.setCreated_date(body_obj.getString("created_date"));
                                postJobModel.setTotal_quotes(body_obj.getString("total_quotes"));
                                postJobModel.setJob_status(body_obj.getString("job_status"));

                                postJobModelArrayList.add(postJobModel);
                            }

                            if (postJobModelArrayList.size() > 0) {
                                error_layout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                error_layout.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }

                            if (postJobModelArrayList.size() > limit)
                                recyclerView.scrollToPosition(linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1);

                            adapter = new PostJobsAdapter(getActivity(), postJobModelArrayList, PostJobsFragment.this);
                            recyclerView.setAdapter(adapter);
//                            adapter.notifyDataSetChanged();

                        } else {
                            Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
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

    public void setQueryListener() {
        search.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterResults(newText);
                return true;
            }
        });
    }

    public void filterResults(String query) {
        if (adapter != null) {
            adapter.getFilter().filter(query);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_JOB_DETAIL) {
                if (data.getBooleanExtra("is_delete_job", false)) {
                    int position = data.getIntExtra("position", 0);
                    postJobModelArrayList.remove(position);
                    adapter.notifyDataSetChanged();
                } else {
                    if (data.getBooleanExtra("is_delete_quote", false)) {
                        offset = 0;
                        swipe_refresh_layout.setRefreshing(true);
                        get_postjob(offset, false);
                    }
                }
            } else if (requestCode == REQUEST_CODE_ADD_JOB) {
                if (postJobModelArrayList.size() == 0) {
                    error_layout.setVisibility(View.GONE);
                    offset = 0;
                    counts = counts + 1;
                    get_postjob(offset, false);
                } else {
                    counts = counts + 1;
                    PostedJobsResponse postJobModel = data.getParcelableExtra("job_detail");
                    postJobModelArrayList.add(0, postJobModel);
                    adapter.notifyDataSetChanged();
                }
            }
        }
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
                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == postJobModelArrayList.size() - 1) {
                        if (postJobModelArrayList.size() == counts) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.no_more_posts), Toast.LENGTH_SHORT).show();
                        } else {
                            swipe_refresh_layout.setRefreshing(true);
                            offset = offset + 1;
                            get_postjob(offset, false);
                        }
                    }

                }
            }
        });

    }
}
