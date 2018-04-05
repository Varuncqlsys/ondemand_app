package com.omorni.model;

/**
 * Created by V on 5/15/2017.
 */

public class ShowEducationResponse {
    private String education_name;
    private String education_type;
    private String education_passing;

    public String getEducation_passing() {
        return education_passing;
    }

    public void setEducation_passing(String education_passing) {
        this.education_passing = education_passing;
    }

    public String getEducation_name() {
        return education_name;
    }

    public void setEducation_name(String education_name) {
        this.education_name = education_name;
    }

    public String getEducation_type() {
        return education_type;
    }

    public void setEducation_type(String education_type) {
        this.education_type = education_type;
    }


}
