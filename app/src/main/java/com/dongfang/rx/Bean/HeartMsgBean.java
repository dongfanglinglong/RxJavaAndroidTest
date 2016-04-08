package com.dongfang.rx.Bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dongfang on 2016/4/7.
 */
public class HeartMsgBean implements Parcelable {
    public long id;
    public String msg;
    public String data;

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.msg);
        dest.writeString(this.data);
    }

    public HeartMsgBean() {}

    protected HeartMsgBean(Parcel in) {
        this.id = in.readLong();
        this.msg = in.readString();
        this.data = in.readString();
    }

    public static final Parcelable.Creator<HeartMsgBean> CREATOR = new Parcelable.Creator<HeartMsgBean>() {
        @Override
        public HeartMsgBean createFromParcel(Parcel source) {return new HeartMsgBean(source);}

        @Override
        public HeartMsgBean[] newArray(int size) {return new HeartMsgBean[size];}
    };
}
