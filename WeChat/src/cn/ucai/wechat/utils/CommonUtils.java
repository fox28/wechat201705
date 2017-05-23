package cn.ucai.wechat.utils;

import android.widget.Toast;

import cn.ucai.wechat.R;
import cn.ucai.wechat.WeChatApplication;


/**
 * Created by clawpo on 16/9/20.
 */
public class CommonUtils {
    public static void showLongToast(String msg){
        Toast.makeText(WeChatApplication.getInstance(),msg,Toast.LENGTH_LONG).show();
    }
    public static void showShortToast(String msg){
        Toast.makeText(WeChatApplication.getInstance(),msg,Toast.LENGTH_SHORT).show();
    }
    public static void showLongToast(int rId){
        showLongToast(WeChatApplication.getInstance().getString(rId));
    }
    public static void showShortToast(int rId){
        showShortToast(WeChatApplication.getInstance().getString(rId));
    }
//    public static void showLongResultMsg(int msg){
//        showLongToast(getMsgString(msg));
//    }
//    public static void showShortResultMsg(int msg){
//        showShortToast(getMsgString(msg));
//    }
//    private static int getMsgString(int msg){
//        int resId = R.string.msg_1;
//        if(msg>0){
//            resId = WeChatApplication.getInstance().getResources()
//                    .getIdentifier(I.MSG_PREFIX_MSG+msg,"string",
//                            WeChatApplication.getInstance().getPackageName());
//        }
//        return resId;
//    }

    public static String getWeChatNoString(){
        return WeChatApplication.getInstance().getString(R.string.userinfo_txt_wechat_no);
    }

    public static String getAddContactPrefixString(){
        return WeChatApplication.getInstance().getString(R.string.addcontact_send_msg_prefix);
    }
}
