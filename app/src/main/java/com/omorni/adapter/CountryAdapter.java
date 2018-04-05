package com.omorni.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.omorni.R;
import com.omorni.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by Skycap on 05/04/16.
 */
public class CountryAdapter extends BaseAdapter {

    ArrayList<JsonObject> list = new ArrayList<JsonObject>();
    Context mContext;
    TextView textview;
    Dialog dialog;

    public CountryAdapter(Context mContext, ArrayList<JsonObject> list, TextView view, Dialog dialog) {
//        super(mContext,textViewResourceId, list);
        this.mContext = mContext;
        this.list = list;
        this.textview = view;
        this.dialog = dialog;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return list.get(i).get("id").getAsLong();
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (Utils.isConfigRtl(mContext)) {
                v = inflater.inflate(R.layout.spinner_item_rtl, null);
            } else {
                v = inflater.inflate(R.layout.spinner_item, null);
            }
        }

        TextView nameview = (TextView) v.findViewById(R.id.name);
        TextView codeview = (TextView) v.findViewById(R.id.code);
        TextView fullname = (TextView) v.findViewById(R.id.fullname);
        RelativeLayout parent = (RelativeLayout) v.findViewById(R.id.parent);

//        String name = list.get(i).get("name").getAsString();
        String iso = list.get(i).get("iso").getAsString();
        String code = list.get(i).get("code").getAsString();

        nameview.setText("(" + iso + ")");
        codeview.setText("+" + code);
        fullname.setText(list.get(i).get("name").getAsString());
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview.setText("(" + list.get(i).get("iso").getAsString() + ")" + " +" + list.get(i).get("code").getAsString());
                dialog.dismiss();
            }
        });

        return v;
    }
}
