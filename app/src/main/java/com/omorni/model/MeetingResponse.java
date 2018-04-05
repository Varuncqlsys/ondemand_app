package com.omorni.model;

/**
 * Created by user on 6/19/2017.
 */

public class MeetingResponse {

    String id;
    String name;
    String location;
    String created;
    String strart_time;
    String end_time;
    String start_date;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getStrart_time() {
        return strart_time;
    }

    public void setStrart_time(String strart_time) {
        this.strart_time = strart_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }
}
