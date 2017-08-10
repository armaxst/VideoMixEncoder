/*
 * Copyright 2016 Maxst, Inc. All Rights Reserved.
 */

package com.maxst.surfaceEncoder;

import android.opengl.GLES20;

import com.maxst.ar.BackgroundTexture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BackgroundQuad {

	private static final String VERTEX_SHADER_SRC =
			"attribute vec4 a_position;\n" +
					"attribute vec2 a_texCoord;\n" +
					"varying vec2 v_texCoord;\n" +
					"uniform mat4 u_mvpMatrix;\n" +
					"void main()							\n" +
					"{										\n" +
					"	gl_Position = u_mvpMatrix * a_position;\n" +
					"	v_texCoord = a_texCoord; 			\n" +
					"}										\n";

	private static final String FRAGMENT_SHADER_SRC =
			"precision mediump float;\n" +
					"varying vec2 v_texCoord;\n" +
					"uniform sampler2D u_texture;\n" +

					"void main(void)\n" +
					"{\n" +
					"	gl_FragColor = texture2D(u_texture, v_texCoord);\n" +
					"}\n";


	private static final float[] VERTEX_BUF = {
			-0.5f, 0.5f, 0.0f,   // top left
			-0.5f, -0.5f, 0.0f,   // bottom left
			0.5f, -0.5f, 0.0f,   // bottom right
			0.5f, 0.5f, 0.0f  // top right
	};

	private static final byte INDEX_BUF[] = {
			0, 1, 2, 2, 3, 0
	};

	private static final float[] TEXTURE_COORD_BUF = {
			0.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f,
			1.0f, 0.0f,
	};

	private int shaderProgramId = 0;
	private int positionHandle;
	private int textureCoordHandle;
	private int mvpMatrixHandle;
	private int textureHandle;

	private FloatBuffer vertexBuffer;
	private ByteBuffer indexBuffer;
	private FloatBuffer textureCoordBuff;

	public BackgroundQuad() {
		ByteBuffer bb = ByteBuffer.allocateDirect(VERTEX_BUF.length * Float.SIZE / 8);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(VERTEX_BUF);
		vertexBuffer.position(0);

		bb = ByteBuffer.allocateDirect(INDEX_BUF.length * Byte.SIZE / 8);
		bb.order(ByteOrder.nativeOrder());
		indexBuffer = bb;
		indexBuffer.put(INDEX_BUF);
		indexBuffer.position(0);

		bb = ByteBuffer.allocateDirect(TEXTURE_COORD_BUF.length * Float.SIZE / 8);
		bb.order(ByteOrder.nativeOrder());
		textureCoordBuff = bb.asFloatBuffer();
		textureCoordBuff.put(TEXTURE_COORD_BUF);
		textureCoordBuff.position(0);

		shaderProgramId = ShaderUtil.createProgram(VERTEX_SHADER_SRC, FRAGMENT_SHADER_SRC);

		positionHandle = GLES20.glGetAttribLocation(shaderProgramId, "a_position");
		textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramId, "a_texCoord");
		mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramId, "u_mvpMatrix");
		textureHandle = GLES20.glGetUniformLocation(shaderProgramId, "u_texture");
	}

	public void draw(BackgroundTexture texture, float [] projectionMatrix) {
		if (texture == null) {
			return;
		}
		GLES20.glUseProgram(shaderProgramId);

		GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
				0, vertexBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);

		GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false,
				0, textureCoordBuff);
		GLES20.glEnableVertexAttribArray(textureCoordHandle);

		GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, projectionMatrix, 0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glUniform1i(textureHandle, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureId());

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_BUF.length, GLES20.GL_UNSIGNED_BYTE, indexBuffer);

		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(textureCoordHandle);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}
}
