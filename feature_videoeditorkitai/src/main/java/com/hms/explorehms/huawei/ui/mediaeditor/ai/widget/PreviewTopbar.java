
/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hms.explorehms.huawei.ui.mediaeditor.ai.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.hms.explorehms.huawei.feature_videoeditorkitai.R;

public class PreviewTopbar extends ConstraintLayout implements View.OnClickListener {

    public static final int PanelFilter = 1;

    private static final int ANIMATION_DURATION = 400;

    private Button mClose;

    private View mBtnSwitch;

    private ImageView mIconSwitch;

    private View mEffect;

    private OnCameraCloseListener mCameraCloseListener;

    private OnCameraSwitchListener mCameraSwitchListener;

    private OnShowListener mShowListener;

    public PreviewTopbar(Context context) {
        this(context, null);
    }

    public PreviewTopbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewTopbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_preview_topbar, this);
        initView();
    }

    private void initView() {
        mClose = findViewById(R.id.btn_close);
        mClose.setOnClickListener(this);

        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
        mIconSwitch = findViewById(R.id.iv_switch);

        mEffect = findViewById(R.id.btn_effect);
        mEffect.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            closeCamera();
        } else if (id == R.id.btn_switch) {
            switchCamera();
        } else if (id == R.id.btn_effect) {
            showEffectPanel();
        }
    }

    private void closeCamera() {
        if (mCameraCloseListener != null) {
            mCameraCloseListener.onCameraClose();
        }
    }

    private void switchCamera() {
        if (mCameraSwitchListener != null) {
            mIconSwitch.setPivotX(mIconSwitch.getWidth() / 2f);
            mIconSwitch.setPivotY(mIconSwitch.getHeight() / 2f);

            mBtnSwitch.setEnabled(false);
            mBtnSwitch.postDelayed(() -> {
                mBtnSwitch.setEnabled(true);
            }, ANIMATION_DURATION);
            mCameraSwitchListener.onCameraSwitch();
        }
    }

    private void showEffectPanel() {
        if (mShowListener != null) {
            mShowListener.onShowPanel(PanelFilter);
        }
    }

    public interface OnShowListener {
        void onShowPanel(int type);
    }

    public PreviewTopbar addOnShowListener(OnShowListener listener) {
        mShowListener = listener;
        return this;
    }

    public interface OnCameraSwitchListener {
        void onCameraSwitch();
    }

    public PreviewTopbar addOnCameraSwitchListener(OnCameraSwitchListener listener) {
        mCameraSwitchListener = listener;
        return this;
    }

    public interface OnCameraCloseListener {
        void onCameraClose();
    }

    public PreviewTopbar addOnCameraCloseListener(OnCameraCloseListener listener) {
        mCameraCloseListener = listener;
        return this;
    }
}
