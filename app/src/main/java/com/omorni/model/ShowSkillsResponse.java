package com.omorni.model;

/**
 * Created by V on 5/15/2017.
 */

public class ShowSkillsResponse {
    private String skill_name;
    private String skill_level;
    private String skill_exp;
    private String level_pos;

    public String getExp_pos() {
        return exp_pos;
    }

    public void setExp_pos(String exp_pos) {
        this.exp_pos = exp_pos;
    }

    private String exp_pos;

    public String getLevel_pos() {
        return level_pos;
    }

    public void setLevel_pos(String level_pos) {
        this.level_pos = level_pos;
    }



    public String getSkill_exp() {
        return skill_exp;
    }

    public void setSkill_exp(String skill_exp) {
        this.skill_exp = skill_exp;
    }

    public String getSkill_name() {
        return skill_name;
    }

    public void setSkill_name(String skill_name) {
        this.skill_name = skill_name;
    }

    public String getSkill_level() {
        return skill_level;
    }

    public void setSkill_level(String skill_level) {
        this.skill_level = skill_level;
    }
}
