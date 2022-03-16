#include <jni.h>
#include <string>

std::string appSignatureSha1 = "";
std::string appPackageName = "";

bool isLegalPackage(jobject thiz){

    return true;
}

bool isLegalSignature(jobject thiz){
    return true;
}
std::string aesKey = "FRN5qiPUWMvCwsQ1c942RhreQOV4p5QB";

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lixiangya_cet46phrase_MyApplication_loadKey(JNIEnv *env, jobject thiz) {
    std::string key = "";
    if ((!isLegalPackage(thiz)) || (!isLegalSignature(thiz))) {
        return env->NewStringUTF(key.c_str());
    }

    return env->NewStringUTF(aesKey.c_str());
}

