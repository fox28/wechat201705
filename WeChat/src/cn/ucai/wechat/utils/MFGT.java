package cn.ucai.wechat.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import cn.ucai.wechat.R;
import cn.ucai.wechat.ui.GuideActivity;
import cn.ucai.wechat.ui.LoginActivity;
import cn.ucai.wechat.ui.MainActivity;
import cn.ucai.wechat.ui.RegisterActivity;
import cn.ucai.wechat.ui.SettingsActivity;
import cn.ucai.wechat.ui.UserProfileActivity;

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

    public static void gotoLoginActivity(Activity activity) {
        startActivity(activity, new Intent(activity, LoginActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void gotoRegisterActivity(Activity activity) {
        startActivity(activity, RegisterActivity.class);
    }

    public static void gotoSettings(Activity activity) {
        startActivity(activity, SettingsActivity.class);
    }

    public static void gotoUserInfo(Activity activity, boolean settings, String username) {
//        startActivity(new Intent(getActivity(), UserProfileActivity.class).putExtra("setting", true)
//                .putExtra("username", EMClient.getInstance().getCurrentUser()));
        startActivity(activity, new Intent(activity, UserProfileActivity.class)
                .putExtra("setting", settings)
                .putExtra("username", username));
    }
}