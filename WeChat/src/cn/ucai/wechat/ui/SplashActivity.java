package cn.ucai.wechat.ui;

import android.content.Intent;
import android.os.Bundle;

import com.hyphenate.chat.EMClient;
import cn.ucai.wechat.WeChatHelper;
import cn.ucai.wechat.R;
import cn.ucai.wechat.utils.MFGT;

import com.hyphenate.util.EasyUtils;

/**
 * 开屏页
 *
 */
public class SplashActivity extends BaseActivity {

	private static final int sleepTime = 2000;

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.em_activity_splash);
		super.onCreate(arg0);

//		RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.splash_root);
//		TextView versionText = (TextView) findViewById(R.id.tv_version);
//
//		versionText.setText(getVersion());
//		AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
//		animation.setDuration(1500);
//		rootLayout.startAnimation(animation);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// 闪屏界面保持2秒钟，如果不足2秒Thread.sleep(sleepTime - costTime)
		new Thread(new Runnable() {
			public void run() {
				if (WeChatHelper.getInstance().isLoggedIn()) {
					// auto login mode, make sure all group and conversation is loaed before enter the main screen
					long start = System.currentTimeMillis();
					EMClient.getInstance().chatManager().loadAllConversations();
					EMClient.getInstance().groupManager().loadAllGroups();
					long costTime = System.currentTimeMillis() - start;
					//wait
					if (sleepTime - costTime > 0) {
						try {
							Thread.sleep(sleepTime - costTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					String topActivityName = EasyUtils.getTopActivityName(EMClient.getInstance().getContext());
					if (topActivityName != null && (topActivityName.equals(VideoCallActivity.class.getName()) || topActivityName.equals(VoiceCallActivity.class.getName()))) {
						// nop
						// avoid main screen overlap Calling Activity
					} else {
						//enter main screen
						MFGT.gotoMainActivity(SplashActivity.this);
					}
					finish();
				}else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}
					MFGT.gotoGuide(SplashActivity.this);
					finish();
				}
			}
		}).start();

	}
	
	/**
	 * get sdk version
	 */
	private String getVersion() {
	    return EMClient.getInstance().VERSION;
	}
}
