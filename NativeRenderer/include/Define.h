#pragma once

#if (defined WIN32) && defined MAXSTAR_API_EXPORTS
#  define MAXSTAR_EXPORTS __declspec(dllexport)
#else
#  define MAXSTAR_EXPORTS
#endif

typedef unsigned char Byte;