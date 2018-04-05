package com.omorni.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.adapter.SellerListAdapter;
import com.omorni.model.AllPackageResponse;
import com.omorni.model.SellerData;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ParameterClass;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class SellerListActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView toolbar_title;
    private RecyclerView recyclerView;
    private SearchView search;
    private SellerListAdapter adapter;
    private ArrayList<SellerData> arrayList;
    private ArrayList<SellerData> tempArrayList;
    private ArrayList<String> filter_rating_list = new ArrayList<>();
    private int sorting_selection = 0;
    private int selected_language = -1, selected_package = -1, selected_price = -1;
    private int filter_selection = 0;
    Context context;
    private boolean service_now;
    SavePref savepref;
    int interval = 0;
    Timer timer;
    String selectLang = "", selectPackage = "", priceLow = "", priceHigh = "", starRating = "";
    String[] languageArray;
    String[] packageArray;
    String[] priceArray;
    private TextView error_layout;
    private RelativeLayout main_layout;
    private String seller_category = "";
    private String selected_lat = "", selected_long = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savepref = new SavePref(SellerListActivity.this);

        languageArray = getResources().getStringArray(R.array.language_array);
        packageArray = getResources().getStringArray(R.array.package_array);
        priceArray = getResources().getStringArray(R.array.price_array);

        if (savepref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_seller_list_seller);
        } else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_seller_list);
        }
        selected_lat = getIntent().getStringExtra("selected_lat");
        selected_long = getIntent().getStringExtra("selected_long");

        arrayList = new ArrayList<SellerData>();
        tempArrayList = new ArrayList<SellerData>();
        //filterList = new ArrayList<SellerData>();
        arrayList = getIntent().getParcelableArrayListExtra("seller_data");
        tempArrayList.addAll(arrayList);
        service_now = getIntent().getBooleanExtra("service_now", false);
        seller_category = getIntent().getStringExtra(ParameterClass.SELLER_CATEGORY);

        initialize();
        setToolbar();
        setQueryListener();

        if (service_now) {
            interval = Integer.parseInt(getIntent().getStringExtra("refreshTime"));
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    setTimer();
                }
            });
            thread.start();
        }
    }

    private void setTimer() {
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getAllPlumberApi(selected_lat, selected_long);
                        }
                    });
                }
            }, 1000 * interval, 1000 * interval);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.cancel();
    }

    private void initialize() {
        context = SellerListActivity.this;
        TextView sort = (TextView) findViewById(R.id.sort);
        TextView filter = (TextView) findViewById(R.id.filter);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        search = (SearchView) findViewById(R.id.search);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        error_layout = (TextView) findViewById(R.id.error_layout);
        search.setQueryHint(getResources().getString(R.string.search));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (service_now)
            adapter = new SellerListAdapter(arrayList, context, service_now);
        else
            adapter = new SellerListAdapter(arrayList, context, service_now, getIntent().getStringExtra("date"), getIntent().getStringExtra("time"), getIntent().getStringExtra("start_time"), getIntent().getStringExtra("end_time"));

        DividerItemDecoration verticalDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        Drawable verticalDivider = ContextCompat.getDrawable(context, R.drawable.recycle_divider);
        verticalDecoration.setDrawable(verticalDivider);
        recyclerView.addItemDecoration(verticalDecoration);
        recyclerView.setAdapter(adapter);

        if (arrayList.size() > 0) {
            main_layout.setVisibility(View.VISIBLE);
            error_layout.setVisibility(View.GONE);
        } else {
            main_layout.setVisibility(View.GONE);
            error_layout.setVisibility(View.VISIBLE);

            if (seller_category.equals(getResources().getString(R.string.plumber_id))) {
                if (service_now)
                    error_layout.setText(getResources().getString(R.string.no_plumbers_avaialable));
                else
                    error_layout.setText(getResources().getString(R.string.no_caps) + " " + getResources().getString(R.string.plumber) + " " +
                            getResources().getString(R.string.is_available) + " " + getIntent().getStringExtra("date") + " " + getResources().getString(R.string.between)
                            + " " + getIntent().getStringExtra("start_time") + " " + getResources().getString(R.string.and) + " " + getIntent().getStringExtra("end_time") +
                            ". " + getResources().getString(R.string.retry_later));
            } else if (seller_category.equals(getResources().getString(R.string.electrician_id))) {
                if (service_now)
                    error_layout.setText(getResources().getString(R.string.no_electrician_avaialable));
                else
                    error_layout.setText(getResources().getString(R.string.no_caps) + " " + getResources().getString(R.string.electrician) + " " +
                            getResources().getString(R.string.is_available) + " " + getIntent().getStringExtra("date") + " " + getResources().getString(R.string.between)
                            + " " + getIntent().getStringExtra("start_time") + " " + getResources().getString(R.string.and) + " " + getIntent().getStringExtra("end_time") +
                            ". " + getResources().getString(R.string.retry_later));
            } else if (seller_category.equals(getResources().getString(R.string.painter_id))) {
                if (service_now)
                    error_layout.setText(getResources().getString(R.string.no_painter_avaialable));
                else
                    error_layout.setText(getResources().getString(R.string.no_caps) + " " + getResources().getString(R.string.painter) + " " +
                            getResources().getString(R.string.is_available) + " " + getIntent().getStringExtra("date") + " " + getResources().getString(R.string.between)
                            + " " + getIntent().getStringExtra("start_time") + " " + getResources().getString(R.string.and) + " " + getIntent().getStringExtra("end_time") +
                            ". " + getResources().getString(R.string.retry_later));
            } else if (seller_category.equals(getResources().getString(R.string.carpenter_id))) {
                if (service_now)
                    error_layout.setText(getResources().getString(R.string.no_carpeneter_avaialable));
                else
                    error_layout.setText(getResources().getString(R.string.no_caps) + " " + getResources().getString(R.string.carpenter) + " " +
                            getResources().getString(R.string.is_available) + " " + getIntent().getStringExtra("date") + " " + getResources().getString(R.string.between)
                            + " " + getIntent().getStringExtra("start_time") + " " + getResources().getString(R.string.and) + " " + getIntent().getStringExtra("end_time") +
                            ". " + getResources().getString(R.string.retry_later));
            } else if (seller_category.equals(getResources().getString(R.string.ac_id))) {
                if (service_now)
                    error_layout.setText(getResources().getString(R.string.no_ac_avaialable));
                else
                    error_layout.setText(getResources().getString(R.string.no_caps) + " " + getResources().getString(R.string.ac_technician) + " " +
                            getResources().getString(R.string.is_available) + " " + getIntent().getStringExtra("date") + " " + getResources().getString(R.string.between)
                            + " " + getIntent().getStringExtra("start_time") + " " + getResources().getString(R.string.and) + " " + getIntent().getStringExtra("end_time") +
                            ". " + getResources().getString(R.string.retry_later));
            } else if (seller_category.equals(getResources().getString(R.string.satellite_id))) {
                if (service_now)
                    error_layout.setText(getResources().getString(R.string.no_satellite_avaialable));
                else
                    error_layout.setText(getResources().getString(R.string.no_caps) + " " + getResources().getString(R.string.satellite_technician) + " " +
                            getResources().getString(R.string.is_available) + " " + getIntent().getStringExtra("date") + " " + getResources().getString(R.string.between)
                            + " " + getIntent().getStringExtra("start_time") + " " + getResources().getString(R.string.and) + " " + getIntent().getStringExtra("end_time") +
                            ". " + getResources().getString(R.string.retry_later));
            }
        }

        sort.setOnClickListener(this);
        filter.setOnClickListener(this);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);

        if (getIntent().getStringExtra("seller_category").equals(getResources().getString(R.string.plumber_id))) {
            toolbar_title.setText(R.string.plumbers_in_area);
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_1, 0, 0, 0);
        } else if (getIntent().getStringExtra("seller_category").equals(getResources().getString(R.string.electrician_id))) {
            toolbar_title.setText(R.string.electrician_in_area);
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_2, 0, 0, 0);
        } else if (getIntent().getStringExtra("seller_category").equals(getResources().getString(R.string.carpenter_id))) {
            toolbar_title.setText(R.string.carpenter_in_area);
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_3, 0, 0, 0);
        } else if (getIntent().getStringExtra("seller_category").equals(getResources().getString(R.string.ac_id))) {
            toolbar_title.setText(R.string.ac_in_area);
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_4, 0, 0, 0);
        } else if (getIntent().getStringExtra("seller_category").equals(getResources().getString(R.string.satellite_id))) {
            toolbar_title.setText(R.string.satellite_in_area);
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_5, 0, 0, 0);
        } else if (getIntent().getStringExtra("seller_category").equals(getResources().getString(R.string.painter_id))) {
            toolbar_title.setText(R.string.painter_in_area);
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.category_6, 0, 0, 0);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sort:
                openSortingdDialog();
                break;
            case R.id.filter:
                openFilterDialog();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
//                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("from_sellerlist", true);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public void filterResults(String query) {
        if (adapter != null) {
            adapter.filter(query);
        }
    }

    public void openSortingdDialog() {
        final Dialog dialog = new Dialog(SellerListActivity.this, android.R.style.Theme_Black_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (savepref.getUserType().equals("1")) {
            dialog.setContentView(R.layout.sorting_dialog_seller);
        } else {
            dialog.setContentView(R.layout.sorting_dialog);
        }

        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        final LinearLayout basic_package_layout = (LinearLayout) dialog.findViewById(R.id.basic_package_layout);
        final LinearLayout standard_package_layout = (LinearLayout) dialog.findViewById(R.id.standard_package_layout);
        final LinearLayout premium_package_layout = (LinearLayout) dialog.findViewById(R.id.premium_package_layout);
        final LinearLayout rating_layout = (LinearLayout) dialog.findViewById(R.id.rating_layout);
        TextView submit = (TextView) dialog.findViewById(R.id.submit);
        final TextView clear = (TextView) dialog.findViewById(R.id.clear);
        ImageView back = (ImageView) dialog.findViewById(R.id.back);

        if (sorting_selection == 1)
            if (savepref.getUserType().equals("1")) {
                basic_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccentSeller));
            } else {
                basic_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }

        else if (sorting_selection == 2)
            if (savepref.getUserType().equals("1")) {
                standard_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccentSeller));
            } else {
                standard_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        else if (sorting_selection == 3)
            if (savepref.getUserType().equals("1")) {
                premium_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccentSeller));
            } else {
                premium_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }

        else if (sorting_selection == 4)
            if (savepref.getUserType().equals("1")) {
                rating_layout.setBackgroundColor(getResources().getColor(R.color.colorAccentSeller));
            } else {
                rating_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        basic_package_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sorting_selection = 1;
                basic_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                standard_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                premium_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                rating_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        });

        standard_package_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sorting_selection = 2;
                standard_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                basic_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                premium_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                rating_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        });

        premium_package_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sorting_selection = 3;
                premium_package_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                standard_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                basic_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                rating_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        });

        rating_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sorting_selection = 4;
                rating_layout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                premium_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                standard_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                basic_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        });

        dialog.show();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sortedArrayList.clear();
//                sortedArrayList.addAll(arrayList);
                sortingMethod();
                filterMethod();
                dialog.dismiss();
                adapter.notifyDataSetChanged();
            }

        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearClearSorting();
                sorting_selection = 0;
                rating_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                premium_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                standard_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                basic_package_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                dialog.dismiss();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void sortingMethod() {
        if (sorting_selection == 1) {
            Collections.sort(arrayList, new Comparator<SellerData>() {
                public int compare(SellerData obj1, SellerData obj2) {
                    return Integer.parseInt(obj1.getAllPackages().get(0).getNormal_charges()) - (Integer.parseInt(obj2.getAllPackages().get(0).getNormal_charges()));
                }
            });
        } else if (sorting_selection == 2) {
            Collections.sort(arrayList, new Comparator<SellerData>() {
                public int compare(SellerData obj1, SellerData obj2) {
                    // TODO Auto-generated method stub
                    return Integer.parseInt(obj1.getAllPackages().get(1).getNormal_charges()) - (Integer.parseInt(obj2.getAllPackages().get(1).getNormal_charges()));
                }
            });
        } else if (sorting_selection == 3) {
            Collections.sort(arrayList, new Comparator<SellerData>() {
                public int compare(SellerData obj1, SellerData obj2) {
                    // TODO Auto-generated method stub
                    return Integer.parseInt(obj1.getAllPackages().get(2).getNormal_charges()) - (Integer.parseInt(obj2.getAllPackages().get(2).getNormal_charges()));
                }
            });
        } else if (sorting_selection == 4) {
            Collections.sort(arrayList, new Comparator<SellerData>() {
                public int compare(SellerData obj1, SellerData obj2) {
                    // TODO Auto-generated method stub
                    return obj2.getRating().compareTo(obj1.getRating());
                }
            });
        }
    }

    public void openFilterDialog() {
        final Dialog dialog = new Dialog(SellerListActivity.this, android.R.style.Theme_Black_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        if (Utils.isConfigRtl(SellerListActivity.this)) {
            if (savepref.getUserType().equals("1")) {
                dialog.setContentView(R.layout.filter_dialog_seller_rtl);
            } else {
                dialog.setContentView(R.layout.filter_dialog_rtl);
            }
        } else {
            if (savepref.getUserType().equals("1")) {
                dialog.setContentView(R.layout.filter_dialog_seller);
            } else {
                dialog.setContentView(R.layout.filter_dialog);
            }
        }


        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        ImageView back = (ImageView) dialog.findViewById(R.id.back);
        final TextView one_star = (TextView) dialog.findViewById(R.id.one_star);
        final TextView two_star = (TextView) dialog.findViewById(R.id.two_star);
        final TextView three_star = (TextView) dialog.findViewById(R.id.three_star);
        final TextView four_star = (TextView) dialog.findViewById(R.id.four_star);
        final TextView five_star = (TextView) dialog.findViewById(R.id.five_star);
        final TextView select_language = (TextView) dialog.findViewById(R.id.select_language);
        final TextView select_package = (TextView) dialog.findViewById(R.id.select_package);
        final TextView select_price = (TextView) dialog.findViewById(R.id.select_price);
        TextView apply = (TextView) dialog.findViewById(R.id.apply);
        TextView clear = (TextView) dialog.findViewById(R.id.clear);


        if (filter_selection == 1) {
            for (int i = 0; i < filter_rating_list.size(); i++) {
                if (filter_rating_list.get(i).equals("1"))
                    one_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                else if (filter_rating_list.get(i).equals("2"))
                    two_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                else if (filter_rating_list.get(i).equals("3"))
                    three_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                else if (filter_rating_list.get(i).equals("4"))
                    four_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                else if (filter_rating_list.get(i).equals("5"))
                    five_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }

        } else if (filter_selection == 2) {
            select_language.setText(languageArray[selected_language]);
        } else if (filter_selection == 3) {
            select_package.setText(packageArray[selected_package]);
            select_price.setText(priceArray[selected_price]);
        }

        one_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceLow = "";
                priceHigh = "";
                selectPackage = "";
                selectLang = "";
                starRating = "1";
                filter_selection = 1;
                select_language.setText("");
                select_package.setText("");
                select_price.setText("");
                if (filter_rating_list.contains("1")) {
                    filter_rating_list.remove("1");
                    one_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                } else {
                    filter_rating_list.add(filter_rating_list.size(), "1");
                    one_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        two_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceLow = "";
                priceHigh = "";
                selectPackage = "";
                selectLang = "";
                starRating = "2";
                filter_selection = 1;
                select_language.setText("");
                select_package.setText("");
                select_price.setText("");
                if (filter_rating_list.contains("2")) {
                    filter_rating_list.remove("2");
                    two_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                } else {
                    filter_rating_list.add(filter_rating_list.size(), "2");
                    two_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        three_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceLow = "";
                priceHigh = "";
                selectPackage = "";
                selectLang = "";
                starRating = "3";
                filter_selection = 1;
                select_language.setText("");
                select_package.setText("");
                select_price.setText("");
                if (filter_rating_list.contains("3")) {
                    filter_rating_list.remove("3");
                    three_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                } else {
                    filter_rating_list.add(filter_rating_list.size(), "3");
                    three_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        four_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceLow = "";
                priceHigh = "";
                selectPackage = "";
                selectLang = "";
                starRating = "4";
                filter_selection = 1;
                select_language.setText("");
                select_package.setText("");
                select_price.setText("");
                if (filter_rating_list.contains("4")) {
                    filter_rating_list.remove("4");
                    four_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                } else {
                    filter_rating_list.add(filter_rating_list.size(), "4");
                    four_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });

        five_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceLow = "";
                priceHigh = "";
                selectPackage = "";
                selectLang = "";
                starRating = "5";
                filter_selection = 1;
                select_language.setText("");
                select_package.setText("");
                select_price.setText("");

                if (filter_rating_list.contains("5")) {
                    filter_rating_list.remove("5");
                    five_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                } else {
                    filter_rating_list.add(filter_rating_list.size(), "5");
                    five_star.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });

        select_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filter_selection = 2;
                select_package.setText("");
                select_price.setText("");
                filter_rating_list.clear();
                one_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                two_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                three_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                four_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                five_star.setBackgroundColor(getResources().getColor(R.color.light_gray));
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle(getResources().getString(R.string.select_language));
                builder.setItems(languageArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        selected_language = position;
                        select_language.setText(languageArray[position]);
                        starRating = "";
                        priceLow = "";
                        priceHigh = "";
                        selectPackage = "";
                        selectLang = languageArray[position];
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        select_package.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_selection = 3;
                select_language.setText("");
                filter_rating_list.clear();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getResources().getString(R.string.select_package));
                builder.setItems(packageArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        selected_package = position;
                        select_package.setText(packageArray[position]);
                        starRating = "";
                        selectLang = "";
                        selectPackage = packageArray[position];
                        if (selected_price == -1) {
                            selected_price = 0;
                            select_price.setText(priceArray[0]);
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        select_price.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                filter_selection = 3;
                                                filter_rating_list.clear();
                                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                builder.setTitle(getResources().getString(R.string.select_price));
                                                builder.setItems(priceArray, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int position) {
                                                                selected_price = position;
                                                                if (selected_package == -1) {
                                                                    selected_package = 0;
                                                                    select_package.setText(packageArray[0]);
                                                                }
                                                                select_price.setText(priceArray[position]);
                                                                starRating = "";
                                                                selectLang = "";
                                                                if (position == 0) {
                                                                    priceLow = "0";
                                                                    priceHigh = "10";
                                                                } else if (position == 1) {
                                                                    priceLow = "11";
                                                                    priceHigh = "20";
                                                                } else if (position == 2) {
                                                                    priceLow = "21";
                                                                    priceHigh = "30";
                                                                } else if (position == 3) {
                                                                    priceLow = "31";
                                                                    priceHigh = "50000";
                                                                }
                                                            }
                                                        }
                                                );
                                                AlertDialog alert = builder.create();
                                                alert.show();
                                            }
                                        }

        );
        back.setOnClickListener(new View.OnClickListener()

                                {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                }

        );

        apply.setOnClickListener(new View.OnClickListener()

                                 {
                                     @Override
                                     public void onClick(View view) {
                                         arrayList.clear();
                                         arrayList.addAll(tempArrayList);
                                         // filter with rating
                                         filterMethod();
                                         sortingMethod();
                                         dialog.dismiss();
                                         adapter.notifyDataSetChanged();
                                     }
                                 }

        );

        clear.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         clearFilters();
                                         dialog.dismiss();
                                         sortingMethod();
                                     }
                                 }

        );

        dialog.show();
    }


    private void filterMethod() {
        if (filter_selection == 1) {
            filterRatingBase(filter_rating_list);
        }// filter with language
        else if (filter_selection == 2) {
            filterLanguageBase(languageArray[selected_language]);
        }// filter with price
        else if (filter_selection == 3) {
            filterPriceBase(selected_package, selected_price);
        }
    }

    private void filterRatingBase(ArrayList<String> rating_list) {
        ArrayList<SellerData> filterList = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            for (int j = 0; j < rating_list.size(); j++) {
                if (arrayList.get(i).getRating().contains(rating_list.get(j))) {
                    filterList.add(arrayList.get(i));
                }
            }
        }
        arrayList.clear();
        if (filterList.size() > 0) {
            arrayList.addAll(filterList);
        }
    }

    private void filterLanguageBase(String language) {
        String eng_lng="",arabic_lng="";
        ArrayList<SellerData> filterList = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            if(selected_language==0){
                eng_lng = "Arabic";
                arabic_lng = "عربى";
                if (arrayList.get(i).getLanguage().toLowerCase().contains(eng_lng.toLowerCase())||arrayList.get(i).getLanguage().toLowerCase().contains(arabic_lng.toLowerCase())) {
                    filterList.add(arrayList.get(i));
                }
            }else if(selected_language==1){
                eng_lng = "English";
                arabic_lng = "الإنجليزية";
                if (arrayList.get(i).getLanguage().toLowerCase().contains(eng_lng.toLowerCase())||arrayList.get(i).getLanguage().toLowerCase().contains(arabic_lng.toLowerCase())) {
                    filterList.add(arrayList.get(i));
                }
            }else if(selected_language==2){
                eng_lng = "Urdu";
                arabic_lng = "الأردية";
                if (arrayList.get(i).getLanguage().toLowerCase().contains(eng_lng.toLowerCase())||arrayList.get(i).getLanguage().toLowerCase().contains(arabic_lng.toLowerCase())) {
                    filterList.add(arrayList.get(i));
                }
            }
            /*if (arrayList.get(i).getLanguage().toLowerCase().contains(language.toLowerCase())) {
                filterList.add(arrayList.get(i));
            }*/
        }
        arrayList.clear();
        if (filterList.size() > 0) {
            arrayList.addAll(filterList);
        } else {

        }
    }

    private void filterPriceBase(int packege, int price) {
        ArrayList<SellerData> filterList = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            if (packege == 0) {
                if (price == 0) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(0).getNormal_charges()) >= 0 && Integer.parseInt(arrayList.get(i).getAllPackages().get(0).getNormal_charges()) <= 10) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 1) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(0).getNormal_charges()) >= 11 && Integer.parseInt(arrayList.get(i).getAllPackages().get(0).getNormal_charges()) <= 20) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 2) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(0).getNormal_charges()) >= 21 && Integer.parseInt(arrayList.get(i).getAllPackages().get(0).getNormal_charges()) <= 30) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 3) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(0).getNormal_charges()) > 30) {
                        filterList.add(arrayList.get(i));
                    }
                }
            } else if (packege == 1) {
                if (price == 0) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(1).getNormal_charges()) >= 0 && Integer.parseInt(arrayList.get(i).getAllPackages().get(1).getNormal_charges()) <= 10) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 1) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(1).getNormal_charges()) >= 11 && Integer.parseInt(arrayList.get(i).getAllPackages().get(1).getNormal_charges()) <= 20) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 2) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(1).getNormal_charges()) >= 21 && Integer.parseInt(arrayList.get(i).getAllPackages().get(1).getNormal_charges()) <= 30) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 3) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(1).getNormal_charges()) > 30) {
                        filterList.add(arrayList.get(i));
                    }
                }
            } else if (packege == 2) {
                if (price == 0) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(2).getNormal_charges()) >= 0 && Integer.parseInt(arrayList.get(i).getAllPackages().get(2).getNormal_charges()) <= 10) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 1) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(2).getNormal_charges()) >= 11 && Integer.parseInt(arrayList.get(i).getAllPackages().get(2).getNormal_charges()) <= 20) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 2) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(2).getNormal_charges()) >= 21 && Integer.parseInt(arrayList.get(i).getAllPackages().get(2).getNormal_charges()) <= 30) {
                        filterList.add(arrayList.get(i));
                    }
                } else if (price == 3) {
                    if (Integer.parseInt(arrayList.get(i).getAllPackages().get(2).getNormal_charges()) > 30) {
                        filterList.add(arrayList.get(i));
                    }
                }
            }
        }
        arrayList.clear();
        if (filterList.size() > 0) {
            arrayList.addAll(filterList);
        } else {
        }
    }

    private void clearFilters() {
        arrayList.clear();
        filter_selection = 0;
        selected_language = -1;
        selected_package = -1;
        selected_price = -1;
        filter_rating_list.clear();
//        if (sortedArrayList.size() > 0) {
//            arrayList.addAll(sortedArrayList);
//        } else {
        arrayList.addAll(tempArrayList);
//        }
//        sortingMethod();
        adapter.notifyDataSetChanged();
    }

    private void clearClearSorting() {
        arrayList.clear();
        arrayList.addAll(tempArrayList);
        filterMethod();
        adapter.notifyDataSetChanged();
    }

    public void setQueryListener() {
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String text = newText;
                adapter.filter(text);
                filterMethod();
                sortingMethod();
                return false;
            }
        });
    }

    private void getAllPlumberApi(final String lat, final String lng) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savepref.getUserId());
        formBuilder.add(AllOmorniParameters.LATTITUDE, lat);
        formBuilder.add(AllOmorniParameters.LONGITUDE, lng);
        formBuilder.add(AllOmorniParameters.SELLER_CATEGORY, seller_category);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savepref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.NEAR_ME_URL, formBody) {
            @Override
            public void getValueParse(final String result) {

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result != null && !result.equalsIgnoreCase("")) {

                                    try {
                                        ArrayList<AllPackageResponse> allPackagesArrayList;
                                        JSONObject jsonObject = new JSONObject(result);
                                        JSONObject status = jsonObject.getJSONObject("status");
                                        interval = Integer.parseInt(status.getString("search_refresh_time"));
                                        tempArrayList.clear();
                                        if (status.getString("code").equalsIgnoreCase("1")) {
                                            JSONArray object = jsonObject.getJSONArray("body");
                                            for (int i = 0; i < object.length(); i++) {

                                                JSONObject object1 = object.getJSONObject(i);
                                                SellerData sellerData = new SellerData();
                                                allPackagesArrayList = new ArrayList<>();

                                                sellerData.setSellerId(object1.getString("id"));
                                                sellerData.setFirst_name(object1.getString("first_name"));
                                                sellerData.setLast_name(object1.getString("last_name"));
                                                sellerData.setEmail(object1.getString("email"));
                                                sellerData.setMobile(object1.getString("mobile"));
                                                sellerData.setLocation(object1.getString("location"));
                                                sellerData.setJob_description(object1.getString("job_description"));
                                                sellerData.setService_title(object1.getString("service_title"));
                                                sellerData.setUser_image(object1.getString("user_image"));
                                                sellerData.setIs_verify(object1.getString("is_verify"));
                                                sellerData.setUser_type(object1.getString("user_type"));
                                                sellerData.setStatus(object1.getString("status"));
                                                sellerData.setCreated_date(object1.getString("created_date"));
                                                sellerData.setReq_lat(lat);
                                                sellerData.setReq_lng(lng);
                                                String lat = object1.getString("latitude");
                                                String longi = object1.getString("longitude");
                                                sellerData.setLatitude(lat);
                                                sellerData.setLongitude(longi);
                                                sellerData.setOn_duty(object1.getString("on_duty"));
                                                sellerData.setDistance(object1.getString("distance"));
                                                sellerData.setFavourite(object1.getString("favourite"));
                                                sellerData.setRating(object1.getString("avgrating"));
                                                sellerData.setSeller_category(object1.getString("category"));


                                                JSONArray jsonArray = object1.getJSONArray("language");
                                                String language = "";
                                                if (jsonArray.length() > 0) {
                                                    for (int j = 0; j < jsonArray.length(); j++) {
                                                        JSONObject jsonObjectLanguage = jsonArray.getJSONObject(j);
                                                        language += jsonObjectLanguage.getString("language") + " " + jsonObjectLanguage.getString("language_level") + ",";
                                                    }
                                                    sellerData.setLanguage(language.substring(0, language.length() - 1));
                                                } else {
                                                    sellerData.setLanguage(language);
                                                }

                                                sellerData.setOmorni_processing_fee(object1.getString("omorni_processing_fee"));
                                                sellerData.setVat_tax(object1.getString("vat_tax"));

                                                sellerData.setTotal_rating_user(object1.getString("total_rating_user"));

                                                JSONArray package_array = object1.getJSONArray("allpackage");
                                                for (int j = 0; j < package_array.length(); j++) {
                                                    AllPackageResponse allpackages = new AllPackageResponse();
                                                    JSONObject package_obj = package_array.getJSONObject(j);
                                                    allpackages.setId(package_obj.getString("id"));
                                                    allpackages.setNormal_charges(package_obj.getString("normal_charges"));
                                                    allpackages.setAdditional_charges(package_obj.getString("additional_charges"));
                                                    allpackages.setStatus(package_obj.getString("status"));
                                                    allpackages.setPackage_name(package_obj.getString("package_name"));
                                                    allpackages.setPackage_description(package_obj.getString("package_description"));
                                                    allpackages.setMain_hours(package_obj.getString("main_hours"));
                                                    allpackages.setExtra_hours(package_obj.getString("extra_hours"));
                                                    allpackages.setPackage_status(package_obj.getString("package_status"));
                                                    allPackagesArrayList.add(allpackages);
                                                }

                                                sellerData.setAllPackages(allPackagesArrayList);
                                                tempArrayList.add(sellerData);
                                            }

                                            arrayList.clear();
                                            arrayList.addAll(tempArrayList);

                                            if (arrayList.size() > 0) {
                                                main_layout.setVisibility(View.VISIBLE);
                                                error_layout.setVisibility(View.GONE);
                                            } else {
                                                main_layout.setVisibility(View.GONE);
                                                error_layout.setVisibility(View.VISIBLE);

                                                if (seller_category.equals(getResources().getString(R.string.plumber_id))) {
                                                    error_layout.setText(getResources().getString(R.string.no_plumbers_avaialable));
                                                } else if (seller_category.equals(getResources().getString(R.string.electrician_id))) {
                                                    error_layout.setText(getResources().getString(R.string.no_electrician_avaialable));
                                                } else if (seller_category.equals(getResources().getString(R.string.carpenter_id))) {
                                                    error_layout.setText(getResources().getString(R.string.no_carpeneter_avaialable));
                                                } else if (seller_category.equals(getResources().getString(R.string.ac_id))) {
                                                    error_layout.setText(getResources().getString(R.string.no_ac_avaialable));
                                                } else if (seller_category.equals(getResources().getString(R.string.satellite_id))) {
                                                    error_layout.setText(getResources().getString(R.string.no_satellite_avaialable));
                                                } else if (seller_category.equals(getResources().getString(R.string.painter_id))) {
                                                    error_layout.setText(getResources().getString(R.string.no_painter_avaialable));
                                                }
                                            }

                                            filterMethod();
                                            sortingMethod();
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Utils.checkAuthToken(SellerListActivity.this, status.getString("auth_token"), status.getString("message"), savepref);
                                        }
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    Utils.showSnackBar(recyclerView, getResources().getString(R.string.internet_error), context);
                                }
                            }
                        });

                    }
                };
                thread.start();
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();

    }


}
