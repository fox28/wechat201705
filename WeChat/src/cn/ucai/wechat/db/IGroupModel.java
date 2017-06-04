package cn.ucai.wechat.db;

import android.content.Context;

import java.io.File;

/**
 * Created by apple on 2017/5/23.
 */

public interface IGroupModel {
    void newGroup(Context context, String hxid,String groupName,String des,String owner,boolean isPublic,
                  boolean isInvites,File file,OnCompleteListener<String> listener);

}
