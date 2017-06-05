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

    @Override
    public void addMembers(Context context, String hxid, String username, OnCompleteListener<String> listener) {
        // http://101.251.196.90:8080/SuperWeChatServerV2.0/addGroupMembers?m_member_user_name=mm01&m_member_group_hxid=18078719082497
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_ADD_GROUP_MEMBERS)
                .addParam(I.Member.GROUP_HX_ID,hxid)
                .addParam(I.Member.USER_NAME,username)
                .targetClass(String.class)
                .execute(listener);
    }
}
