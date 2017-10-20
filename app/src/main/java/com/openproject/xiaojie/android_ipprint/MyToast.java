package com.openproject.xiaojie.android_ipprint;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast 的渣渣工具类
 * Created by xxj on 07/28.
 */

public class MyToast {

    private static Toast mToast;
    private static Context context;

    public static void init(Context context){
        MyToast.context = context;
    }

    public static void showShort(String msg){
        if (mToast != null) {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }else {
            mToast = Toast.makeText(context,msg, Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
