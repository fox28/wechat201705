package cn.ucai.wechat.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.redpacketui.utils.RPRedPacketUtil;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.ucai.wechat.Constant;
import cn.ucai.wechat.R;
import cn.ucai.wechat.utils.L;
import cn.ucai.wechat.utils.MFGT;


/**
 * Created by apple on 2017/5/24.
 */

public class FragmentProfile extends Fragment {
    private static final String TAG = "FragmentProfile";
    @BindView(R.id.iv_profile_avatar)
    ImageView mIvProfileAvatar;
    @BindView(R.id.tv_profile_nickname)
    TextView mTvProfileNickname;
    @BindView(R.id.tv_profile_username)
    TextView mTvProfileUsername;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
    }

    private void initData() {
        String username = EMClient.getInstance().getCurrentUser();
        mTvProfileUsername.setText(username);
        EaseUserUtils.setAppUserAvatar(getContext(), username, mIvProfileAvatar);
        EaseUserUtils.setAppUserNick(username, mTvProfileNickname);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.tv_profile_settings)
    public void onSettings() {
        MFGT.gotoSettings(getActivity());
    }

    @OnClick(R.id.tv_profile_money)
    public void redPacket(){
        //red packet code : 进入零钱或红包记录页面
//        case R.id.ll_change: d
        //支付宝版红包SDK调用如下方法进入红包记录页面
//				RPRedPacketUtil.getInstance().startRecordActivity(SettingsActivity.this);
        //钱包版红包SDK调用如下方法进入零钱页面
        RPRedPacketUtil.getInstance().startChangeActivity(getActivity());
//        break;
        //end of red packet code
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(((MainActivity)getActivity()).isConflict){
            outState.putBoolean("isConflict", true);
        }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
            outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }

}
