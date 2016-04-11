package com.dongfang.rx.Bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by dongfang on 2016/4/7.
 */
public class BaseBean<T> implements Parcelable {
    public long id;
    public String msg;
    public String data;


    public T fromJson() {
        Type[] parameterizedTypes = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        Class pClazz = (Class) parameterizedTypes[0];
        return (T) new Gson().fromJson(data, pClazz);
    }


    public BaseBean() { }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.msg);
        dest.writeString(this.data);
    }

    protected BaseBean(Parcel in) {
        this.id = in.readLong();
        this.msg = in.readString();
        this.data = in.readString();
    }

    @Override
    public String toString() {
        return "BaseBean{" +
                "id=" + id +
                ", msg='" + msg + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
