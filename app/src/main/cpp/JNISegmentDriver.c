#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>
#include <jni.h>
#include <jni.h>

int fd = 0;

JNIEXPORT jint JNICALL
Java_com_example_calculator_MainActivity_openDriver2(JNIEnv
* env,
jclass clazz, jstring
path) {
// TODO: implement openDriver2()

    jboolean iscopy;
    const char *path_utf = (*env)->GetStringUTFChars(env,path,&iscopy);
    fd = open(path_utf,O_WRONLY);
    (*env)->ReleaseStringUTFChars(env,path,path_utf);

    if(fd<0) return -1;
    else return 1;
}

JNIEXPORT void JNICALL
Java_com_example_calculator_MainActivity_closeDriver2(JNIEnv *env, jclass clazz) {
    // TODO: implement closeDriver2()
    if(fd>0) close(fd);
}

JNIEXPORT void JNICALL
Java_com_example_calculator_MainActivity_writeDriver2(JNIEnv * env, jclass class, jbyteArray arr, jint count) {
// TODO: implement writeDriver()
    jbyte* chars = (*env)->GetByteArrayElements(env,arr,0);
    if(fd>0) write(fd, (unsigned char*)chars,count);
    (*env)->ReleaseByteArrayElements(env, arr, chars, 0);
}