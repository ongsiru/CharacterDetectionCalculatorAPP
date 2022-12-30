#include<jni.h>
#include<stdio.h>
#include<stdlib.h>
#include <sys/time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <CL/opencl.h>
#include <assert.h>

#define CL_FILE "/data/local/tmp/Blur.cl"

#define LOG_TAG "DEBUG"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define RGB8888_A(p) ((p & (0xff<<24))      >> 24 )
#define RGB8888_B(p) ((p & (0xff << 16)) >> 16 )
#define RGB8888_G(p) ((p & (0xff << 8))  >> 8 )
#define RGB8888_R(p) (p & (0xff) )

#define CHECK_CL(err) {\
    cl_int er = (err);\
    if(er<0 && er > -64){\
        LOGE("%d line, OpenCL Error:%d\n",__LINE__,er);\
    }\
}

JNIEXPORT jobject JNICALL
Java_com_example_calculator_MainActivity_GaussianBlurGPU (JNIEnv *env, jclass class, jobject bitmap )
{
    //getting bitmap info:
    LOGD("reading bitmap info...");
    AndroidBitmapInfo info;
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return NULL;
    }
    LOGD("width:%d height:%d stride:%d", info.width, info.height, info.stride);
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return NULL;
    }


    //read pixels of bitmap into native memory :
    LOGD("reading bitmap pixels...");
    void* bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return NULL;
    }
    uint32_t* src = (uint32_t*) bitmapPixels;
    uint32_t* tempPixels = (uint32_t*)malloc(info.height * info.width*4);
    int pixelsCount = info.height * info.width;
    memcpy(tempPixels, src, sizeof(uint32_t) * pixelsCount);

    ////GPU
    LOGD("GPU Start");
    FILE* file_handle;
    char* kernel_file_buffer, * file_log;;
    size_t kernel_file_size, log_size;

    unsigned char* cl_file_name = CL_FILE;
    unsigned char* kernel_name = "kernel_blur";

    //Device input buffers
    cl_mem d_src;
    //Device output buffer
    cl_mem d_dst;

    cl_platform_id clPlatform;        //OpenCL platform
    cl_device_id device_id;            //device ID
    cl_context context;                //context
    cl_command_queue queue;            //command queue
    cl_program program;                //program
    cl_kernel kernel;                //kernel
    LOGD("cl_file_open");
    file_handle = fopen(cl_file_name, "r");
    if (file_handle == NULL) {
        printf("Couldn't find the file");
        exit(1);
    }

    //read kernel file
    fseek(file_handle, 0, SEEK_END);
    kernel_file_size = ftell(file_handle);
    rewind(file_handle);
    kernel_file_buffer = (char*)malloc(kernel_file_size + 1);
    kernel_file_buffer[kernel_file_size] = '\0';
    fread(kernel_file_buffer, sizeof(char), kernel_file_size, file_handle);
    fclose(file_handle);
    LOGD("%s",kernel_file_buffer);
    LOGD("file_buffer_read");
    // Initialize vectors on host
    int i;

    size_t globalSize, localSize, grid;
    cl_int err;

    // Number of work items in each local work group
    localSize = 64;
    int n_pix = info.width * info.height;

    //Number of total work items - localSize must be devisor
    grid = (n_pix % localSize) ? (n_pix / localSize) + 1 : n_pix / localSize;
    globalSize = grid * localSize;

    LOGD("calc grid and globalSize");
    //openCL 기반 실행

    //Bind to platform
    LOGD("error check");
    CHECK_CL(clGetPlatformIDs(1, &clPlatform, NULL));
    LOGD("error end check");
    //Get ID for the device
    CHECK_CL(clGetDeviceIDs(clPlatform, CL_DEVICE_TYPE_GPU, 1, &device_id, NULL));
    //Create a context
    context = clCreateContext(0, 1, &device_id, NULL, NULL, &err);
    CHECK_CL(err);
    //Create a command queue
    queue = clCreateCommandQueue(context, device_id, 0, &err);
    CHECK_CL(err);
    //Create the compute program from the source buffer
    program = clCreateProgramWithSource(context, 1, (const char**)&kernel_file_buffer, &kernel_file_size, &err);
    CHECK_CL(err);
    //Build the program executable
    err = clBuildProgram(program, 0, NULL, NULL, NULL, NULL);
    LOGD("error 22 check");
    CHECK_CL(err);
    if (err != CL_SUCCESS) {
        //LOGD("%s", err);
        size_t len;
        char buffer[4096];
        LOGD("Error: Failed to build program executable!");
        clGetProgramBuildInfo(program, device_id, CL_PROGRAM_BUILD_LOG, sizeof(buffer),
                              buffer, &len);

        LOGD("%s", buffer);
        //exit(1);
    }
    LOGD("error 323 check");

    //Create the compute kernel in the program we wish to run
    kernel = clCreateKernel(program, kernel_name, &err);
    CHECK_CL(err);




    //////openCL 커널 수행
    //Create the input and output arrays in device memory for our calculation
    d_src = clCreateBuffer(context, CL_MEM_READ_ONLY, sizeof(uint32_t)*info.width*info.height, NULL, NULL);
    d_dst = clCreateBuffer(context, CL_MEM_WRITE_ONLY, sizeof(uint32_t)*info.width*info.height, NULL, NULL);

    //Write our data set into the input array in device memory
    CHECK_CL(clEnqueueWriteBuffer(queue, d_src, CL_TRUE, 0, sizeof(uint32_t)*info.width*info.height, tempPixels, 0, NULL, NULL));

    //Set the arguments to our compute kernel
    CHECK_CL(clSetKernelArg(kernel, 0, sizeof(cl_mem), &d_src));
    CHECK_CL(clSetKernelArg(kernel, 1, sizeof(cl_mem), &d_dst));
    CHECK_CL(clSetKernelArg(kernel, 2, sizeof(uint32_t), &info.width));
    CHECK_CL(clSetKernelArg(kernel, 3, sizeof(uint32_t), &info.height));


    //Execute the kernel over the entire range of the data set
    CHECK_CL(clEnqueueNDRangeKernel(queue, kernel, 1, NULL, &globalSize, &localSize, 0, NULL, NULL));
    //Wait for the command queue to get serviced before reading back results
    CHECK_CL(clFinish(queue));
    //read the results form the device
    CHECK_CL(clEnqueueReadBuffer(queue, d_dst, CL_TRUE, 0, sizeof(uint32_t)*info.width*info.height, src, 0, NULL, NULL));


    // release OpenCL resources
    CHECK_CL(clReleaseMemObject(d_src));
    CHECK_CL(clReleaseMemObject(d_dst));
    CHECK_CL(clReleaseProgram(program));
    CHECK_CL(clReleaseKernel(kernel));
    CHECK_CL(clReleaseCommandQueue(queue));
    CHECK_CL(clReleaseContext(context));

    AndroidBitmap_unlockPixels(env, bitmap);
    //
    // free the native memory used to store the pixels
    //
    free(tempPixels);
    return bitmap;

}