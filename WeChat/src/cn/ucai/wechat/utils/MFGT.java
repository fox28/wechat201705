package cn.ucai.wechat.utils;

import android.app.Activity;
import android.content.Intent;

import cn.ucai.wechat.R;
import cn.ucai.wechat.ui.GuideActivity;
import cn.ucai.wechat.ui.MainActivity;

/**
 * Created by apple on 2017/5/23.
 */

public class MFGT {

    public static void finish(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public static void startActivity(Activity activity, Class cls) {
        activity.startActivity(new Intent(activity, cls));
        activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public static void startActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

    }

    public static void startActivityForResult(Activity activity, Intent intent, int requestCode) {
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }


    public static void gotoMainActivity(Activity activity) {
        startActivity(activity, MainActivity.class);
    }

    public static void gotoGuide(Activity activity) {
        startActivity(activity, GuideActivity.class);
    }
//
//    public static void gotoLoginActivity(Activity activity) {
//        startActivity(activity, LoginActivity.class);
//    }
//
//    public static void gotoRegisterActivity(Activity activity) {
//        startActivity(activity, RegisterActivity.class);
//    }
}