package cn.ucai.wechat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import cn.ucai.wechat.R;
import cn.ucai.wechat.runtimepermissions.PermissionsManager;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MFGT;

import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.util.EasyUtils;

/**
 * chat activity，EaseChatFragment was used {@link EaseChatFragment}
 *
 */
public class ChatActivity extends BaseActivity{
    private static final String TAG = "ChatActivity";

    public static ChatActivity activityInstance;
    private EaseChatFragment chatFragment;
    String toChatUsername;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.em_activity_chat);
        activityInstance = this;
        //get user id or group id
        toChatUsername = getIntent().getExtras().getString("userId");
        //use EaseChatFratFragment
        chatFragment = new ChatFragment();
        //pass parameters to chat fragment
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityInstance = null;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	// make sure only one chat activity is opened
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent); // Activity又将intent传送出去、最终到EaseChatFragment
        else {
            finish();
            startActivity(intent);
        }

    }
    
    @Override
    public void onBackPressed() {
//        L.e(TAG, "onBackPressed, --1");
        chatFragment.onBackPressed();
//        L.e(TAG, "onBackPressed, --2");
        if (EasyUtils.isSingleActivity(this)) {
            MFGT.gotoMainActivity(ChatActivity.this, true);
//            L.e(TAG, "onBackPressed, --3");
        }
//        L.e(TAG, "onBackPressed, --4");
    }
    
    public String getToChatUsername(){
        return toChatUsername;
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }
}
