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
package com.hms.explorehms.huawei.feature_arengine.ui.hand.rendering;

import android.opengl.GLES20;
import android.util.Log;

public class HandShaderUtil {

    private static final String TAG = HandShaderUtil.class.getSimpleName();

    /**
     * Newline character.
     */
    public static final String LS = System.lineSeparator();

    /**
     * Code for the hand vertex shader.
     */
    public static final String HAND_VERTEX =
            "uniform vec4 inColor;" + LS
                    + "attribute vec4 inPosition;" + LS
                    + "uniform float inPointSize;" + LS
                    + "varying vec4 varColor;" + LS
                    + "uniform mat4 inMVPMatrix;" + LS
                    + "void main() {" + LS
                    + "    gl_PointSize = inPointSize;" + LS
                    + "    gl_Position = inMVPMatrix * vec4(inPosition.xyz, 1.0);" + LS
                    + "    varColor = inColor;" + LS
                    + "}";

    /**
     * Code for the hand fragment shader.
     */
    public static final String HAND_FRAGMENT =
            "precision mediump float;" + LS
                    + "varying vec4 varColor;" + LS
                    + "void main() {" + LS
                    + "    gl_FragColor = varColor;" + LS
                    + "}";

    private HandShaderUtil() {
    }

    static int createGlProgram() {
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, HandShaderUtil.HAND_VERTEX);
        if (vertex == 0) {
            return 0;
        }
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, HandShaderUtil.HAND_FRAGMENT);
        if (fragment == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertex);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "glError: Could not compile shader " + shaderType);
                Log.e(TAG, "GLES20 Error: " + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

}
