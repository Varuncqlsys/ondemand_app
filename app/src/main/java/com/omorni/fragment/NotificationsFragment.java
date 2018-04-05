package com.omorni.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
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
import com.omorni.activity.MainActivity;
import com.omorni.adapter.NotificatioAdapter;
import com.omorni.adapter.NotificatioAdapter_Seller;
import com.omorni.model.NotificationResponse;
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


public class NotificationsFragment extends Fragment implements View.OnClickListener {
    private SavePref savePref;
    private RecyclerView recyclerView;
    private RecyclerView recycle_seller;
    private Context context;
    View view;
    private TextView error_layout;
    private NotificatioAdapter adapter;
    private NotificatioAdapter_Seller adapter_seller;
    private TextView as_seller, as_buyer;
    private SearchView search;
    private String user_type = "";
    private ArrayList<NotificationResponse> arrayListBuyer;
    private ArrayList<NotificationResponse> arrayListSeller;
    int offset = 0, limit = 15, counts = 0;
    int offset_seller = 0, limit_seller = 10, counts_seller = 0;
    SwipeRefreshLayout swipeRefreshLayout;
    SwipeRefreshLayout swipe_refresh_layout_seller;
    LinearLayoutManager linearLayoutManager;
    LinearLayoutManager linearLayoutManager_seller;

    boolean is_first_click_seller = true, is_first_click_buyer = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        savePref = new SavePref(getActivity());
        if (savePref.getUserType().equals("1")) {
            user_type = "1";
            view = inflater.inflate(R.layout.fragment_notifications_seller, container, false);
        } else {
            user_type = "2";
            view = inflater.inflate(R.layout.fragment_notifications, container, false);
        }

        initialize();
        setQueryListener();

        if (ConnectivityReceiver.isConnected()) {
            getNotificationApi(offset, true);
        }

        LoadmoreElements();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                search.setQuery("", false);
                search.clearFocus();
                if (savePref.getUserType().equals("2") || user_type.equals("2")) {
                    offset = 0;
                    getNotificationApi(offset, false);
                }
            }
        });

        if (savePref.getUserType().equals("1")) {
            swipe_refresh_layout_seller.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipe_refresh_layout_seller.setRefreshing(true);
                    search.setQuery("", false);
                    search.clearFocus();
                    if (user_type.equals("1")) {
                        offset_seller = 0;
                        getNotificationApi(offset_seller, false);
                    }
                }
            });
        }
        return view;
    }

    private void initialize() {
        context = (MainActivity) getActivity();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        search = (SearchView) view.findViewById(R.id.search);
        search.setQueryHint(getResources().getString(R.string.search));
        error_layout = (TextView) view.findViewById(R.id.error_layout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle);

        if (savePref.getUserType().equals("1")) {
            recycle_seller = (RecyclerView) view.findViewById(R.id.recycle_seller);
            swipe_refresh_layout_seller = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_seller);
        }

        arrayListBuyer = new ArrayList<NotificationResponse>();
        arrayListSeller = new ArrayList<NotificationResponse>();


        if (savePref.getUserType().equals("1")) {
            swipe_refresh_layout_seller.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);

            linearLayoutManager_seller = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recycle_seller.setLayoutManager(linearLayoutManager_seller);
            adapter_seller = new NotificatioAdapter_Seller(getActivity(), arrayListSeller, NotificationsFragment.this);
            recycle_seller.setAdapter(adapter_seller);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapter = new NotificatioAdapter(getActivity(), arrayListBuyer);
            recyclerView.setAdapter(adapter);

            as_seller = (TextView) view.findViewById(R.id.as_seller);
            as_buyer = (TextView) view.findViewById(R.id.as_buyer);
            as_seller.setOnClickListener(this);
            as_buyer.setOnClickListener(this);

        } else {
            swipeRefreshLayout.setVisibility(View.VISIBLE);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapter = new NotificatioAdapter(getActivity(), arrayListBuyer);
            recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        MainActivity.filter.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.as_seller:
                user_type = "1";
                as_seller.setBackgroundColor(getResources().getColor(R.color.tab_bg_color));
                as_buyer.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                as_seller.setTextColor(getResources().getColor(R.color.colorWhite));
                as_buyer.setTextColor(getResources().getColor(R.color.colorBlueTextLight));
                swipeRefreshLayout.setVisibility(View.GONE);

                search.setQuery("", false);
                search.clearFocus();

                if (arrayListSeller.size() > 0) {
                    error_layout.setVisibility(View.GONE);
                    swipe_refresh_layout_seller.setVisibility(View.VISIBLE);
                    MainActivity.toolbar_title.setText(getResources().getString(R.string.notification) + " " + "(" + arrayListSeller.size() + ")");
                } else {
                    error_layout.setVisibility(View.VISIBLE);
                    swipe_refresh_layout_seller.setVisibility(View.GONE);
                    MainActivity.toolbar_title.setText(getResources().getString(R.string.notification));
                }
                adapter_seller = new NotificatioAdapter_Seller(getActivity(), arrayListSeller, NotificationsFragment.this);
                recycle_seller.setAdapter(adapter_seller);
                adapter_seller.notifyDataSetChanged();
                break;

            case R.id.as_buyer:
                user_type = "2";
                as_seller.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                as_buyer.setBackgroundColor(getResources().getColor(R.color.tab_bg_color));
                as_seller.setTextColor(getResources().getColor(R.color.colorBlueTextLight));
                as_buyer.setTextColor(getResources().getColor(R.color.colorWhite));
                swipe_refresh_layout_seller.setVisibility(View.GONE);

                search.setQuery("", false);
                search.clearFocus();

                if (arrayListBuyer.size() > 0) {
                    error_layout.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    MainActivity.toolbar_title.setText(getResources().getString(R.string.notification) + "(" + arrayListBuyer.size() + ")");
                } else {
                    error_layout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setVisibility(View.GONE);
                    MainActivity.toolbar_title.setText(getResources().getString(R.string.notification));
                }

                adapter = new NotificatioAdapter(getActivity(), arrayListBuyer);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (is_first_click_buyer) {
                    getNotificationApi(offset, true);
                }
                is_first_click_buyer = false;
                break;
        }
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


    private void getNotificationApi(final int offset, final boolean show_progress) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        if (show_progress) {
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.TYPE, user_type);
        formBuilder.add(AllOmorniParameters.LIMIT, String.valueOf(limit));

        if (savePref.getUserType().equals("2") || user_type.equals("2"))
            formBuilder.add(AllOmorniParameters.OFFSET, String.valueOf(offset));
        else
            formBuilder.add(AllOmorniParameters.OFFSET, String.valueOf(offset_seller));

        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.GET_NOTIFICATIONS, formBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("notify", "rsponse " + result);
                if (show_progress) {
                    progressDialog.dismiss();
                }

                if (savePref.getUserType().equals("1"))
                    swipe_refresh_layout_seller.setRefreshing(false);

                swipeRefreshLayout.setRefreshing(false);

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {

                            if (savePref.getUserType().equals("2") || user_type.equals("2")) {
                                counts = Integer.parseInt(status.getString("total_count"));

                                if (offset == 0) {
                                    if (arrayListBuyer.size() > 0)
                                        arrayListBuyer.clear();
                                }
                            } else if (savePref.getUserType().equals("1")) {
                                counts_seller = Integer.parseInt(status.getString("total_count"));
                                if (user_type.equals("1")) {
                                    if (offset_seller == 0) {
                                        if (arrayListSeller.size() > 0)
                                            arrayListSeller.clear();
                                    }
                                }
                            }
                            JSONArray body = jsonObject.getJSONArray("body");
                            for (int i = 0; i < body.length(); i++) {
                                JSONObject body_obj = body.getJSONObject(i);
                                NotificationResponse notificationResponse = new NotificationResponse();
                                notificationResponse.setId(body_obj.getString("id"));
                                notificationResponse.setReceiver(body_obj.getString("receiver"));
                                notificationResponse.setSender(body_obj.getString("sender"));
                                notificationResponse.setNotification_type(body_obj.getString("notification_type"));
                                notificationResponse.setNottification_message(body_obj.getString("nottification_message"));
                                notificationResponse.setNottification_status(body_obj.getString("nottification_status"));
                                notificationResponse.setNottification_time(body_obj.getString("nottification_time"));
                                notificationResponse.setReceiver_type(body_obj.getString("receiver_type"));

                                if (savePref.getUserType().equals("2") || user_type.equals("2"))
                                    arrayListBuyer.add(notificationResponse);
                                else
                                    arrayListSeller.add(notificationResponse);
                            }
                            if (savePref.getUserType().equals("2") || user_type.equals("2")) {
                                if (arrayListBuyer.size() > 0) {
                                    error_layout.setVisibility(View.GONE);
//                                    recyclerView.setVisibility(View.VISIBLE);
                                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                                    MainActivity.toolbar_title.setText(getResources().getString(R.string.notification) + "(" + arrayListBuyer.size() + ")");
                                } else {
                                    error_layout.setVisibility(View.VISIBLE);
//                                    recyclerView.setVisibility(View.GONE);
                                    swipeRefreshLayout.setVisibility(View.GONE);
                                    MainActivity.toolbar_title.setText(getResources().getString(R.string.notification));
                                }
                                if (arrayListBuyer.size() > limit)
                                    recyclerView.scrollToPosition(linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1);

                                adapter = new NotificatioAdapter(getActivity(), arrayListBuyer);
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            } else {
                                if (user_type.equals("1")) {
                                    if (arrayListSeller.size() > 0) {
                                        error_layout.setVisibility(View.GONE);
//                                        recycle_seller.setVisibility(View.VISIBLE);
                                        swipe_refresh_layout_seller.setVisibility(View.VISIBLE);
                                        MainActivity.toolbar_title.setText(getResources().getString(R.string.notification) + "(" + arrayListSeller.size() + ")");
                                    }
                                    if (arrayListSeller.size() > limit)
                                        recycle_seller.scrollToPosition(linearLayoutManager_seller.findLastCompletelyVisibleItemPosition() + 1);

                                    adapter_seller = new NotificatioAdapter_Seller(getActivity(), arrayListSeller, NotificationsFragment.this);
                                    recycle_seller.setAdapter(adapter_seller);
                                    adapter_seller.notifyDataSetChanged();
                                }
                            }

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
            }
        };
        mAsync.execute();
    }

    public void filterResults(String query) {

        if (adapter != null) {
            adapter.getFilter().filter(query);
        }
        if (adapter_seller != null) {
            adapter_seller.getFilter().filter(query);
        }

        /*if (savePref.getUserType().equals("2") || user_type.equals("2")) {
            if (adapter != null) {
                adapter.getFilter().filter(query);
            }
        } else {
            if (adapter_seller != null) {
                adapter_seller.getFilter().filter(query);
            }
        }*/
    }

    private void LoadmoreElements() {
        if (savePref.getUserType().equals("2")) {
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
                        if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == arrayListBuyer.size() - 1) {
                            if (arrayListBuyer.size() == counts) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.no_more_notification), Toast.LENGTH_SHORT).show();
                            } else {
                                swipeRefreshLayout.setRefreshing(true);
                                offset = offset + 1;
                                getNotificationApi(offset, false);
                            }
                        }

                    }
                }
            });
        } else {
            recycle_seller.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy > 0) //check for scroll down
                    {
                        if (user_type.equals("1")) {
                            if (linearLayoutManager_seller.findLastCompletelyVisibleItemPosition() == arrayListSeller.size() - 1) {
                                if (user_type.equals("1")) {
                                    if (arrayListSeller.size() == counts_seller) {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.no_more_notification), Toast.LENGTH_SHORT).show();
                                    } else {
                                        swipe_refresh_layout_seller.setRefreshing(true);
                                        offset_seller = offset_seller + 1;
                                        getNotificationApi(offset_seller, false);
                                    }
                                }
                            }
                        } else {
                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == arrayListBuyer.size() - 1) {
                                if (arrayListBuyer.size() == counts) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.no_more_notification), Toast.LENGTH_SHORT).show();
                                } else {
                                    swipeRefreshLayout.setRefreshing(true);
                                    offset = offset + 1;
                                    getNotificationApi(offset, false);
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
