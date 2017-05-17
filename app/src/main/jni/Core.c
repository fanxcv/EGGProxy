#include "Core.h"

extern void loadConfTiny(char *conf);

extern void loadConfFmns(char *conf);

char _mode[16];
char _del_h[1024];
int _is_net, _all_https;
char _port_h[6], _port_s[6];
char _host_h[16], _host_s[16];
char _first_h[4096], _first_s[4096];

JNIEXPORT jboolean JNICALL
Java_cn_EGGMaster_util_JniUtils_loadConf(JNIEnv *env, jobject obj, jstring conf, jint type) {

    char *cConf = (char *) (*env)->GetStringUTFChars(env, conf, NULL);

    switch (type) {
        case 0:
            loadConfTiny(cConf);
            break;
        case 1:
            loadConfFmns(cConf);
            break;
        default:
            (*env)->ReleaseStringUTFChars(env, conf, cConf);
            return 0;
    }

    (*env)->ReleaseStringUTFChars(env, conf, cConf);

    if (strcasecmp(_mode, "net") == 0) {
        _is_net = 1;
    } else if (strcasecmp(_mode, "wap_https") == 0) {
        _all_https = 1;
    }

    if (strlen(_first_h) > 0 && strlen(_first_s) > 0)
        return 1;
    else
        return 0;

}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getConfString(JNIEnv *env, jobject obj, jint type) {

    switch (type) {
        case HTTP_IP:
            return (*env)->NewStringUTF(env, _host_h);
        case HTTP_PORT:
            return (*env)->NewStringUTF(env, _port_h);
        case HTTPS_IP:
            return (*env)->NewStringUTF(env, _host_s);
        case HTTPS_PORT:
            return (*env)->NewStringUTF(env, _port_s);
        case 100131:
            return (*env)->NewStringUTF(env, _first_h);
        case 100132:
            return (*env)->NewStringUTF(env, _first_s);
        case 100133:
            return (*env)->NewStringUTF(env, _del_h);
        default:
            break;
    }
    return (*env)->NewStringUTF(env, "");

}

JNIEXPORT jboolean JNICALL
Java_cn_EGGMaster_util_JniUtils_getConfBoolean(JNIEnv *env, jobject obj, jint type) {

    switch (type) {
        case ISNET:
            return (jboolean) _is_net;
        case ALLHTTPS:
            return (jboolean) _all_https;
        default:
            break;
    }
    return 0;
}