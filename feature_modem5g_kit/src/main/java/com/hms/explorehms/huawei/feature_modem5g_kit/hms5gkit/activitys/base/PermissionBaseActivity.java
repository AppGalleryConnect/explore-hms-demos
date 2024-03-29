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

package com.hms.explorehms.huawei.feature_modem5g_kit.hms5gkit.activitys.base;


import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionBaseActivity extends BaseActivity {
    private static final String TAG = "[5ghmskit] PermissionBaseActivity";
    public static final int REQUEST_CODE = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static List<String> sPermissionList = new ArrayList<>();
    private PermissionListener mlistener;

    public void verifyStoragePermissions(PermissionListener permissionListener) {
        mlistener = permissionListener;
        sPermissionList.clear();
        for (String permission : PERMISSIONS_STORAGE) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                sPermissionList.add(permission);
            }
        }

        if (!sPermissionList.isEmpty()) {
            Log.d(TAG, "Obtain the corresponding permissions");
            String[] permissions = sPermissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        } else {
            Log.d(TAG, "All authorized");
            permissionListener.onGranted(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length <= 0) {
                return;
            }
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    String permission = permissions[i];
                    deniedPermissions.add(permission);
                    break;
                }
            }

            if (deniedPermissions.isEmpty()) {
                mlistener.onGranted(true);
            } else {
                mlistener.onGranted(false);
            }
        }
    }

    public interface PermissionListener {
        void onGranted(boolean flag);
    }
}