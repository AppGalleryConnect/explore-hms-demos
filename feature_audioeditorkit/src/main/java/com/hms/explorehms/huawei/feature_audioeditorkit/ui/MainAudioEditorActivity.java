/*
 *
 *   Copyright 2020. Explore in HMS. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.explorehms.huawei.feature_audioeditorkit.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hms.explorehms.huawei.feature_audioeditorkit.R;
import com.hms.explorehms.huawei.feature_audioeditorkit.util.FileUtils;
import com.hms.explorehms.huawei.feature_audioeditorkit.util.PermissionUtils;
import com.hms.explorehms.huawei.feature_audioeditorkit.widget.ProgressDialog;
import com.google.android.material.button.MaterialButton;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.audioeditor.common.agc.HAEApplication;
import com.huawei.hms.audioeditor.sdk.AudioExtractCallBack;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;
import com.huawei.hms.audioeditor.ui.api.AudioEditorLaunchOption;
import com.huawei.hms.audioeditor.ui.api.AudioExportCallBack;
import com.huawei.hms.audioeditor.ui.api.AudioInfo;
import com.huawei.hms.audioeditor.ui.api.HAEUIManager;
import com.huawei.hms.audioeditor.ui.api.MenuCommon;

import java.io.IOException;
import java.util.ArrayList;

public class MainAudioEditorActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUESTS = 1;

    private static final int REQUEST_CODE_FOR_SELECT_VIDEO = 1000;

    private final int PERMISSION_TYPE_EDIT = 1;

    private final int PERMISSION_TYPE_EXTRACT = 2;

    private final int PERMISSION_TYPE_FORMAT = 3;

    private int currentPermissionType = PERMISSION_TYPE_EDIT;
    public ProgressDialog fragmentDialog;
    private MaterialButton startEdit;
    private MaterialButton extractAudio;
    private MaterialButton formatMain;
    private Context mContext;
    private final String[] PERMISSIONS =
            new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main_audio_editor);
        HAEApplication.getInstance().setApiKey(AGConnectServicesConfig.fromContext(this).getString("client/api_key"));
        Toolbar toolBar = findViewById(R.id.tb_main_audioeditor);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        initView();
        initEvent();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void requestPermission() throws IOException {
        PermissionUtils.checkMorePermissions(
                mContext,
                PERMISSIONS,
                new PermissionUtils.PermissionCheckCallBack() {
                    @Override
                    public void onHasPermission() throws IOException {
                        if (currentPermissionType == PERMISSION_TYPE_EDIT) {
                            startUIActivity();
                        } else if (currentPermissionType == PERMISSION_TYPE_EXTRACT) {
                            extractAudio();
                        } else if (currentPermissionType == PERMISSION_TYPE_FORMAT) {
                            startFormatActivity();
                        }
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDown(String... permission) {
                        PermissionUtils.requestMorePermissions(mContext, PERMISSIONS, PERMISSION_REQUESTS);
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                        PermissionUtils.requestMorePermissions(mContext, PERMISSIONS, PERMISSION_REQUESTS);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initEvent() {
        startEdit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPermissionType = PERMISSION_TYPE_EDIT;
                        try {
                            MainAudioEditorActivity.this.requestPermission();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        formatMain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPermissionType = PERMISSION_TYPE_FORMAT;
                        try {
                            MainAudioEditorActivity.this.requestPermission();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


        extractAudio.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_EXTRACT;
                    try {
                        requestPermission();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

    }

    private void initView() {
        startEdit = findViewById(R.id.btn_create_audio);
        formatMain = findViewById(R.id.btn_audio_format_conversion);
        extractAudio = findViewById(R.id.btn_video_audio_extraction);
    }

    // The default UI is displayed.

    /**
     * Import audio and enter the audio editing interface management class.
     */
    private void startUIActivity() throws IOException {
        //Old way
        /*HAEUIManager.getInstance().launchEditorActivity(this);
        HAEUIManager.getInstance().setCallback(callBack);*/

        ArrayList<Integer> level1List = new ArrayList<>();
        level1List.add(MenuCommon.MAIN_MENU_ADD_AUDIO_CODE);
        level1List.add(MenuCommon.MAIN_MENU_ADD_SOUND_EFFECTS_CODE);
        level1List.add(MenuCommon.MAIN_MENU_AUDIO_RECORDER_CODE);
        level1List.add(MenuCommon.MAIN_MENU_PIECE_CODE);
        level1List.add(MenuCommon.MAIN_MENU_TEXT_TO_SPEECH_CODE);

        ArrayList<Integer> level2List = new ArrayList<>();

        level2List.add(MenuCommon.EDIT_MENU_BALANCE_CODE);
        level2List.add(MenuCommon.EDIT_MENU_CHANGE_VOICE_CODE);
        level2List.add(MenuCommon.EDIT_MENU_COPY_CODE);
        level2List.add(MenuCommon.EDIT_MENU_DEL_CODE);
        level2List.add(MenuCommon.EDIT_MENU_EFFECT_CODE);
        level2List.add(MenuCommon.EDIT_MENU_FADE_IN_OUT_CODE);
        level2List.add(MenuCommon.EDIT_MENU_SEGREGATED_CODE);
        level2List.add(MenuCommon.EDIT_MENU_SPATIAL_ORIENTATION_CODE);
        level2List.add(MenuCommon.EDIT_MENU_SPLIT_CODE);
        level2List.add(MenuCommon.EDIT_MENU_VOICE_SEGREGATED_CODE);
        level2List.add(MenuCommon.EDIT_MENU_VOLUME_SPEED_CODE);
        level2List.add(MenuCommon.EDIT_MENU_VOLUME2_CODE);
        level2List.add(MenuCommon.EDIT_MENU_VOLUME2_CODE);
        level2List.add(MenuCommon.EDIT_MENU_VOLUME2_CODE);

        AudioEditorLaunchOption.Builder audioEditorLaunch = new AudioEditorLaunchOption.Builder()
                .setCustomMenuList(level1List)
                .setSecondMenuList(level2List)
                .setFilePaths(new ArrayList<>());
        HAEUIManager.getInstance().launchEditorActivity(this, audioEditorLaunch.build());
    }

    // Start the audio format conversion page.
    private void startFormatActivity() {
        Intent safeIntent = new Intent(new Intent());
        safeIntent.setClass(this, AudioFormatActivity.class);
        startActivity(safeIntent);
    }

    // Export interface callback
    private static AudioExportCallBack callBack =
            new AudioExportCallBack() {
                @Override
                public void onAudioExportSuccess(AudioInfo audioInfo) {
                    String mediaPath = audioInfo.getAudioPath();
                    Log.i("MainActivity", "The current audio export path is" + mediaPath);
                }

                @Override
                public void onAudioExportFailed(int i) {}
            };

    /**
     * Display Go to App Settings Dialog
     */
    private void showToAppSettingDialog() {
        new AlertDialog.Builder(this)
                .setMessage("")
                .setPositiveButton(
                        getString(R.string.setting), (dialog, which) -> PermissionUtils.toAppSetting(mContext))
                .setNegativeButton(getString(R.string.cancels), null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUESTS) {
            try {
                PermissionUtils.onRequestMorePermissionsResult(
                        mContext,
                        PERMISSIONS,
                        new PermissionUtils.PermissionCheckCallBack() {
                            @Override
                            public void onHasPermission() throws IOException {
                                if (currentPermissionType == PERMISSION_TYPE_EDIT) {
                                    startUIActivity();
                                } else if (currentPermissionType == PERMISSION_TYPE_EXTRACT) {
                                    extractAudio();
                                }
                            }

                            @Override
                            public void onUserHasAlreadyTurnedDown(String... permission) {
                            }

                            @Override
                            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                                showToAppSettingDialog();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void extractAudio() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_CODE_FOR_SELECT_VIDEO);

    }

    /**
     * Extracting Audio Files from Videos Through Video File Paths
     *
     * @param path Full path of the video file
     */
    private void beginExtractAudio(String path) {
        fragmentDialog = ProgressDialog.newInstance("Extracting");
        fragmentDialog.show(getSupportFragmentManager(), "ProgressDialogFragment");
        String outPutDir = FileUtil.getAudioExtractStorageDirectory(this);
        String name = "audio_extract";
        if (path != null) {
            int slashIndex = path.lastIndexOf("/");
            if (slashIndex == -1) {
                name = path;
            } else {
                name = path.substring(slashIndex + 1);
            }
            int dotIndex = name.lastIndexOf(".");
            if (dotIndex >= 0) {
                name = name.substring(0, dotIndex);
            }
        }
        HAEAudioExpansion.getInstance()
                .extractAudio(
                        this,
                        path,
                        outPutDir,
                        name,
                        new AudioExtractCallBack() {
                            @Override
                            public void onSuccess(String audioPath) {
                                Log.d(TAG, "ExtractAudio onSuccess : " + audioPath);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                fragmentDialog.dismiss();
                                                String format = getResources().getString(R.string.extract_success);
                                                Intent intent = new Intent(getApplicationContext(), VideoAudioExtractionActivity.class);
                                                intent.putExtra("audioPath",audioPath);
                                                startActivity(intent);
                                            }
                                        });
                            }

                            @Override
                            public void onProgress(int progress) {
                                Log.d(TAG, "ExtractAudio onProgress : " + progress);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                fragmentDialog.setProgress(progress);
                                            }
                                        });
                            }

                            @Override
                            public void onFail(int errCode) {
                                Log.i(TAG, "ExtractAudio onFail : " + errCode);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainAudioEditorActivity.this,
                                                        getResources().getString(R.string.extract_fail)
                                                                + "errCode : "
                                                                + errCode,
                                                        Toast.LENGTH_LONG)
                                                        .show();
                                                fragmentDialog.dismiss();
                                            }
                                        });
                            }

                            @Override
                            public void onCancel() {
                                Log.d(TAG, "ExtractAudio onCancel.");
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainAudioEditorActivity.this,
                                                        getResources().getString(R.string.dm_extract_cancel),
                                                        Toast.LENGTH_LONG)
                                                        .show();
                                                fragmentDialog.dismiss();
                                            }
                                        });
                            }
                        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_SELECT_VIDEO) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getResources().getString(R.string.select_none_video), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    String filePath = FileUtils.getRealPath(this, uri);
                    Log.i(TAG, filePath);
                    if (!TextUtils.isEmpty(filePath)) {
                        beginExtractAudio(filePath);
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.file_not_avable), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }
    }
}