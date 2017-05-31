package cn.ucai.wechat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.wechat.I;
import cn.ucai.wechat.R;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.domain.InviteMessage;
import cn.ucai.wechat.utils.MFGT;

/**
 * Created by apple on 2017/5/30.
 */

public class FriendProfileActivity extends BaseActivity {

    @BindView(R.id.title_bar)
    EaseTitleBar mTitleBar;
    @BindView(R.id.iv_friend_avatar)
    ImageView mIvFriendAvatar;
    @BindView(R.id.tv_friend_nick)
    TextView mTvFriendNick;
    @BindView(R.id.tv_friend_username)
    TextView mTvFriendUsername;
    @BindView(R.id.btn_add_contact)
    Button mBtnAddContact;
    @BindView(R.id.btn_send_msg)
    Button mBtnSendMsg;
    @BindView(R.id.btn_send_video)
    Button mBtnSendVideo;

    User user = null; // 添加联系人的搜索结果

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_friend_profile);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initData() {
        user = (User) getIntent().getSerializableExtra(I.User.USER_NAME);
        if (user != null) {
            showFriendUserInfo();
        } else {
            InviteMessage msg = (InviteMessage) getIntent().getSerializableExtra(I.User.NICK);
            if (msg != null) {
                user = new User(msg.getFrom());
                user.setAvatar(msg.getAvatar());
                user.setMUserNick(msg.getNick());

                showFriendUserInfo();
            } else {
                    MFGT.finish(FriendProfileActivity.this);
            }
        }
    }

    /**
     * 显示搜索到用户的详细信息(用户名、头像、昵称)
     */
    private void showFriendUserInfo() {
        boolean isFriend = WeChatHelper.getInstance().getAppContactList().containsKey(user.getMUserName());
        if (isFriend) {// 当查到的用户是好友时，更新好友的数据库、内存

            // getAppContactList().put(user.getMUserName(), user);这是存到内存？？？
            WeChatHelper.getInstance().saveAppContact(user);
        }
        mTvFriendUsername.setText(user.getMUserName());
        EaseUserUtils.setAppUserNick(user, mTvFriendNick);
        EaseUserUtils.setAppUserAvatar(FriendProfileActivity.this, user,mIvFriendAvatar);

        showButton(isFriend);

        // 从服务器异步加载用户的最新信息，填充到好友列表或者新的朋友列表
        syncFriendUserInfo();
    }

    /**
     * 布局中3个按钮的显示逻辑，取决于登录用户和搜索用户的好友关系
     * @param isFriend
     */
    private void showButton(boolean isFriend) {
        mBtnAddContact.setVisibility(isFriend?  View.GONE:      View.VISIBLE);
        mBtnSendMsg.setVisibility(isFriend?     View.VISIBLE:   View.GONE);
        mBtnSendVideo.setVisibility(isFriend?   View.VISIBLE:   View.GONE);
    }

    private void initView() {
        mTitleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MFGT.finish(FriendProfileActivity.this);
            }
        });
    }

    @OnClick(R.id.btn_add_contact)
    public void onAddContact() {
        boolean isConfirm = true;
        if (isConfirm) {
            // 跳转发送验证申请的界面
            MFGT.gotoSendMsgForAddFriend(FriendProfileActivity.this, user.getMUserName());
        } else {
            // 直接添加好友
        }
    }

    private void syncFriendUserInfo(){
        // 从服务器异步加载用户的最新信息，填充到好友列表或者新的朋友列表
    }
}
