package cn.ucai.wechat.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.wechat.I;
import cn.ucai.wechat.R;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MFGT;

public class UserProfileActivity extends BaseActivity {
    private static final String TAG = "UserProfileActivity";
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    @BindView(R.id.title_bar)
    EaseTitleBar mTitleBar;
    @BindView(R.id.iv_userInfo_avatar)
    ImageView mIvUserInfoAvatar;
    @BindView(R.id.layout_userInfo_avatar)
    RelativeLayout mLayoutUserInfoAvatar;
    @BindView(R.id.tv_userInfo_nick)
    TextView mTvUserInfoNick;
    @BindView(R.id.layout_userInfo_nick)
    RelativeLayout mLayoutUserInfoNick;
    @BindView(R.id.tv_userInfo_name)
    TextView mTvUserInfoName;
    @BindView(R.id.layout_userInfo_name)
    RelativeLayout mLayoutUserInfoName;

    private ProgressDialog dialog;

    User user = null;
    UpdateNickReceiver mReceiver;
    UpdateAvatarReceiver mUpdateAvatarReceiver;
    String avatarName;

    @Override
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        setContentView(R.layout.em_activity_user_profile);
        ButterKnife.bind(this);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mReceiver = new UpdateNickReceiver();
        IntentFilter filter = new IntentFilter(I.BROADCAST_UPDATE_USER_NICK);
        registerReceiver(mReceiver, filter);

        // 注册更新头像的接听器
        mUpdateAvatarReceiver = new UpdateAvatarReceiver();
        IntentFilter filterAvatar = new IntentFilter(I.BROADCAST_UPDATE_AVATAR);
        registerReceiver(mUpdateAvatarReceiver, filterAvatar);
    }

    private void initData() {
        user = WeChatHelper.getInstance().getUserProfileManager().getCurrentAppUserInfo();
        if (user == null) {
            finish();
        } else {
            showUserInfo();
        }
    }

    private void showUserInfo() {
        mTvUserInfoName.setText(user.getMUserName());
        EaseUserUtils.setAppUserAvatar(UserProfileActivity.this, user.getMUserName(), mIvUserInfoAvatar);
        EaseUserUtils.setAppUserNick(user.getMUserName(), mTvUserInfoNick);
    }

    private void initView() {
        mTitleBar.setLeftLayoutClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MFGT.finish(UserProfileActivity.this);
            }
        });
    }

    @OnClick({R.id.layout_userInfo_avatar, R.id.layout_userInfo_nick})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_userInfo_avatar:
                uploadHeadPhoto();
                break;
            case R.id.layout_userInfo_nick:
                final EditText editText = new EditText(this);
                editText.setText(user.getMUserNick());
                editText.setSelectAllOnFocus(true);// 获得焦点时全选文本
                new Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                        .setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nickString = editText.getText().toString();
                                if (TextUtils.isEmpty(nickString)) {
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                L.e(TAG, "标识1， onViewClicked, nickString = "+nickString);
                                updateRemoteNick(nickString);
                            }
                        }).setNegativeButton(R.string.dl_cancel, null).show();
                break;
        }
    }


    public void asyncFetchUserInfo(String username) {
        WeChatHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    WeChatHelper.getInstance().saveContact(user);
                    if (isFinishing()) {
                        return;
                    }
                    mTvUserInfoNick.setText(user.getNick());
                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.em_default_avatar).into(mIvUserInfoAvatar);
                    } else {
                        Glide.with(UserProfileActivity.this).load(R.drawable.em_default_avatar).into(mIvUserInfoAvatar);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }


    private void uploadHeadPhoto() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:// 上传本地图片
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }


    /**
     * 调用UserProfileManager()相关方法，更新服务器的用户昵称
     * @param nickName
     */
    private void updateRemoteNick(final String nickName) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
        L.e(TAG, "标识2， updateRemoteNick, nickName = "+nickName);
        // 通过UserProfileManager.java修改
        WeChatHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     * 保存上传并截取的照片
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        L.e(TAG, "setPicToView, picdata = "+picdata);
        Bundle extras = picdata.getExtras();
        L.e(TAG, "setPicToView, extras = "+extras);
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            L.e(TAG, "setPicToView, bitmap = "+bitmap);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            mIvUserInfoAvatar.setImageDrawable(drawable);
//            uploadUserAvatar(Bitmap2Bytes(bitmap));
            updateAppUserAvatar(saveBitmapFile(bitmap));
        }

    }

    /**
     * 通过UserProfileManager更新服务器头像
     * 部分参考updateUserAvatar()
     * @param file
     */
    private void updateAppUserAvatar(File file) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));

        // 通过UserProfileManager更新服务器头像
        WeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(file);
        dialog.show();
    }

    /**
     * 获取头像文件的名字
     *
     * @return
     */
    private String getAvatarName() {
        avatarName = user.getMUserName() + System.currentTimeMillis();
        L.e(TAG, "avatarName = " + avatarName);
        return avatarName;
    }
    /**
     * 返回头像保存在sd卡的位置:
     * Android/data/cn.ucai.superwechat/files/pictures/user_avatar
     *
     * @param context
     * @param path
     * @return
     */
    public static String getAvatarPath(Context context, String path) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File folder = new File(dir, path);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }
    /**
     * 将bitmap转换或file文件
     *
     * @param bitmap
     * @return
     */
    private File saveBitmapFile(Bitmap bitmap) {
        if (bitmap != null) {
            String imagePath = getAvatarPath(UserProfileActivity.this, I.AVATAR_TYPE_USER_PATH) +
                    "/" + getAvatarName() + ".jpg";
            File file = new File(imagePath); // 将要保存图片的途径
            L.e(TAG, "file = " + file.getAbsolutePath());
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }

//    private void uploadUserAvatar(final byte[] data) {
//        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                final String avatarUrl = WeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        dialog.dismiss();
//                        if (avatarUrl != null) {
//                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
//                                    Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                });
//
//            }
//        }).start();
//
//        dialog.show();
//    }


//    public byte[] Bitmap2Bytes(Bitmap bm) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        return baos.toByteArray();
//    }

    private void updateNickView(boolean success){
        dialog.dismiss();
        if (!success) {
            Toast.makeText(this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT).show();
            user = WeChatHelper.getInstance().getUserProfileManager().getCurrentAppUserInfo();
            L.e(TAG, "updateNickView, nick = "+user.getMUserNick());
            mTvUserInfoNick.setText(user.getMUserNick());
        }
    }

    class UpdateNickReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(I.User.NICK, false);
            L.e(TAG, "标识4， 收到Receiver，success = "+success);
            updateNickView(success);
        }
    }


    class UpdateAvatarReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(I.Avatar.UPDATE_TIME, false);
            updateAvatarView(success);
        }
    }
    private void updateAvatarView(boolean success) {
        dialog.dismiss();
        if (success) {
            Toast.makeText(this, getString(R.string.toast_updatephoto_success), Toast.LENGTH_SHORT).show();
            user = WeChatHelper.getInstance().getUserProfileManager().getCurrentAppUserInfo();
            L.e(TAG, "updateAvatarView, user = " + user);
            EaseUserUtils.setUserAvatar(UserProfileActivity.this, user.getMUserName(), mIvUserInfoAvatar);
        } else {
            Toast.makeText(this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mUpdateAvatarReceiver != null) {
            unregisterReceiver(mUpdateAvatarReceiver);
        }
    }

}
