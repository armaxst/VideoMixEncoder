#pragma once

namespace maxstAR
{

	struct Matrix44F 
	{
		float data[4 * 4];

		static Matrix44F identity()
		{
			Matrix44F matrix;
			for (int i = 0; i < 16; i++)
			{
				matrix.data[i] = 0.0f;
			}

			matrix.data[0] = 1.0f;
			matrix.data[5] = 1.0f;
			matrix.data[10] = 1.0f;
			matrix.data[15] = 1.0f;

			return matrix;
		}
	};

	struct Vector3F 
	{
		float x;
		float y;
		float z;
	};
}
