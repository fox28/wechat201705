package cn.ucai.wechat.parse;

import android.content.Context;
import android.content.Intent;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;

import cn.ucai.wechat.I;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.WeChatHelper.DataSyncListener;
import cn.ucai.wechat.db.IUserModel;
import cn.ucai.wechat.db.OnCompleteListener;
import cn.ucai.wechat.db.UserModel;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.PreferenceManager;
import cn.ucai.wechat.utils.Result;
import cn.ucai.wechat.utils.ResultUtils;

import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserProfileManager {
	private static final String TAG = "UserProfileManager";

	/**
	 * application context
	 */
	protected Context appContext = null;

	/**
	 * init flag: test if the sdk has been inited before, we don't need to init
	 * again
	 */
	private boolean sdkInited = false;

	/**
	 * HuanXin sync contact nick and avatar listener
	 */
	private List<DataSyncListener> syncContactInfosListeners;

	private boolean isSyncingContactInfosWithServer = false;

	private EaseUser currentUser;
	private User currentAppUser;
	IUserModel userModel;

	public UserProfileManager() {
	}

	public synchronized boolean init(Context context) {
		if (sdkInited) {
			return true;
		}
		// 实例化
		appContext = context;
		ParseManager.getInstance().onInit(context);
		syncContactInfosListeners = new ArrayList<DataSyncListener>();
		userModel = new UserModel();
		sdkInited = true;
		return true;
	}

	public void addSyncContactInfoListener(DataSyncListener listener) {
		if (listener == null) {
			return;
		}
		if (!syncContactInfosListeners.contains(listener)) {
			syncContactInfosListeners.add(listener);
		}
	}

	public void removeSyncContactInfoListener(DataSyncListener listener) {
		if (listener == null) {
			return;
		}
		if (syncContactInfosListeners.contains(listener)) {
			syncContactInfosListeners.remove(listener);
		}
	}

	public void asyncFetchContactInfosFromServer(List<String> usernames, final EMValueCallBack<List<EaseUser>> callback) {
		if (isSyncingContactInfosWithServer) {
			return;
		}
		isSyncingContactInfosWithServer = true;
		ParseManager.getInstance().getContactInfos(usernames, new EMValueCallBack<List<EaseUser>>() {

			@Override
			public void onSuccess(List<EaseUser> value) {
				isSyncingContactInfosWithServer = false;
				// in case that logout already before server returns,we should
				// return immediately
				if (!WeChatHelper.getInstance().isLoggedIn()) {
					return;
				}
				if (callback != null) {
					callback.onSuccess(value);
				}
			}

			@Override
			public void onError(int error, String errorMsg) {
				isSyncingContactInfosWithServer = false;
				if (callback != null) {
					callback.onError(error, errorMsg);
				}
			}

		});

	}

	public void notifyContactInfosSyncListener(boolean success) {
		for (DataSyncListener listener : syncContactInfosListeners) {
			listener.onSyncComplete(success);
		}
	}

	public boolean isSyncingContactInfoWithServer() {
		return isSyncingContactInfosWithServer;
	}

	public synchronized void reset() {
		isSyncingContactInfosWithServer = false;
		currentUser = null;
		currentAppUser = null;
		PreferenceManager.getInstance().removeCurrentUserInfo();
	}

	public synchronized EaseUser getCurrentUserInfo() {
		if (currentUser == null) {
			String username = EMClient.getInstance().getCurrentUser();
			currentUser = new EaseUser(username);
			String nick = getCurrentUserNick();
			currentUser.setNick((nick != null) ? nick : username);
			currentUser.setAvatar(getCurrentUserAvatar());
		}
		return currentUser;
	}
	public synchronized User getCurrentAppUserInfo() {
		if (currentAppUser == null||currentAppUser.getMUserName()==null) {
			String username = EMClient.getInstance().getCurrentUser();
			currentAppUser = new User(username);
			String nick = getCurrentUserNick();
			currentAppUser.setMUserNick((nick != null) ? nick : username);
		}
		return currentAppUser;
	}

	public boolean updateCurrentUserNickName(final String nickname) {
		userModel.updateUserNick(appContext, EMClient.getInstance().getCurrentUser(), nickname,
				new OnCompleteListener<String>() {
					boolean isUpdateNick = false;
					@Override
					public void onSuccess(String jsonStr) {
						L.e(TAG, "updateCurrentUserNickName, onSuccess, jsonStr = "+jsonStr);
						if (jsonStr != null) {
							Result result = ResultUtils.getResultFromJson(jsonStr, User.class);
							if (result != null && result.isRetMsg()) {
								User user = (User) result.getRetData();
								if (user != null) {
									isUpdateNick = true;
									setCurrentAppUserNick(user.getMUserNick());// 更新内存和SharePreference
									WeChatHelper.getInstance().saveAppContact(user);// 更新数据库
								}
							}
						}
						L.e(TAG, "标识3， updateRemoteNick, isUpdateNick = "+isUpdateNick+", nickname = "+nickname);
						appContext.sendBroadcast(new Intent(I.BROADCAST_UPDATE_USER_NICK)
								.putExtra(I.User.NICK, isUpdateNick));
					}
					@Override
					public void onError(String error) {
						appContext.sendBroadcast(new Intent(I.BROADCAST_UPDATE_USER_NICK)
								.putExtra(I.User.NICK, false));
					}
				});
		return false;
	}

	public void uploadUserAvatar(File file) {
		userModel.updateAvatar(appContext, EMClient.getInstance().getCurrentUser(), file,
				new OnCompleteListener<String>() {
					@Override
					public void onSuccess(String jsonStr) {
						boolean success = false;
						L.e(TAG, "uploadUserAvatar, jsonStr = "+jsonStr);
						if (jsonStr != null) {
							Result result = ResultUtils.getResultFromJson(jsonStr, User.class);
							if (result != null && result.isRetMsg()) {
								User user = (User) result.getRetData();
								L.e(TAG, "updateUserAvatar|onSuccess, user = "+user);
								if (user != null) {
									success = true;
									setCurrentAppUserAvatar(user.getAvatar()); // 保存到内存和SharePreference；
									WeChatHelper.getInstance().saveAppContact(user); // 保存到数据库
								}
							}
						}
						appContext.sendBroadcast(new Intent(I.BROADCAST_UPDATE_AVATAR).
								putExtra(I.Avatar.UPDATE_TIME, success));
					}

					@Override
					public void onError(String error) {
						appContext.sendBroadcast(new Intent(I.BROADCAST_UPDATE_AVATAR)
								.putExtra(I.Avatar.UPDATE_TIME, false));
					}
				});
//		String avatarUrl = ParseManager.getInstance().uploadParseAvatar(data);
//		if (avatarUrl != null) {
//			setCurrentUserAvatar(avatarUrl);
//		}
//		return avatarUrl;
	}

	public void asyncGetCurrentUserInfo() {
		ParseManager.getInstance().asyncGetCurrentUserInfo(new EMValueCallBack<EaseUser>() {

			@Override
			public void onSuccess(EaseUser value) {
			    if(value != null){
					// 保存demo/currentUser的昵称、头像
    				setCurrentUserNick(value.getNick());
    				setCurrentUserAvatar(value.getAvatar());
			    }
			}

			@Override
			public void onError(int error, String errorMsg) {

			}
		});

	}
	public void asyncGetCurrentAppUserInfo(){
		userModel.loadUserInfo(appContext, EMClient.getInstance().getCurrentUser(),
				new OnCompleteListener<String>() {
			@Override
			public void onSuccess(String jsonStr) {
//				L.e(TAG, "asyncGetCurrentAppUserInfo, jsonStr = "+jsonStr);
				if (jsonStr != null) {
					Result result = ResultUtils.getResultFromJson(jsonStr, User.class);
					if (result != null &&result.isRetMsg()) {
						User user = (User) result.getRetData();
//						L.e(TAG, "asyncGetCurrentAppUserInfo, user = "+user);

						// 将user.getAvatar()保存到内存 SharePreference中
						setCurrentAppUserNick(user.getMUserNick());
						setCurrentAppUserAvatar(user.getAvatar());
                        WeChatHelper.getInstance().saveAppContact(user);

					}
				}
			}

			@Override
			public void onError(String error) {
			}
		});
	}

	public void asyncGetUserInfo(final String username,final EMValueCallBack<EaseUser> callback){
		ParseManager.getInstance().asyncGetUserInfo(username, callback);
	}
	private void setCurrentUserNick(String nickname) {
		getCurrentUserInfo().setNick(nickname);
		PreferenceManager.getInstance().setCurrentUserNick(nickname);
	}

	private void setCurrentUserAvatar(String avatar) {
		getCurrentUserInfo().setAvatar(avatar);
		PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
	}
	private void setCurrentAppUserNick(String nickname) {
		getCurrentAppUserInfo().setMUserNick(nickname); //昵称保存到currentAppUser
		PreferenceManager.getInstance().setCurrentUserNick(nickname);
	}

	private void setCurrentAppUserAvatar(String avatar) {
		getCurrentAppUserInfo().setAvatar(avatar);
		PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
	}

	private String getCurrentUserNick() {
		return PreferenceManager.getInstance().getCurrentUserNick();
	}

	private String getCurrentUserAvatar() {
		return PreferenceManager.getInstance().getCurrentUserAvatar();
	}

}
