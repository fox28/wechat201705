/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.wechat.ui;

import java.util.Hashtable;
import java.util.Map;

import com.hyphenate.chat.EMClient;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.WeChatHelper.DataSyncListener;
import cn.ucai.wechat.R;
import cn.ucai.wechat.db.IUserModel;
import cn.ucai.wechat.db.InviteMessgeDao;
import cn.ucai.wechat.db.OnCompleteListener;
import cn.ucai.wechat.db.UserDao;
import cn.ucai.wechat.db.UserModel;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MFGT;
import cn.ucai.wechat.utils.Result;
import cn.ucai.wechat.utils.ResultUtils;
import cn.ucai.wechat.widget.ContactItemView;
import cn.ucai.wechat.widget.TitleMenu.ActionItem;
import cn.ucai.wechat.widget.TitleMenu.TitlePopup;

import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.NetUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * contact list
 * 
 */
public class ContactListFragment extends EaseContactListFragment {
	
    private static final String TAG = ContactListFragment.class.getSimpleName();
    private ContactSyncListener contactSyncListener;
    private BlackListSyncListener blackListSyncListener;
    private ContactInfoSyncListener contactInfoSyncListener;
    private View loadingView;
    private ContactItemView applicationItem;
    private InviteMessgeDao inviteMessgeDao;

    ProgressDialog pd;
    IUserModel model;

    @SuppressLint("InflateParams")
    @Override
    protected void initView() {
        super.initView();
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.em_contacts_header, null);
        HeaderItemClickListener clickListener = new HeaderItemClickListener();
        applicationItem = (ContactItemView) headerView.findViewById(R.id.application_item);
        applicationItem.setOnClickListener(clickListener);
        headerView.findViewById(R.id.group_item).setOnClickListener(clickListener);
//        headerView.findViewById(R.id.chat_room_item).setOnClickListener(clickListener);
//        headerView.findViewById(R.id.robot_item).setOnClickListener(clickListener);
        listView.addHeaderView(headerView);
        //add loading view  下载数据是显示dialog
        loadingView = LayoutInflater.from(getActivity()).inflate(R.layout.em_layout_loading_data, null);
        contentContainer.addView(loadingView);

        registerForContextMenu(listView);// 长按联系人人时显示的菜单
    }
    
    @Override
    public void refresh() {
        Map<String, User> m = WeChatHelper.getInstance().getAppContactList();
        if (m instanceof Hashtable<?, ?>) {
            //noinspection unchecked
            m = (Map<String, User>) ((Hashtable<String, User>)m).clone();
        }
        setContactsMap(m);
        super.refresh();
        if (model == null) {
            model = new UserModel();
        }
        if(inviteMessgeDao == null){
            inviteMessgeDao = new InviteMessgeDao(getActivity());
        }
        if(inviteMessgeDao.getUnreadMessagesCount() > 0){
            applicationItem.showUnreadMsgView();
        }else{
            applicationItem.hideUnreadMsgView();
        }
    }
    
    
    @SuppressWarnings("unchecked")
    @Override
    protected void setUpView() {
        // 隐藏TitleBar, 继承自EaseBaseFragment
        hideTitleBar();

        //设置联系人数据
        Map<String, User> m = WeChatHelper.getInstance().getAppContactList();
        if (m instanceof Hashtable<?, ?>) {
            m = (Map<String, User>) ((Hashtable<String, User>)m).clone();
        }
        setContactsMap(m);
        super.setUpView();
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) listView.getItemAtPosition(position);
                if (user != null) {
                    String username = user.getMUserName();
                    // demo中直接进入聊天页面，实际一般是进入用户详情页
                    MFGT.gotoFriendProfileActivity(getActivity(), user);
//                    startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("userId", username));
                }
            }
        });

        

        
        
        contactSyncListener = new ContactSyncListener();
        WeChatHelper.getInstance().addSyncContactListener(contactSyncListener);
        
        blackListSyncListener = new BlackListSyncListener();
        WeChatHelper.getInstance().addSyncBlackListListener(blackListSyncListener);
        
        contactInfoSyncListener = new ContactInfoSyncListener();
        WeChatHelper.getInstance().getUserProfileManager().addSyncContactInfoListener(contactInfoSyncListener);
        
        if (WeChatHelper.getInstance().isContactsSyncedWithServer()) {
            loadingView.setVisibility(View.GONE); // dialog消失
        } else if (WeChatHelper.getInstance().isSyncingContactsWithServer()) {
            loadingView.setVisibility(View.VISIBLE);  // dialog显示
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (contactSyncListener != null) {
            WeChatHelper.getInstance().removeSyncContactListener(contactSyncListener);
            contactSyncListener = null;
        }
        
        if(blackListSyncListener != null){
            WeChatHelper.getInstance().removeSyncBlackListListener(blackListSyncListener);
        }
        
        if(contactInfoSyncListener != null){
            WeChatHelper.getInstance().getUserProfileManager().removeSyncContactInfoListener(contactInfoSyncListener);
        }
    }
    
	
	protected class HeaderItemClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.application_item:
                // 进入申请与通知页面
                MFGT.gotoNewFriendsMsgActivity(getActivity());
//                startActivity(new Intent(getActivity(), NewFriendsMsgActivity.class));
                break;
            case R.id.group_item:
                // 进入群聊列表页面
                MFGT.gotoGroups(getActivity());
                break;
//            case R.id.chat_room_item:
//                //进入聊天室列表页面
//                startActivity(new Intent(getActivity(), PublicChatRoomsActivity.class));
//                break;
//            case R.id.robot_item:
//                //进入Robot列表页面
//                startActivity(new Intent(getActivity(), RobotsActivity.class));
//                break;

            default:
                break;
            }
        }
	    
	}
	

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    toBeProcessUser = (User) listView.getItemAtPosition(((AdapterContextMenuInfo) menuInfo).position);
	    toBeProcessUsername = toBeProcessUser.getMUserName();
		getActivity().getMenuInflater().inflate(R.menu.em_context_contact_list, menu);
	}

    /**
     * 删除联系人
     * @param item 选择的菜单项
     * @return
     */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        L.e(TAG, "onContextItemSelected, start...");
		if (item.getItemId() == R.id.delete_contact) {
			try {
                // delete contact
                deleteContact(toBeProcessUser);
                // remove invitation message
                InviteMessgeDao dao = new InviteMessgeDao(getActivity());
                dao.deleteMessage(toBeProcessUser.getMUserName());
            } catch (Exception e) {
                e.printStackTrace();
            }
			return true;
		}
//		else if(item.getItemId() == R.id.add_to_blacklist){
//			moveToBlacklist(toBeProcessUsername);
//			return true;
//		}
		return super.onContextItemSelected(item);
	}


	/**
	 * delete contact
	 * 
	 * @param tobeDeleteUser
	 */
	public void deleteContact(final User tobeDeleteUser) {
		String st1 = getResources().getString(R.string.deleting);
		final String st2 = getResources().getString(R.string.Delete_failed);
		ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
        removeContact(tobeDeleteUser);

    }

    private void removeContact(final User tobeDeleteUser) {
        final String st2 = getResources().getString(R.string.Delete_failed);
        model.deleteConatact(getContext(), EMClient.getInstance().getCurrentUser(), tobeDeleteUser.getMUserName(),
                new OnCompleteListener<String>() {
                    @Override
                    public void onSuccess(String jsonStr) {
                        if (jsonStr != null) {
                            Result result = ResultUtils.getResultFromJson(jsonStr, User.class);
                            if (result != null && result.isRetMsg()) {
                                removeEMContact(tobeDeleteUser);
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), st2 + "\nfor: "+error, Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void removeEMContact(final User tobeDeleteUser) {
        final String st2 = getResources().getString(R.string.Delete_failed);
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(tobeDeleteUser.getMUserName());
                    // remove user from memory and database 删除环信相关内容
                    UserDao dao = new UserDao(getActivity());
                    dao.deleteContact(tobeDeleteUser.getMUserName());
                    WeChatHelper.getInstance().getContactList().remove(tobeDeleteUser.getMUserName());

                    // 删除自己保存（区别于环信原生）的相关内容
                    // remove user from database 删除数据库内容
                    dao.deleteAppContact(tobeDeleteUser.getMUserName());
                    // remove user from memory 删除内存相关内容
                    WeChatHelper.getInstance().getAppContactList().remove(tobeDeleteUser.getMUserName());

                    // 更新显示列表
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            contactList.remove(tobeDeleteUser);
                            contactListLayout.refresh();

                        }
                    });

                } catch (final Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }

            }
        }).start();
    }

    class ContactSyncListener implements DataSyncListener{
        @Override
        public void onSyncComplete(final boolean success) {
            EMLog.d(TAG, "on contact list sync success:" + success);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    getActivity().runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            if(success){
                                loadingView.setVisibility(View.GONE);
                                refresh();
                            }else{
                                String s1 = getResources().getString(R.string.get_failed_please_check);
                                Toast.makeText(getActivity(), s1, Toast.LENGTH_LONG).show();
                                loadingView.setVisibility(View.GONE);
                            }
                        }
                        
                    });
                }
            });
        }
    }
    
    class BlackListSyncListener implements DataSyncListener{

        @Override
        public void onSyncComplete(boolean success) {
            getActivity().runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    refresh();
                }
            });
        }
        
    }

    class ContactInfoSyncListener implements DataSyncListener{

        @Override
        public void onSyncComplete(final boolean success) {
            EMLog.d(TAG, "on contactinfo list sync success:" + success);
            getActivity().runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    loadingView.setVisibility(View.GONE);
                    if(success){
                        refresh();
                    }
                }
            });
        }
        
    }
	
}
