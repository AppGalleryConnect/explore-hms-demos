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
package com.hms.explorehms.huawei.feature_arengine.utils;

import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hms.explorehms.huawei.feature_arengine.common.AREngineRuntimeException;

public class ShaderUtil {
    private ShaderUtil() {
    }

    /**
     * Check OpenGL ES running exceptions and throw them when necessary.
     *
     * @param tag Exception information.
     * @param label Program label.
     */
    public static void checkGlError(@NonNull String tag, @NonNull String label) {
        int lastError = GLES20.GL_NO_ERROR;
        int error = GLES20.glGetError();
        while (error != GLES20.GL_NO_ERROR) {
            Log.e(tag, label + ": glError " + error);
            lastError = error;
            error = GLES20.glGetError();
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw new AREngineRuntimeException(label + ": glError " + lastError);
        }
    }
}
