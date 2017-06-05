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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;

import cn.ucai.wechat.I;
import cn.ucai.wechat.R;
import cn.ucai.wechat.db.GroupModel;
import cn.ucai.wechat.db.IGroupModel;
import cn.ucai.wechat.db.OnCompleteListener;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MFGT;
import cn.ucai.wechat.utils.Result;
import cn.ucai.wechat.utils.ResultUtils;

import com.hyphenate.easeui.domain.Group;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.exceptions.HyphenateException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NewGroupActivity extends BaseActivity {
    private static final String TAG = "NewGroupActivity";

	private EditText groupNameEditText;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox publibCheckBox;
	private CheckBox memberCheckbox;
	private TextView secondTextView;
	private EaseTitleBar mTitleBar;

    LinearLayout groupIconLayout;
    ImageView ivIcon;

	private IGroupModel model;// 已实例化
    private String avatarName;
    File avatarFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_new_group);
		groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
		publibCheckBox = (CheckBox) findViewById(R.id.cb_public);
		memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
		secondTextView = (TextView) findViewById(R.id.second_desc);
		mTitleBar = (EaseTitleBar) findViewById(R.id.title_bar);

        groupIconLayout = (LinearLayout) findViewById(R.id.layout_group_icon);
        ivIcon = (ImageView) findViewById(R.id.iv_avatar);
        initBackListener();
		model = new GroupModel();

        setListener();

	}

    private void setListener() {
        publibCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    secondTextView.setText(R.string.join_need_owner_approval);
                }else{
                    secondTextView.setText(R.string.Open_group_members_invited);
                }
            }
        });

        groupIconLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadHeadPhoto();
            }
        });
    }

    private void initBackListener() {
		mTitleBar.setLeftLayoutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MFGT.finish(NewGroupActivity.this);
			}
		});
	}

	/**
	 * @param v
	 */
	public void save(View v) {
		String name = groupNameEditText.getText().toString();
		if (TextUtils.isEmpty(name)) {
		    new EaseAlertDialog(this, R.string.Group_name_cannot_be_empty).show();
		} else {
			// select from contact list
			startActivityForResult(new Intent(this, GroupPickContactsActivity.class)
                    .putExtra("groupName", name), I.REQUEST_CODE_PICK_CONTACT);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case I.REQUEST_CODE_PICK_PIC:
                if (data==null||data.getData()==null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case I.REQUEST_CODE_CUTTING:
                if (data!=null) {
                    setPicToView(data);
                }
                break;
            case I.REQUEST_CODE_PICK_CONTACT:
                if (resultCode == RESULT_OK) {
                    // new group
                    showDialog();
                    createEMGroup(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

	}

    private void createEMGroup(final Intent data) {
        final String st2 = getResources().getString(R.string.Failed_to_create_groups);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String groupName = groupNameEditText.getText().toString().trim();
                String desc = introductionEditText.getText().toString();
                String[] members = data.getStringArrayExtra("newmembers");
                try {
                    EMGroupOptions option = new EMGroupOptions();
                    option.maxUsers = 200;
                    option.inviteNeedConfirm = true;

                    String reason = NewGroupActivity.this.getString(R.string.invite_join_group);
                    reason  = EMClient.getInstance().getCurrentUser() + reason + groupName;

                    if(publibCheckBox.isChecked()){
                        option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
                    }else{
                        option.style = memberCheckbox.isChecked()?EMGroupStyle.EMGroupStylePrivateMemberCanInvite:EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                    }

                    EMGroup emGroup = EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);

                    createAppGroup(emGroup, members);

                } catch (final HyphenateException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        }).start();
    }

    private void createAppGroup(EMGroup emGroup, final String[] members) {
        if(emGroup!=null){
            if (emGroup.getGroupId() != null) {
                model.newGroup(NewGroupActivity.this, emGroup.getGroupId(), emGroup.getGroupName(),
                        emGroup.getDescription(), emGroup.getOwner(), emGroup.isPublic(), emGroup.isAllowInvites(),
                        avatarFile, new OnCompleteListener<String>() {
                            @Override
                            public void onSuccess(String jsonStr) {
                                L.e(TAG, "createAppGroup|onSuccess, jsonStr"+jsonStr);
                                boolean success = false;
                                if (jsonStr!=null) {
                                    Result result = ResultUtils.getResultFromJson(jsonStr, Group.class);
                                    if (result != null && result.isRetMsg()) {
                                        Group group = (Group) result.getRetData();
                                        L.e(TAG, "createAppGroup, group = "+group);
                                        if (group!=null) {
                                            if (members.length > 0) {
                                                addMembers(group.getMGroupHxid(), getStringFromMembers(members));
                                            } else {
                                                success =true;
                                            }
                                        }
                                    }
                                }
                                if (members.length<=0) {
                                    createSuccess(success);
                                }
                            }
                            @Override
                            public void onError(String error) {
                                createSuccess(false);
                            }
                        });
            }
        }
    }

    private void addMembers(String hxid, String members) {
        L.e(TAG, "addMembers, hxid = "+hxid+", members = "+members);
        model.addMembers(NewGroupActivity.this, hxid, members, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String jsonStr) {
                boolean success = false;
                if (jsonStr != null) {
                    Result result = ResultUtils.getResultFromJson(jsonStr, Group.class);
                    if (result.isRetMsg() && result != null) {
                        success =true;
                    }
                }
                createSuccess(success);
            }
            @Override
            public void onError(String error) {
                L.e(TAG, "addMembers|onError, error = "+error);
                createSuccess(false);
            }
        });
    }

    private String getStringFromMembers(String[] members) {
        StringBuffer buffer = new StringBuffer();
        for (String member :members) {
            buffer.append(member).append(",");
        }
        return buffer.toString();
    }

    private void createSuccess(final boolean isSuccess) {
        runOnUiThread(new Runnable() {
            public void run() {
                progressDialog.dismiss();
                if (isSuccess) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(NewGroupActivity.this, R.string.Failed_to_create_groups, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showDialog() {
        String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(st1);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void uploadHeadPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(NewGroupActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:// 上传本地图片
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, I.REQUEST_CODE_PICK_PIC);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
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
            ivIcon.setImageDrawable(drawable);// 显示图片
            saveBitmapFile(bitmap); // 保存图片
        }
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
        startActivityForResult(intent, I.REQUEST_CODE_CUTTING);
    }

    /**
     * 获取头像文件的名字
     *
     * @return
     */
    private String getAvatarName() {
        avatarName = I.AVATAR_TYPE_GROUP_PATH + System.currentTimeMillis();
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
    private void saveBitmapFile(Bitmap bitmap) {
        if (bitmap != null) {
            String imagePath = getAvatarPath(NewGroupActivity.this, I.AVATAR_TYPE_USER_PATH) +
                    "/" + getAvatarName() + ".jpg";
            File file = new File(imagePath); // 将要保存图片的途径
            L.e(TAG, "saveBitmapFile, file = " + file.getAbsolutePath());
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            avatarFile = file;
        }
    }

}
