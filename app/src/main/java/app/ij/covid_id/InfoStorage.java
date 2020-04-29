package app.ij.covid_id;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

class InfoStorage {

    ArrayList<HashMap<String, Object>> list;
    Context mContext;

    public InfoStorage(Context context, ArrayList<HashMap<String, Object>> lists) {
        list = lists;
        mContext = context;
    }

    public ArrayList<HashMap<String, Object>> getList() {
        return list;
    }

    public Context getContext() {
        return mContext;
    }
}
