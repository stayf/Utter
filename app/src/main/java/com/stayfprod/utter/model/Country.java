package com.stayfprod.utter.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Country implements Parcelable {
    public String name;
    public int code;
    public String title;
    public String alpha2;

    public Country(Parcel in) {
        this.code = in.readInt();
        this.name = in.readString();
    }

    public Country(int code, String name, String alpha2, String title) {
        this.name = name;
        this.code = code;
        this.title = title;
        this.alpha2 = alpha2;
    }

    public Country(int code, String name, String alpha2) {
        this.name = name;
        this.code = code;
        this.alpha2 = alpha2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<Country> CREATOR = new Creator<Country>() {
        public Country createFromParcel(Parcel source) {
            return new Country(source);
        }

        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
}
