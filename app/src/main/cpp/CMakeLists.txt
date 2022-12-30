# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("calculator")


add_library( # Sets the name of the library.
native-lib

# Sets the library as a shared library.
SHARED

# Provides a relative path to your source file(s).
native-lib.cpp )

add_library(GLES_mali STATIC IMPORTED)

add_library( # Sets the name of the library.
        JNIDriver

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        JNIDriver.c)

add_library( # Sets the name of the library.
        JNILEDDriver

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        JNILEDDriver.c)

add_library( # Sets the name of the library.
        JNISegmentDriver

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        JNISegmentDriver.c)

add_library( # Sets the name of the library.
OpenCLDriver

# Sets the library as a shared library.
SHARED

# Provides a relative path to your source file(s).
OpenCLDriver.c )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
jnigraphics-lib

# Specifies the name of the NDK library that
# you want CMake to locate.
jnigraphics )

find_library( # Sets the name of the path variable.
log-lib

# Specifies the name of the NDK library that
# you want CMake to locate.
log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
native-lib

# Links the target library to the log library
# included in the NDK.
${log-lib} )

target_link_libraries( # Specifies the target library.
OpenCLDriver

# Links the target library to the log library
# included in the NDK.
C:/Users/OpenCL_lib_and_include/lib/libGLES_mali.so ${log-lib} ${jnigraphics-lib} )

target_include_directories(OpenCLDriver PRIVATE src/main/cpp)

target_link_libraries( # Specifies the target library.
        JNIDriver

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

target_link_libraries( # Specifies the target library.
        JNISegmentDriver

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

target_link_libraries( # Specifies the target library.
        JNILEDDriver

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})