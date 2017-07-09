#include "Core.h"

using namespace std;

extern string getHost(string &src);

extern int endWith(const char *src, const char *str);

extern int startWith(const char *src, const char *str);

extern void delHeader(string &src, string const &_ds);

extern void resFstLine(string &url, string &version);

extern void replaceAll(string &src, string const &find, string const &replace);

int init = 0;
string _del_h;

extern "C" {

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getConfString(JNIEnv *env, jobject obj, jint type) {

    if (EC_SUCCESS) {
        switch (type) {
            case KEY:
                return env->NewStringUTF(DEFAULTKEY);
            case URL:
                return env->NewStringUTF(DEFAULTURL);
            default:
                break;
        }
    }
    return env->NewStringUTF("");

}

JNIEXPORT jboolean JNICALL
Java_cn_EGGMaster_util_JniUtils_setVal(JNIEnv *env, jobject obj, jstring del) {
    if (!init) return 0;
    const char *c_del = env->GetStringUTFChars(del, NULL);
    _del_h = c_del;
    env->ReleaseStringUTFChars(del, c_del);
    return 1;
}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getHost(JNIEnv *env, jobject obj, jstring str) {

    const char *s = env->GetStringUTFChars(str, NULL);
    string ns = s;
    string host = getHost(ns);
    env->ReleaseStringUTFChars(str, s);
    return env->NewStringUTF(host.c_str());
}

void _uniComSupport(JNIEnv *env, char *urls, string &dhost, string &dport, string &ns) {

    char stime[32];

    time_t currentTime;
    time(&currentTime);
    sprintf(stime, "%li000", currentTime);

    jstring a = env->NewStringUTF("13072257727");
    jstring b = env->NewStringUTF(urls);
    jstring c = env->NewStringUTF("00000000000/1");
    jstring d = env->NewStringUTF(stime);
    jstring e = env->NewStringUTF(dhost.c_str());
    jstring f = env->NewStringUTF(dport.c_str());


    jclass c_utils = env->FindClass("cn/EGGMaster/util/Utils");
    jmethodID m_getKey = env->GetStaticMethodID(c_utils, "getKey",
                                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    jstring result = (jstring) env->CallStaticObjectMethod(c_utils, m_getKey, a, b, c, d, e, f);
    const char *c_result = env->GetStringUTFChars(result, NULL);

    replaceAll(ns, "[T]", stime);
    replaceAll(ns, "[K]", c_result);

    env->ReleaseStringUTFChars(result, c_result);
}

JNIEXPORT jobjectArray JNICALL
Java_cn_EGGMaster_util_JniUtils_getHttpHeader(JNIEnv *env, jobject obj, jstring header) {

    const char *tHeader = env->GetStringUTFChars(header, NULL);
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray x = env->NewObjectArray(5, stringClass, 0);
    string method, host, url, version, cHeader = tHeader;

    size_t n_a = cHeader.find("\r\n");
    if (n_a == string::npos) return NULL;
    url = cHeader.substr(0, n_a);
    cHeader.erase(0, n_a + 2);
    host = getHost(cHeader);

    if (startWith(url.c_str(), "GET")) {
        method = "GET";
        delHeader(cHeader, _del_h);
        resFstLine(url.erase(0, 4), version);
    } else if (startWith(url.c_str(), "POST")) {
        method = "POST";
        resFstLine(url.erase(0, 5), version);

        size_t pos = cHeader.find("\r\n\r\n");
        if (pos != string::npos) {
            string tmp = cHeader.substr(pos + 4, cHeader.length() - pos - 4);
            delHeader(cHeader.erase(pos + 4), _del_h);
            cHeader += tmp;
        } else {
            delHeader(cHeader, _del_h);
        }
    }
    env->ReleaseStringUTFChars(header, tHeader);
    env->SetObjectArrayElement(x, 0, env->NewStringUTF(url.c_str()));
    env->SetObjectArrayElement(x, 1, env->NewStringUTF(host.c_str()));
    env->SetObjectArrayElement(x, 2, env->NewStringUTF(method.c_str()));
    env->SetObjectArrayElement(x, 3, env->NewStringUTF(version.c_str()));
    env->SetObjectArrayElement(x, 4, env->NewStringUTF(cHeader.c_str()));
    return x;
}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_initCore(JNIEnv *env, jobject obj, jobject context) {
    jclass m_Context = env->GetObjectClass(context);
    jmethodID getPackageName = env->GetMethodID(m_Context, "getPackageName",
                                                "()Ljava/lang/String;");
    jmethodID getPackageManager = env->GetMethodID(m_Context, "getPackageManager",
                                                   "()Landroid/content/pm/PackageManager;");
    jobject o_getPackageManager = env->CallObjectMethod(context, getPackageManager);
    jclass m_getPackageManager = env->GetObjectClass(o_getPackageManager);

    jmethodID getPackageInfo = env->GetMethodID(m_getPackageManager, "getPackageInfo",
                                                "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jstring packageName = (jstring) (env->CallObjectMethod(context, getPackageName));

    jobject o_package = env->CallObjectMethod(o_getPackageManager, getPackageInfo, packageName, 0);
    jclass m_package = env->GetObjectClass(o_package);

    jfieldID i_version = env->GetFieldID(m_package, "versionName", "Ljava/lang/String;");
    jstring version = (jstring) env->GetObjectField(o_package, i_version);

    const char *tversion = env->GetStringUTFChars(version, NULL);
    if (strcmp(tversion, VERSION) != 0 || !EC_SUCCESS) {
        env->ReleaseStringUTFChars(version, tversion);
        return env->NewStringUTF("-1");
    }
    string cversion = tversion;
    cversion = "version=" + cversion;
    jstring jversion = env->NewStringUTF(cversion.c_str());
    jstring urlPath = env->NewStringUTF("getVersion");

    jclass c_utils = env->FindClass("cn/EGGMaster/util/Utils");
    jmethodID sendPost = env->GetStaticMethodID(c_utils, "sendPosts",
                                                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    jstring result = (jstring) env->CallStaticObjectMethod(c_utils, sendPost, urlPath, jversion);

    const char *tresult = env->GetStringUTFChars(result, NULL);
    if (*tresult) init = 1;
    env->ReleaseStringUTFChars(result, tresult);
    env->ReleaseStringUTFChars(version, tversion);

    return result;
}

JNIEXPORT jboolean JNICALL
Java_cn_EGGMaster_util_JniUtils_init(JNIEnv *env, jobject obj, jobject context) {
    return (jboolean) ec_init(env, context);
}
}