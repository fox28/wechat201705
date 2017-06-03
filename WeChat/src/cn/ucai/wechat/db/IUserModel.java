package cn.ucai.wechat.db;

import android.content.Context;

import java.io.File;

/**
 * Created by apple on 2017/5/23.
 */

public interface IUserModel {
    void register(Context context, String username, String nickname, String password,
                  OnCompleteListener<String> listener);
    void login(Context context,String username,String password,
               OnCompleteListener<String> listener);
    void unregister(Context context,String username,OnCompleteListener<String> listener);

    void loadUserInfo(Context context, String username, OnCompleteListener<String> listener);

    void updateUserNick(Context context, String username, String nick, OnCompleteListener<String> listener);

    void updateAvatar(Context context, String username, File file, OnCompleteListener<String> listener);

    void addContact(Context context, String username, String cname, OnCompleteListener<String> listener);

    void loadContacts(Context context, String username, OnCompleteListener<String> listener);

    void deleteConatact(Context context, String username, String cname, OnCompleteListener<String> listener);

}
