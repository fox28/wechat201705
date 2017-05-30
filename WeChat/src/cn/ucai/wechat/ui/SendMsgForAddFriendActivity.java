package cn.ucai.wechat.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.wechat.I;
import cn.ucai.wechat.R;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MFGT;

/**
 * Created by apple on 2017/5/30.
 */

public class SendMsgForAddFriendActivity extends BaseActivity{
    private static final String TAG = "SendMsgForAddFriendActi";

    @BindView(R.id.title_bar)
    EaseTitleBar mTitleBar;
    @BindView(R.id.edit_msg)
    EditText mEditMsg;
    @BindView(R.id.edit_note)
    EditText mEditNote;

    ProgressDialog progressDialog;
    String toAddUserName = null;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_send_msg_for_add_friend);
        ButterKnife.bind(this);

        initView();
        initData();

    }

    private void initData() {
        toAddUserName = getIntent().getStringExtra(I.User.USER_NAME);
        if (toAddUserName == null) {
            MFGT.finish(SendMsgForAddFriendActivity.this);
        }
        L.e(TAG, "initData, toAddUserName = "+toAddUserName);
        mEditNote.setText(toAddUserName);

    }

    private void initView() {
        mTitleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MFGT.finish(SendMsgForAddFriendActivity.this);
            }
        });
        mEditMsg.setText(getString(R.string.addcontact_send_msg_prefix)
                +WeChatHelper.getInstance().getUserProfileManager().getCurrentAppUserInfo().getMUserName());
    }

    /**
     * //     *  add contact
     * //     * @param view
     * //
     */
    public void addContact() {
        if (EMClient.getInstance().getCurrentUser().equals(toAddUserName)) {
            new EaseAlertDialog(this, R.string.not_add_myself).show();
            return;
        }

        if (WeChatHelper.getInstance().getContactList().containsKey(toAddUserName)) {
            //let the user know the contact already in your contact list
            if (EMClient.getInstance().contactManager().getBlackListUsernames().contains(toAddUserName)) {
                new EaseAlertDialog(this, R.string.user_already_in_contactlist).show();
                return;
            }
            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMClient.getInstance().contactManager().addContact(toAddUserName, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @OnClick(R.id.btn_send)
    public void onViewClicked() {
        addContact();
    }
}
