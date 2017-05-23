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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.wechat.I;
import cn.ucai.wechat.R;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.db.IUserModel;
import cn.ucai.wechat.db.OnCompleteListener;
import cn.ucai.wechat.db.UserModel;
import cn.ucai.wechat.utils.CommonUtils;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MD5;
import cn.ucai.wechat.utils.MFGT;
import cn.ucai.wechat.utils.Result;
import cn.ucai.wechat.utils.ResultUtils;

/**
 * registerAppServer screen
 */
public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";
    @BindView(R.id.txt_title)
    TextView mTxtTitle;
    @BindView(R.id.et_username)
    EditText etUsername;
    @BindView(R.id.et_nickname)
    EditText etNickname;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_confirm_password)
    EditText etConfirmPassword;
    @BindView(R.id.img_back)
    ImageView mImgBack;

    String username, nickname, pwd;
    ProgressDialog pd;
    IUserModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_register);
        ButterKnife.bind(this);

        model = new UserModel();
        initView();
//		etUsername = (EditText) findViewById(R.id.username);
//		etPassword = (EditText) findViewById(R.id.password);
//		etConfirmPassword = (EditText) findViewById(R.id.confirm_password);
    }

    private void initView() {
        mTxtTitle.setVisibility(View.VISIBLE);
        mTxtTitle.setText(R.string.register);
        mImgBack.setVisibility(View.VISIBLE);
    }

    public void registerEMServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    L.e(TAG, "registerEMServer(), username = " + username + ", pwd = " + MD5.getMessageDigest(pwd));
                    // call method in SDK
                    // EMClient环信客户端的持有实例化，
                    EMClient.getInstance().createAccount(username, MD5.getMessageDigest(pwd));
                    // 工作线程修改UI，runOnUiThread(),还有其他
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            // save current user
                            WeChatHelper.getInstance().setCurrentUserName(username);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                            MFGT.gotoLoginActivity(RegisterActivity.this);
                        }
                    });
                } catch (final HyphenateException e) {
                    unregister();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NETWORK_ERROR) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();

    }


    private void showDialog() {
        pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.Is_the_registered));
        pd.show();
    }

    private boolean checkInput() {
        username = etUsername.getText().toString().trim();
        nickname = etNickname.getText().toString().trim();
        pwd = etPassword.getText().toString().trim();
        String confirm_pwd = etConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, getResources().getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
            etNickname.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return false;
        } else if (!pwd.equals(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }

    @OnClick({R.id.img_back, R.id.btn_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                MFGT.finish(RegisterActivity.this);
                break;
            case R.id.btn_register:
                registerAppServer();
                break;
        }
    }

    private void registerAppServer() {
        if (checkInput()) {
            showDialog();
            L.e(TAG, "registerAppServer(), username = " + username + ", pwd = " + MD5.getMessageDigest(pwd));
            model.register(RegisterActivity.this, username, nickname, MD5.getMessageDigest(pwd),
                    new OnCompleteListener<String>() {
                        @Override
                        public void onSuccess(String jsonStr) {
                            L.e(TAG, "registerAppServer|onSuccess, jsonStr = " + jsonStr);
                            boolean success = false; // 设置标识，控制dialog的关闭
                            if (jsonStr != null) {
                                Result result = ResultUtils.getResultFromJson(jsonStr, String.class);
                                if (result != null) {
                                    if (result.isRetMsg()) {
                                        success = true;
                                        registerEMServer();
                                    } else if (result.getRetCode() == I.MSG_REGISTER_USERNAME_EXISTS) {
                                        CommonUtils.showShortToast(R.string.User_already_exists);
                                    } else {
                                        CommonUtils.showShortToast(R.string.Registration_failed);
                                    }
                                }
                            }
                            if (!success) {// 如果注册不成功，关闭dialog
                                pd.dismiss();

                            }


                        }

                        @Override
                        public void onError(String error) { // 发送请求不成功，关闭dialog（上面是发送成功但是结果失败）
                            pd.dismiss();
                            L.e(TAG, "registerAppServer|onError = " + error);
                            CommonUtils.showShortToast(R.string.Registration_failed);
                        }
                    });
        }
    }

    /**
     * 本服务器注册成功、环信注册不成功时，需要删除已注册的账户
     */
    private void unregister() {
        model.unregister(RegisterActivity.this, username, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String result) {
                L.e(TAG, "unregister|onSuccess, result = " + result);
            }

            @Override
            public void onError(String error) {

            }
        });
    }
}
