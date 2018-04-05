package com.omorni.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.fragment.SellerFirstFormFragment;
import com.omorni.model.ShowLanguageResponse;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.util.ArrayList;

/**
 * Created by V on 2/15/2017.
 */

public class DialogAddLanguageAdapter extends RecyclerView.Adapter<DialogAddLanguageAdapter.MyViewHolder> {
    private Context context;
    SavePref savepref;
    private ArrayList<ShowLanguageResponse> arrayList;
    private String[] language_levelArray;
    private String[] language_array;
    private SellerFirstFormFragment firstFormFragment;

    public DialogAddLanguageAdapter(Context context, ArrayList<ShowLanguageResponse> arrayList, SellerFirstFormFragment firstFormFragment) {
        this.context = context;
        savepref = new SavePref(context);
        this.arrayList = arrayList;
        this.firstFormFragment = firstFormFragment;
        language_levelArray = context.getResources().getStringArray(R.array.language_level_array);
        language_array = context.getResources().getStringArray(R.array.language_array);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (savepref.getUserType().equals("1")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_language_row_seller, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_language_row, parent, false);
        }

        return new DialogAddLanguageAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.language.setText(arrayList.get(position).getLanguage_name());
        holder.level.setText(arrayList.get(position).getLanguage_level());

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditLanguageDialog(arrayList.get(position).getLanguage_name(), arrayList.get(position).getLanguage_level(), position);
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLanguage(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView language, level;
        private ImageView edit, remove;

        public MyViewHolder(View view) {
            super(view);
            language = (TextView) view.findViewById(R.id.language);
            level = (TextView) view.findViewById(R.id.level);
            edit = (ImageView) view.findViewById(R.id.edit);
            remove = (ImageView) view.findViewById(R.id.remove);
        }
    }

    private void openEditLanguageDialog(final String language_value, String level_value, final int pos) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.edit_language_row);
        dialog.setCanceledOnTouchOutside(false);
        final RelativeLayout toolbar = (RelativeLayout) dialog.findViewById(R.id.toolbar);
        final TextView language = (TextView) dialog.findViewById(R.id.language);
        final TextView level = (TextView) dialog.findViewById(R.id.level);
        final TextView save_language = (TextView) dialog.findViewById(R.id.save_language);
        ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        language.setText(language_value);
        level.setText(level_value);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLevelListDialog(level);
            }
        });
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLanguageList(language);
            }
        });


        if (savepref.getUserType().equals("1")) {
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSeller));
            save_language.setBackground(ContextCompat.getDrawable(context, R.drawable.sort_bgd_seller_drawable));
        } else {
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            save_language.setBackground(ContextCompat.getDrawable(context, R.drawable.sort_bg_drawable));
        }
        
        save_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (language.getText().toString().length() == 0) {
                    Utils.showToast(context, context.getResources().getString(R.string.language_error));
                } else if (level.getText().toString().length() == 0) {
                    Utils.showToast(context, context.getResources().getString(R.string.language_level_error));
                } else {
                    boolean isDuplicateLanguage = false;
                    if (language_value.equals(language.getText().toString())) {
                        isDuplicateLanguage = false;
                    } else {
                        if (arrayList.size() > 0) {
                            for (int i = 0; i < arrayList.size(); i++) {
                                if (arrayList.get(i).getLanguage_name().equals(language.getText().toString())) {
                                    isDuplicateLanguage = true;
                                    new android.support.v7.app.AlertDialog.Builder(context)
                                            .setTitle(context.getResources().getString(R.string.duplicate_language))
                                            .setMessage(context.getResources().getString(R.string.duplicate_language_error) + " " + language.getText().toString())
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // continue with delete
                                                }
                                            })
                                            .show();
                                    break;
                                }
                            }
                        }
                    }
                    if (!isDuplicateLanguage) {
                        arrayList.get(pos).setLanguage_name(language.getText().toString());
                        arrayList.get(pos).setLanguage_level(level.getText().toString());
                        notifyDataSetChanged();
                        firstFormFragment.updateLanguageText();
                        dialog.dismiss();
                    }
                }
            }
        });

        dialog.show();
    }

    private void openLevelListDialog(final TextView level) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.select_level));
        builder.setItems(language_levelArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                level.setText(language_levelArray[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openLanguageList(final TextView language) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.select_language));
        builder.setItems(language_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                language.setText(language_array[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void removeLanguage(int pos) {
        arrayList.remove(pos);
        notifyDataSetChanged();
        firstFormFragment.updateLanguageText();

    }

}
