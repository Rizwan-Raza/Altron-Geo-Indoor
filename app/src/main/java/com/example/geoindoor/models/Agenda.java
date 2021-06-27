package com.example.geoindoor.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class Agenda implements Parcelable {
    public static final Creator<Agenda> CREATOR = new Creator<Agenda>() {
        @Override
        public Agenda createFromParcel(Parcel in) {
            return new Agenda(in);
        }

        @Override
        public Agenda[] newArray(int size) {
            return new Agenda[size];
        }
    };
    private String _agendaId;
    private String name;
    private String mobile;
    private String type;
    private String date;
    private String time;
    private String address;
    private String personToMeet;
    private String contactNumber;
    private String notes;
    private String timestamp;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    private boolean status;

    public Agenda() {
    }

    public Agenda(String name, String date, String time) {
        this.name = name;
        this.date = date;
        this.time = time;
    }

    protected Agenda(Parcel in) {
        _agendaId = in.readString();
        name = in.readString();
        type = in.readString();
        date = in.readString();
        time = in.readString();
        address = in.readString();
        personToMeet = in.readString();
        contactNumber = in.readString();
        notes = in.readString();
        timestamp = in.readString();
    }

    public String get_agendaId() {
        return _agendaId;
    }

    public void set_agendaId(String _agendaId) {
        this._agendaId = _agendaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPersonToMeet() {
        return personToMeet;
    }

    public void setPersonToMeet(String personToMeet) {
        this.personToMeet = personToMeet;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Agenda{" +
                "_agendaId='" + _agendaId + '\'' +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", address='" + address + '\'' +
                ", personToMeet='" + personToMeet + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_agendaId);
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeString(date);
        parcel.writeString(time);
        parcel.writeString(address);
        parcel.writeString(personToMeet);
        parcel.writeString(contactNumber);
        parcel.writeString(notes);
        parcel.writeString(timestamp);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


//    public Long getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(Long timestamp) {
//        this.timestamp = timestamp;
//    }
//
//    @Override
//    public int compareTo(Agenda agenda) {
//        return this.timestamp.compareTo(agenda.timestamp);
//    }
}
