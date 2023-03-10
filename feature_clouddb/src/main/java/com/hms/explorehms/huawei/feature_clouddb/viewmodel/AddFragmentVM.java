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
package com.hms.explorehms.huawei.feature_clouddb.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hms.explorehms.huawei.feature_clouddb.dao.CloudDBZoneWrapper;
import com.hms.explorehms.huawei.feature_clouddb.model.BookComment;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class AddFragmentVM extends AndroidViewModel {

    private static final String TAG = "AddFragmentVM";

    public AddFragmentVM(@NonNull Application application) {
        super(application);
    }

    public void addNewComment(BookComment comment) {
        if (comment != null) {
            Random generateRandomNumber = null;
            try {
                generateRandomNumber = SecureRandom.getInstanceStrong();
                int low = 1;
                int max = 100000000;
                int id = generateRandomNumber.nextInt(max - low) + low;
                comment.setId(id);
                CloudDBZoneWrapper.upsertData(comment);

            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "addNewCommentError:" + e.getMessage());
            }
        }
    }
}
