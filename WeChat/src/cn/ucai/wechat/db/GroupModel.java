package cn.ucai.wechat.db;

import android.content.Context;

import java.io.File;

import cn.ucai.wechat.I;
import cn.ucai.wechat.utils.OkHttpUtils;

/**
 * Created by apple on 2017/6/5.
 */

public class GroupModel implements IGroupModel {

    @Override
    public void newGroup(Context context, String hxid, String groupName, String des, String owner, boolean isPublic, boolean isInvites, File file, OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_CREATE_GROUP)
                .addParam(I.Group.HX_ID,hxid)
                .addParam(I.Group.NAME,groupName)
                .addParam(I.Group.DESCRIPTION,des)
                .addParam(I.Group.OWNER,owner)
                .addParam(I.Group.IS_PUBLIC,String.valueOf(isPublic))
                .addParam(I.Group.ALLOW_INVITES,String.valueOf(isInvites))
                .addFile2(file)
                .targetClass(String.class)
                .post()
                .execute(listener);
    }
}
