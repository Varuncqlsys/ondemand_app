package com.omorni.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.MainActivity;
import com.omorni.adapter.InboxAdapter;
import com.omorni.model.GetChatListResponse;
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


public class InboxFragment extends Fragment {
    private SavePref savePref;
    private RecyclerView recyclerView;
    private Context context;
    View view;
    private TextView error_layout;
    private InboxAdapter adapter;
    private ArrayList<GetChatListResponse> arrayList;
    private SearchView search;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_inbox, container, false);
        initialize();
        setQueryListener();
        return view;
    }

    private void initialize() {
        context = (MainActivity) getActivity();
        savePref = new SavePref(getActivity());
        search = (SearchView) view.findViewById(R.id.search);
        search.setQueryHint(getResources().getString(R.string.search));
        error_layout = (TextView) view.findViewById(R.id.error_layout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        arrayList = new ArrayList<GetChatListResponse>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new InboxAdapter(getActivity(), arrayList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar_title.setText(getResources().getString(R.string.inbox));
        MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        MainActivity.filter.setVisibility(View.GONE);
        MainActivity.filter.setText(R.string.edit);
        MainActivity.filter.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filter, 0);

        if (ConnectivityReceiver.isConnected()) {
            getChatListApi();
        }

    }

    private void getChatListApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.GET_CHAT_LIST, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            if (arrayList.size() > 0)
                                arrayList.clear();
                            JSONArray array = jsonObject.getJSONArray("body");
                            for (int i = 0; i < array.length(); i++) {
                                GetChatListResponse getChatListResponse = new GetChatListResponse();
                                JSONObject jsonObject1 = array.getJSONObject(i);
                                getChatListResponse.setMessage(jsonObject1.getString("message"));
                                getChatListResponse.setMsg_id(jsonObject1.getString("msg_id"));
                                getChatListResponse.setThread_id(jsonObject1.getString("thread_id"));
                                getChatListResponse.setCreated(jsonObject1.getString("created"));
                                getChatListResponse.setUser(jsonObject1.getString("user"));
                                getChatListResponse.setName(jsonObject1.getString("name"));
                                getChatListResponse.setUser_image(jsonObject1.getString("user_image"));
                                getChatListResponse.setRequest_id(jsonObject1.getString("request_id"));
                                getChatListResponse.setJob_status(jsonObject1.getString("job_status"));
                                getChatListResponse.setMessage_type(jsonObject1.getString("message_type"));
                                arrayList.add(getChatListResponse);
                            }
                            if (arrayList.size() > 0) {
                                error_layout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                error_layout.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
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
                progressDialog.dismiss();
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
}

