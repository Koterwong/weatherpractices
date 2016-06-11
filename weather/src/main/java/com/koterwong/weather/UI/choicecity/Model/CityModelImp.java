package com.koterwong.weather.ui.choicecity.Model;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.koterwong.weather.MyApp;
import com.koterwong.weather.R;
import com.koterwong.weather.beans.CityBean;
import com.koterwong.weather.beans.ProvinceBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author：Koterwong，Data：2016/4/25.
 * Description:
 */
public class CityModelImp implements CityModel {

    @Override public void queryProvince(CityModelImp.QueryProListener listener) {
        SQLiteDatabase cityDb = CityDbManager.getCityDataBase(CityDbManager.getDBCopyPath());
        Cursor cursor = cityDb.query("T_Province", null, null, null, null, null, null);
        if (cursor == null) {
            listener.queryFailed(new FileNotFoundException("数据库文件不存在"));
            return;
        }
        List<ProvinceBean> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ProvinceBean province = new ProvinceBean();
                province.ProName = cursor.getString(cursor.getColumnIndex("ProName"));
                province.ProShort = cursor.getString(cursor.getColumnIndex("ProSort"));
                province.proRemeark = cursor.getString(cursor.getColumnIndex("ProRemark"));
                list.add(province);
            } while (cursor.moveToNext());
            cursor.close();
            listener.querySuccess(list);
        }
    }

    @Override public void queryCity(String ProID, CityModelImp.QueryCityListener listener) {
        SQLiteDatabase cityDb = CityDbManager.getCityDataBase(CityDbManager.getDBCopyPath());
        Cursor cursor = cityDb.query("T_City", null, "ProID = ?", new String[]{ProID}, null, null, null);
        if (cursor == null) {
            listener.queryFailed(new FileNotFoundException("数据库文件不存在"));
            return;
        }
        List<CityBean> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                CityBean city = new CityBean();
                city.CityName = cursor.getString(cursor.getColumnIndex("CityName"));
                city.ProID = ProID;
                list.add(city);
            } while (cursor.moveToNext());
            listener.querySuccess(list);
        }
        cursor.close();
    }

    @Override public void copyDatabase(final CityModelImp.LoadCityDBListener listener) {
        File file = new File(CityDbManager.getDBCopyPath());
        if (file.exists()) {
            listener.copySuccess();
            return;
        }
        new Thread(new Runnable() {
            @Override public void run() {
                Resources resources = MyApp.getApp().getResources();
                InputStream is = resources.openRawResource(R.raw.china_city);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(CityDbManager.getDBCopyPath());
                    byte[] buf = new byte[1024 * 8];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    listener.copySuccess();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    listener.copyFailed(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.copyFailed(e);
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public interface LoadCityDBListener {

        void copySuccess();

        void copyFailed(Exception e);
    }

    public interface QueryCityListener {

        void querySuccess(List<CityBean> cityList);

        void queryFailed(Exception e);
    }

    public interface QueryProListener {

        void querySuccess(List<ProvinceBean> provinceList);

        void queryFailed(Exception e);
    }
}
