/*
 * Copyright 2016 Maxst, Inc. All Rights Reserved.
 */

package com.maxst.videomixer;

import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class BaseModel {

	float[] localMvpMatrix = new float[16];
	float [] projectionMatrix = new float[16];
	float[] modelMatrix = new float[16];
	float[] translation = new float[16];
	float[] scale = new float[16];
	float[] rotation = new float[16];
	float[] transform = new float[16];

	int shaderProgramId = 0;
	int positionHandle;
	int colorHandle;
	int textureCoordHandle;
	int mvpMatrixHandle;
	int textureHandle;

	FloatBuffer vertexBuffer;
	ShortBuffer indexBuffer;
	FloatBuffer colorBuffer;
	FloatBuffer textureCoordBuff;

	public BaseModel() {
		Matrix.setIdentityM(localMvpMatrix, 0);
		Matrix.setIdentityM(projectionMatrix, 0);
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.setIdentityM(translation, 0);
		Matrix.setIdentityM(scale, 0);
		Matrix.setIdentityM(rotation, 0);
	}

	public abstract void draw();

	public void setProjectionMatrix(float [] projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}

	public void setScale(float x, float y, float z) {
		Matrix.setIdentityM(scale, 0);
		Matrix.scaleM(scale, 0, x, y, z);
	}

	public void setTranslate(float x, float y, float z) {
		Matrix.setIdentityM(translation, 0);
		Matrix.translateM(translation, 0, x, y, z);
	}

	public void setRotation(float angle, float x, float y, float z) {
		Matrix.setIdentityM(rotation, 0);
		Matrix.rotateM(rotation, 0, angle, x, y, z);
	}

	public void setTransform(float[] transform) {
		System.arraycopy(transform, 0, this.transform, 0, transform.length);
	}
}
