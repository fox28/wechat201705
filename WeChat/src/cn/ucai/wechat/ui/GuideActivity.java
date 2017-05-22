package cn.ucai.wechat.ui;

import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.wechat.R;
import cn.ucai.wechat.utils.MFGT;

/**
 * Created by apple on 2017/5/23.
 */

public class GuideActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.btn_login, R.id.btn_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                MFGT.gotoLoginActivity(GuideActivity.this);
                break;
            case R.id.btn_register:
                MFGT.gotoRegisterActivity(GuideActivity.this);
                break;
        }
    }
}
