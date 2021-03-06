/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.wechat.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ucai.wechat.R;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.db.IUserModel;
import cn.ucai.wechat.db.OnCompleteListener;
import cn.ucai.wechat.db.UserModel;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MFGT;
import cn.ucai.wechat.utils.Result;
import cn.ucai.wechat.utils.ResultUtils;

public class AddContactActivity extends BaseActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();

    @BindView(R.id.title_bar)
    EaseTitleBar mTitleBar;
    @BindView(R.id.edit_note)
    EditText mEditNote;
    @BindView(R.id.ll_user)
    RelativeLayout mLlUser;
    //	private EditText editText;
//	private RelativeLayout searchedUserLayout;
//	private TextView nameText;
//	private Button searchBtn;
    private String toAddUsername;
    private ProgressDialog progressDialog;
    IUserModel model;
    User user = null; // 添加联系人的搜索结果

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_add_contact);
        ButterKnife.bind(this);

        initView();
        model = new UserModel();

    }

    private void initView() {
        mEditNote.setHint(R.string.user_name);
        mEditNote.setSelectAllOnFocus(true);

        // 设置返回按钮单击事件
        mTitleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MFGT.finish(AddContactActivity.this);
            }
        });
    }

    private void showDialog() {
        progressDialog = new ProgressDialog(AddContactActivity.this);
        progressDialog.setMessage(getString(R.string.addcontact_search));
        progressDialog.show();
    }

    /**
     * search contact
     * @param v
     */
    public void searchContact(View v) {
        final String name = mEditNote.getText().toString().trim();
        toAddUsername = name;
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Please_enter_a_username).show();
            return;
        }
        // TODO you can search the user from your app server here.

        showDialog();
        searchUser();


        // 一种保护措施：验证单击按钮文字是否跟实际相符，防止非法入侵的一种手段
//        String saveText = searchBtn.getText().toString();
//        if (getString(R.string.button_search).equals(saveText)) {
//
//        }
    }

    private void searchUser() {
        model.loadUserInfo(AddContactActivity.this, toAddUsername, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String jsonStr) {
                L.e(TAG, "searchUser|onSuccess, jsonStr = "+jsonStr);
                boolean success = false;
                if (jsonStr != null) {
                    Result result = ResultUtils.getResultFromJson(jsonStr, User.class);
                    if (result.isRetMsg() && result != null) {
                        // 跳转用户详情用户详情
                        success = true;
                        user = (User) result.getRetData();
                    }
                }
                showResult(success);
            }

            @Override
            public void onError(String error) {
                showResult(false);
            }
        });
    }

    private void showResult(boolean success) {
        progressDialog.dismiss();// 隐藏dialog
        mLlUser.setVisibility(success?View.GONE:View.VISIBLE);
        if (success) {
            // 跳转用户详情页面
            MFGT.gotoFriendProfileActivity(AddContactActivity.this, user);
        }
    }

//    /**
//     *  add contact
//     * @param view
//     */
//    public void addContact(View view) {
//        if (EMClient.getInstance().getCurrentUser().equals(nameText.getText().toString())) {
//            new EaseAlertDialog(this, R.string.not_add_myself).show();
//            return;
//        }
//
//        if (WeChatHelper.getInstance().getContactList().containsKey(nameText.getText().toString())) {
//            //let the user know the contact already in your contact list
//            if (EMClient.getInstance().contactManager().getBlackListUsernames().contains(nameText.getText().toString())) {
//                new EaseAlertDialog(this, R.string.user_already_in_contactlist).show();
//                return;
//            }
//            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
//            return;
//        }
//
//        progressDialog = new ProgressDialog(this);
//        String stri = getResources().getString(R.string.Is_sending_a_request);
//        progressDialog.setMessage(stri);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();
//
//        new Thread(new Runnable() {
//            public void run() {
//
//                try {
//                    //demo use a hardcode reason here, you need let user to input if you like
//                    String s = getResources().getString(R.string.Add_a_friend);
//                    EMClient.getInstance().contactManager().addContact(toAddUsername, s);
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            progressDialog.dismiss();
//                            String s1 = getResources().getString(R.string.send_successful);
//                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } catch (final Exception e) {
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            progressDialog.dismiss();
//                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
//                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            }
//        }).start();
//    }

    public void back(View v) {
        finish();
    }
}
