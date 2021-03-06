package com.hyphenate.easeui.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.controller.EaseUI.EaseUserProfileProvider;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;

public class EaseUserUtils {
    
    static EaseUserProfileProvider userProvider;
    
    static {
        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }
    
    /**
     * get EaseUser according username
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username){
        if(userProvider != null)
            return userProvider.getUser(username);
        
        return null;
    }
    /**
     * get User according username
     * @param username
     * @return
     */
    public static User getAppUserInfo(String username){
        if(userProvider != null)
            return userProvider.getAppUser(username);

        return null;
    }

    /**
     * set user avatar
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
    	EaseUser user = getUserInfo(username);
        if(user != null && user.getAvatar() != null){
            showAvatar(context, user.getAvatar(), imageView);
        }else{
            Glide.with(context).load(R.drawable.default_hd_avatar).into(imageView);
        }
    }
    
    /**
     * set user's nickname
     */
    public static void setUserNick(String username,TextView textView){
        if(textView != null){
        	EaseUser user = getUserInfo(username);
        	if(user != null && user.getNick() != null){
        		textView.setText(user.getNick());
        	}else{
        		textView.setText(username);
        	}
        }
    }
    /**
     * set user avatar
     * @param username
     */
    public static void setAppUserAvatar(Context context, String username, ImageView imageView){
    	User user = getAppUserInfo(username);
        setAppUserAvatar(context, user, imageView);

    }
    /**
     * set user avatar
     * @param user
     */
    public static void setAppUserAvatar(Context context, User user, ImageView imageView){
        if(user != null && user.getAvatar() != null){
            showAvatar(context, user.getAvatar(), imageView);
        }else{
            Glide.with(context).load(R.drawable.default_hd_avatar).into(imageView);
        }
    }

    public static void showAvatar(Context context, String avatarPath, ImageView imageView) {
        if (avatarPath != null) {
            try {
                int avatarResId = Integer.parseInt(avatarPath);
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context).load(avatarPath).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_hd_avatar).into(imageView);
            }
        } else {
            Glide.with(context).load(R.drawable.default_hd_avatar).into(imageView);
        }
    }
    public static void showGroupAvatar(Context context, String avatarPath, ImageView imageView) {
        if (avatarPath != null) {
            try {
                int avatarResId = Integer.parseInt(avatarPath);
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context).load(avatarPath).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.em_group_icon).into(imageView);
            }
        } else {// 相对于if(avatarPath != null)
            Glide.with(context).load(R.drawable.em_group_icon).into(imageView);
        }
    }

    /**
     * set user's nickname
     */
    public static void setAppUserNick(String username,TextView textView){

        if(textView != null){
        	User user = getAppUserInfo(username);
        	setAppUserNick(user, textView);
        }
    }
    /**
     * set user's nickname
     */
    public static void setAppUserNick(User user,TextView textView){

        if(textView != null&& user!=null){
        	if(user.getMUserNick() != null){
        		textView.setText(user.getMUserNick());
        	}else{
        		textView.setText(user.getMUserName());
        	}
        }
    }

}
