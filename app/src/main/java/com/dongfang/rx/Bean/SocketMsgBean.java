package com.dongfang.rx.Bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by dongfang on 2016/3/31.
 */
public class SocketMsgBean implements Parcelable {
    public static final int MSG_TYPE_HTTP = 0;
    public static final int MSG_TYPE_SOCKET = 1;


    public long msgId;
    public int mstType;
    public String[] dataArrary;

    @Override
    public String toString() {
        return "SocketMsgBean{" +
                "msgId=" + msgId +
                ",mstType=" + (MSG_TYPE_HTTP == mstType ? "HTTP" : "SOCKET") +
                ",dataArrary=" + Arrays.toString(dataArrary) +
                '}';
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.msgId);
        dest.writeInt(this.mstType);
        dest.writeStringArray(this.dataArrary);
    }

    public SocketMsgBean() {}

    protected SocketMsgBean(Parcel in) {
        this.msgId = in.readLong();
        this.mstType = in.readInt();
        this.dataArrary = in.createStringArray();
    }

    public static final Creator<SocketMsgBean> CREATOR = new Creator<SocketMsgBean>() {
        @Override
        public SocketMsgBean createFromParcel(Parcel source) {return new SocketMsgBean(source);}

        @Override
        public SocketMsgBean[] newArray(int size) {return new SocketMsgBean[size];}
    };
}
