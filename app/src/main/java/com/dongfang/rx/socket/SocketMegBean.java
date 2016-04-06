package com.dongfang.rx.socket;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by dongfang on 2016/3/31.
 */
public class SocketMegBean implements Parcelable {

    public String id;
    public String msg;
    public String data;
    public String[] dataArrary;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.msg);
        dest.writeString(this.data);
        dest.writeStringArray(this.dataArrary);
    }

    public SocketMegBean() {
    }

    protected SocketMegBean(Parcel in) {
        this.id = in.readString();
        this.msg = in.readString();
        this.data = in.readString();
        this.dataArrary = in.createStringArray();
    }

    public static final Parcelable.Creator<SocketMegBean> CREATOR = new Parcelable.Creator<SocketMegBean>() {
        @Override
        public SocketMegBean createFromParcel(Parcel source) {
            return new SocketMegBean(source);
        }

        @Override
        public SocketMegBean[] newArray(int size) {
            return new SocketMegBean[size];
        }
    };

    @Override
    public String toString() {
        return "SocketMegBean{" +
                "data='" + data + '\'' +
                ", id='" + id + '\'' +
                ", msg='" + msg + '\'' +
                ", dataArrary=" + Arrays.toString(dataArrary) +
                '}';
    }
}
