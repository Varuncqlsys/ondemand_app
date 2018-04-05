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

import android.widget.TextView;

import com.omorni.R;

import com.omorni.fragment.SellerFirstFormFragment;
import com.omorni.model.ShowSkillsResponse;

import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.util.ArrayList;

/**
 * Created by V on 2/15/2017.
 */

public class DialogAddSkillsAdapter extends RecyclerView.Adapter<DialogAddSkillsAdapter.MyViewHolder> {
    private Context context;
    SavePref savepref;
    private ArrayList<ShowSkillsResponse> arrayList;
    public String[] levelArray;
    public String[] expArray;
    private SellerFirstFormFragment firstFormFragment;
    private int exp_position = -1, level_position = -1;

    public DialogAddSkillsAdapter(Context context, ArrayList<ShowSkillsResponse> arrayList, SellerFirstFormFragment firstFormFragment) {
        this.context = context;
        savepref = new SavePref(context);
        this.arrayList = arrayList;
        this.firstFormFragment = firstFormFragment;
        levelArray = context.getResources().getStringArray(R.array.seller_level_array);
        expArray = context.getResources().getStringArray(R.array.exp_array);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (savepref.getUserType().equals("1")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_skills_row_seller, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_skills_row, parent, false);
        }

        return new DialogAddSkillsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.skills.setText(arrayList.get(position).getSkill_name());
        holder.level.setText(arrayList.get(position).getSkill_level());
        holder.experience.setText(arrayList.get(position).getSkill_exp());

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditSkillDialog(arrayList.get(position).getSkill_name(), arrayList.get(position).getSkill_level(), arrayList.get(position).getSkill_exp(), position);
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSkill(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView skills, level, experience;
        private ImageView edit, remove;

        public MyViewHolder(View view) {
            super(view);
            skills = (TextView) view.findViewById(R.id.skills);
            level = (TextView) view.findViewById(R.id.level);
            edit = (ImageView) view.findViewById(R.id.edit);
            remove = (ImageView) view.findViewById(R.id.remove);
            experience = (TextView) view.findViewById(R.id.experience);
        }
    }

    private void openEditSkillDialog(final String skill_value, String level_value, final String exp_value, final int pos) {

        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        if (savepref.getUserType().equals("1"))
            dialog.setContentView(R.layout.edit_skills_row_seller);
        else
            dialog.setContentView(R.layout.edit_skills_row);
        dialog.setCanceledOnTouchOutside(false);
        final EditText skills = (EditText) dialog.findViewById(R.id.skills);
        final TextView level = (TextView) dialog.findViewById(R.id.level);
        final TextView save_skill = (TextView) dialog.findViewById(R.id.save_skill);
        final TextView experience = (TextView) dialog.findViewById(R.id.experience);
        final ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        exp_position = Integer.parseInt(arrayList.get(pos).getExp_pos());
        level_position = Integer.parseInt(arrayList.get(pos).getLevel_pos());
        skills.setText(skill_value);
        level.setText(level_value);
        experience.setText(exp_value);
        skills.setSelection(skills.getText().length());

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
        experience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openExperienceDialog(experience);
            }
        });
        save_skill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (skills.getText().toString().length() == 0) {
                    Utils.showToast(context, context.getResources().getString(R.string.skill_error));
                } else if (level.getText().toString().length() == 0) {
                    Utils.showToast(context, context.getResources().getString(R.string.skill_level_error));
                } else {
                    boolean is_proceed = false;
                    if (level_position == 0) {
                        if (exp_position != 0) {
                            is_proceed = false;
                            Utils.showToast(context, context.getString(R.string.exp_less_2));
                        } else {
                            is_proceed = true;
                        }
                    } else if (level_position == 1) {
                        if (exp_position != 1) {
                            is_proceed = false;
                            Utils.showToast(context, context.getString(R.string.exp_less_5));
                        } else {
                            is_proceed = true;
                        }
                    } else if (level_position == 2) {
                        if (exp_position != 2) {
                            is_proceed = false;
                            Utils.showToast(context, context.getString(R.string.exp_less_greater_5));
                        } else {
                            is_proceed = true;
                        }
                    }
                    if (is_proceed) {
                        boolean isDuplicateSkill = false;
                        if (skill_value.equals(skills.getText().toString())) {
                            isDuplicateSkill = false;
                        } else {
                            if (arrayList.size() > 0) {
                                for (int i = 0; i < arrayList.size(); i++) {
                                    if (arrayList.get(i).getSkill_name().equals(skills.getText().toString())) {
                                        isDuplicateSkill = true;
                                        new android.support.v7.app.AlertDialog.Builder(context)
                                                .setTitle(context.getResources().getString(R.string.duplicate_skill))
                                                .setMessage(context.getResources().getString(R.string.duplicate_skill_error) + " " + skills.getText().toString())
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
                        if (!isDuplicateSkill) {
                            arrayList.get(pos).setSkill_name(skills.getText().toString());
                            arrayList.get(pos).setSkill_level(level.getText().toString());
                            arrayList.get(pos).setSkill_exp(experience.getText().toString());
                            arrayList.get(pos).setExp_pos(String.valueOf(exp_position));
                            arrayList.get(pos).setLevel_pos(String.valueOf(level_position));
                            notifyDataSetChanged();
                            firstFormFragment.updateSkillText();
                            dialog.dismiss();
                        }
                    }
                }
            }
        });

        dialog.show();
    }

    private void openLevelListDialog(final TextView level) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.select_level));
        builder.setItems(levelArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                level_position = position;
                level.setText(levelArray[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openExperienceDialog(final TextView exp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.select_exp));
        builder.setItems(expArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                exp_position = position;
                exp.setText(expArray[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void removeSkill(int pos) {
        arrayList.remove(pos);
        notifyDataSetChanged();
        firstFormFragment.updateSkillText();
    }

}
