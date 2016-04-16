package com.dongfang.rx.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dongfang on 2016/4/7.
 */
public class HeartMsgBean extends BaseBean implements Parcelable {

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {super.writeToParcel(dest, flags);}

    public HeartMsgBean() {}

    protected HeartMsgBean(Parcel in) {super(in);}

    public static final Creator<HeartMsgBean> CREATOR = new Creator<HeartMsgBean>() {
        @Override
        public HeartMsgBean createFromParcel(Parcel source) {return new HeartMsgBean(source);}

        @Override
        public HeartMsgBean[] newArray(int size) {return new HeartMsgBean[size];}
    };
}
