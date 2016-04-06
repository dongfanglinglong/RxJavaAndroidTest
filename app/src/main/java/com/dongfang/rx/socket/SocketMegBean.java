package com.dongfang.rx.socket;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by dongfang on 2016/3/31.
 */
public class SocketMegBean implements Parcelable {
    public static final int MSG_TYPE_HTTP = 0;
    public static final int MSG_TYPE_SOCKET = 1;


    public String id;
    public int mstType;
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
        dest.writeInt(this.mstType);
    }

    public SocketMegBean() {
    }

    protected SocketMegBean(Parcel in) {
        this.id = in.readString();
        this.msg = in.readString();
        this.data = in.readString();
        this.dataArrary = in.createStringArray();
        this.mstType = in.readInt();
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
                "id='" + id + '\'' +
                ", msgType='" + mstType + '\'' +
                ", data='" + data + '\'' +
                ", msg='" + msg + '\'' +
                ", dataArrary=" + Arrays.toString(dataArrary) +
                '}';
    }
}
