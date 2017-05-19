#include "Core.h"

using namespace std;

extern void loadConfTiny(const char *conf);

extern string getHost(string &src);

extern int startWith(const char *src, const char *str);

extern void delHeader(string &src, string const &_ds);

extern void resFstLine(string &url, string &version);

extern void replaceAll(string &src, string const &find, string const &replace);

int _is_net, _all_https;
string _mode, _del_h;
string _port_h, _port_s;
string _host_h, _host_s;
string _first_h, _first_s;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_cn_EGGMaster_util_JniUtils_loadConf(JNIEnv *env, jobject obj, jstring conf, jint type) {

    const char *cConf = env->GetStringUTFChars(conf, NULL);

    switch (type) {
        case 0:
            loadConfTiny(cConf);
            break;
        case 1:
            //loadConfFmns(cConf);
            break;
        default:
            env->ReleaseStringUTFChars(conf, cConf);
            return 0;
    }
    env->ReleaseStringUTFChars(conf, cConf);

    if (_mode.find("net") == 0) {
        _is_net = 1;
    } else if (_mode.find("wap_https") == 0) {
        _all_https = 1;
    }

    if (_first_h.length() > 1 && _first_s.length() > 1)
        return 1;
    else
        return 0;

}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getConfString(JNIEnv *env, jobject obj, jint type) {

    switch (type) {
        case HTTP_IP:
            return env->NewStringUTF(_host_h.c_str());
        case HTTP_PORT:
            return env->NewStringUTF(_port_h.c_str());
        case HTTPS_IP:
            return env->NewStringUTF(_host_s.c_str());
        case HTTPS_PORT:
            return env->NewStringUTF(_port_s.c_str());
        default:
            break;
    }
    return env->NewStringUTF("");

}

JNIEXPORT jboolean JNICALL
Java_cn_EGGMaster_util_JniUtils_getConfBoolean(JNIEnv *env, jobject obj, jint type) {

    switch (type) {
        case ISNET:
            return (jboolean)
                    _is_net;
        case ALLHTTPS:
            return (jboolean)
                    _all_https;
        default:
            break;
    }
    return 0;
}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getHost(JNIEnv *env, jobject obj, jstring str) {

    const char *s = env->GetStringUTFChars(str, NULL);
    string ns = s;
    string host = getHost(ns);
    env->ReleaseStringUTFChars(str, s);
    return env->NewStringUTF(host.c_str());
}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getCoonHeader(JNIEnv *env, jobject obj, jstring host) {
    const char *tHost = env->GetStringUTFChars(host, NULL);

    string ns = _first_s + "\r\n";

    replaceAll(ns, "[U]", "/");
    replaceAll(ns, "[H]", tHost);
    replaceAll(ns, "[M]", "CONNECT");
    replaceAll(ns, "[V]", "HTTP/1.1");

    jstring newReq = env->NewStringUTF(ns.c_str());
    env->ReleaseStringUTFChars(host, tHost);
    return newReq;
}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getHttpHeader(JNIEnv *env, jobject obj, jstring header) {

    const char *tHeader = env->GetStringUTFChars(header, NULL);

    string method, host, url, version, cHeader = tHeader, ns = _first_h;

    size_t n_a = cHeader.find("\r\n");
    if (n_a < cHeader.length()) {
        url = cHeader.substr(0, n_a);
        if (startWith(url.c_str(), "GET")) {
            method = "GET";
            resFstLine(url.erase(0, 4), version);
        } else if (startWith(url.c_str(), "POST")) {
            method = "POST";
            resFstLine(url.erase(0, 5), version);
        }
        cHeader.erase(0, n_a + 2);
        host = getHost(cHeader);
        delHeader(cHeader, _del_h);
    }
    replaceAll(ns, "[M]", method);
    replaceAll(ns, "[H]", host);
    replaceAll(ns, "[V]", version);
    replaceAll(ns, "[U]", url);
    ns += cHeader;
    jstring newReq = env->NewStringUTF(ns.c_str());
    env->ReleaseStringUTFChars(header, tHeader);
    return newReq;
}
}