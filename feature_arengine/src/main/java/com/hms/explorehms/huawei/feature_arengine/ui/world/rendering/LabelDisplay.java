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
package com.hms.explorehms.huawei.feature_arengine.ui.world.rendering;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.util.Pair;

import com.hms.explorehms.huawei.feature_arengine.utils.ShaderUtil;
import com.huawei.hiar.ARPlane;
import com.huawei.hiar.ARPose;
import com.huawei.hiar.ARTrackable;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class LabelDisplay {
    private static final String TAG = LabelDisplay.class.getSimpleName();

    private static final int COORDS_PER_VERTEX = 3;

    private static final float LABEL_WIDTH = 0.3f;

    private static final float LABEL_HEIGHT = 0.3f;

    private static final int TEXTURES_SIZE = 12;

    private static final int MATRIX_SIZE = 16;

    private static final int PLANE_ANGLE_MATRIX_SIZE = 4;

    private final int[] textures = new int[TEXTURES_SIZE];

    // Allocate a temporary list/matrix here to reduce the number of allocations per frame.
    private final float[] modelMatrix = new float[MATRIX_SIZE];

    private final float[] modelViewMatrix = new float[MATRIX_SIZE];

    private final float[] modelViewProjectionMatrix = new float[MATRIX_SIZE];

    // A 2 * 2 rotation matrix applied to the uv coordinates.
    private final float[] planeAngleUvMatrix = new float[PLANE_ANGLE_MATRIX_SIZE];

    private int mProgram;

    private int glPositionParameter;

    private int glModelViewProjectionMatrix;

    private int glTexture;

    private int glPlaneUvMatrix;

    /**
     * Create the shader program for label display in the openGL thread.
     * This method will be called when {@link WorldRenderManager#onSurfaceCreated}.
     *
     * @param labelBitmaps View data indicating the plane type.
     */
    public void init(List<Bitmap> labelBitmaps) {
        ShaderUtil.checkGlError(TAG, "Init start.");
        if (labelBitmaps.size() == 0) {
            Log.e(TAG, "No bitmap.");
        }
        createProgram();
        int idx = 0;
        GLES20.glGenTextures(textures.length, textures, 0);
        for (Bitmap labelBitmap : labelBitmaps) {
            // for semantic label plane
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + idx);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[idx]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, labelBitmap, 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            idx++;
            ShaderUtil.checkGlError(TAG, "Texture loading");
        }
        ShaderUtil.checkGlError(TAG, "Init end.");
    }

    private void createProgram() {
        ShaderUtil.checkGlError(TAG, "program start.");
        mProgram = WorldShaderUtil.getLabelProgram();
        glPositionParameter = GLES20.glGetAttribLocation(mProgram, "inPosXZAlpha");
        glModelViewProjectionMatrix = GLES20.glGetUniformLocation(mProgram, "inMVPMatrix");
        glTexture = GLES20.glGetUniformLocation(mProgram, "inTexture");
        glPlaneUvMatrix = GLES20.glGetUniformLocation(mProgram, "inPlanUVMatrix");
        ShaderUtil.checkGlError(TAG, "program end.");
    }

    /**
     * Render the plane type at the center of the currently identified plane.
     * This method will be called when {@link WorldRenderManager#onDrawFrame}.
     *
     * @param allPlanes All identified planes.
     * @param cameraPose Location and pose of the current camera.
     * @param cameraProjection Projection matrix of the current camera.
     */
    public void onDrawFrame(Collection<ARPlane> allPlanes, ARPose cameraPose, float[] cameraProjection) {
        ArrayList<ARPlane> sortedPlanes = getSortedPlanes(allPlanes, cameraPose);
        float[] cameraViewMatrix = new float[MATRIX_SIZE];
        cameraPose.inverse().toMatrix(cameraViewMatrix, 0);
        drawSortedPlans(sortedPlanes, cameraViewMatrix, cameraProjection);
    }

    private ArrayList<ARPlane> getSortedPlanes(Collection<ARPlane> allPlanes, ARPose cameraPose) {
        // Planes must be sorted by the distance from the camera so that we can
        // first draw the closer planes, and have them block the further planes.
        ArrayList<Pair<ARPlane, Float>> pairPlanes = new ArrayList<>();
        for (ARPlane plane : allPlanes) {
            if ((plane.getType() == ARPlane.PlaneType.UNKNOWN_FACING)
                    || plane.getTrackingState() != ARTrackable.TrackingState.TRACKING || plane.getSubsumedBy() != null) {
                continue;
            }

            // Store the normal vector of the current plane.
            float[] planeNormalVector = new float[3];
            ARPose planeCenterPose = plane.getCenterPose();
            planeCenterPose.getTransformedAxis(1, 1.0f, planeNormalVector, 0);

            // Calculate the distance from the camera to the plane. If the value is negative,
            // it indicates that the camera is behind the plane (the normal vector distinguishes
            // the front side from the back side).
            float distanceBetweenPlaneAndCamera = (cameraPose.tx() - planeCenterPose.tx()) * planeNormalVector[0]
                    + (cameraPose.ty() - planeCenterPose.ty()) * planeNormalVector[1]
                    + (cameraPose.tz() - planeCenterPose.tz()) * planeNormalVector[2];
            pairPlanes.add(new Pair<>(plane, distanceBetweenPlaneAndCamera));
        }

        pairPlanes.sort(new PlanCompare());

        ArrayList<ARPlane> sortedPlanes = new ArrayList<>();
        for (Pair<ARPlane, Float> eachPlane : pairPlanes) {
            sortedPlanes.add(eachPlane.first);
        }
        return sortedPlanes;
    }

    /**
     * Sort the planes.
     *
     * @author HW
     * @since 2020-04-17
     */
    static class PlanCompare implements Comparator<Pair<ARPlane, Float>>, Serializable {
        private static final long serialVersionUID = -7710923839970415650L;

        @Override
        public int compare(Pair<ARPlane, Float> planA, Pair<ARPlane, Float> planB) {
            return planB.second.compareTo(planA.second);
        }
    }

    private void drawSortedPlans(ArrayList<ARPlane> sortedPlanes, float[] cameraViews, float[] cameraProjection) {
        ShaderUtil.checkGlError(TAG, "Draw sorted plans start.");

        GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFuncSeparate(GLES20.GL_DST_ALPHA, GLES20.GL_ONE, GLES20.GL_ZERO, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(glPositionParameter);

        for (ARPlane plane : sortedPlanes) {
            float[] planeMatrix = new float[MATRIX_SIZE];
            plane.getCenterPose().toMatrix(planeMatrix, 0);

            System.arraycopy(planeMatrix, 0, modelMatrix, 0, MATRIX_SIZE);

            float scaleU = 1.0f / LABEL_WIDTH;

            // Set the value of the plane angle uv matrix.
            planeAngleUvMatrix[0] = scaleU;
            planeAngleUvMatrix[1] = 0.0f;
            planeAngleUvMatrix[2] = 0.0f;
            float scaleV = 1.0f / LABEL_HEIGHT;
            planeAngleUvMatrix[3] = scaleV;

            int idx = plane.getLabel().ordinal();
            Log.d(TAG, "Plane getLabel:" + idx);
            idx = Math.abs(idx);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + idx);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[idx]);
            GLES20.glUniform1i(glTexture, idx);
            GLES20.glUniformMatrix2fv(glPlaneUvMatrix, 1, false, planeAngleUvMatrix, 0);

            drawLabel(cameraViews, cameraProjection);
        }

        GLES20.glDisableVertexAttribArray(glPositionParameter);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDepthMask(true);
        ShaderUtil.checkGlError(TAG, "Draw sorted plans end.");
    }

    private void drawLabel(float[] cameraViews, float[] cameraProjection) {
        ShaderUtil.checkGlError(TAG, "Draw label start.");
        Matrix.multiplyMM(modelViewMatrix, 0, cameraViews, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraProjection, 0, modelViewMatrix, 0);

        float halfWidth = LABEL_WIDTH / 2.0f;
        float halfHeight = LABEL_HEIGHT / 2.0f;
        float[] vertices = {-halfWidth, -halfHeight, 1, -halfWidth, halfHeight, 1, halfWidth, halfHeight, 1, halfWidth,
                -halfHeight, 1};

        // The size of each floating point is 4 bits.
        FloatBuffer vetBuffer =
                ByteBuffer.allocateDirect(4 * vertices.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vetBuffer.rewind();
        for (int i = 0; i < vertices.length; ++i) {
            vetBuffer.put(vertices[i]);
        }
        vetBuffer.rewind();

        // The size of each floating point is 4 bits.
        GLES20.glVertexAttribPointer(glPositionParameter, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                4 * COORDS_PER_VERTEX, vetBuffer);

        // Set the sequence of OpenGL drawing points to generate two triangles that form a plane.
        short[] indices = {0, 1, 2, 0, 2, 3};

        // Size of the allocated buffer.
        ShortBuffer idxBuffer =
                ByteBuffer.allocateDirect(2 * indices.length).order(ByteOrder.nativeOrder()).asShortBuffer();
        idxBuffer.rewind();
        for (int i = 0; i < indices.length; ++i) {
            idxBuffer.put(indices[i]);
        }
        idxBuffer.rewind();

        GLES20.glUniformMatrix4fv(glModelViewProjectionMatrix, 1, false, modelViewProjectionMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, idxBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, idxBuffer);
        ShaderUtil.checkGlError(TAG, "Draw label end.");
    }
}
