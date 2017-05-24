package cn.ucai.wechat.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.ucai.wechat.R;
import cn.ucai.wechat.utils.L;

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
}
