#include "Core.h"
#include <sstream>

using namespace std;

extern void loadConfTiny(const char *conf);

extern string getHost(string &src);

extern int endWith(const char *src, const char *str);

extern int startWith(const char *src, const char *str);

extern void delHeader(string &src, string const &_ds);

extern void resFstLine(string &url, string &version);

extern void replaceAll(string &src, string const &find, string const &replace);

int init = 0;
int _is_net, _all_https;
string _mode, _del_h;
string _port_h, _port_s;
string _host_h, _host_s;
string _first_h, _first_s;
int _key_h = 0, _key_s = 0;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_cn_EGGMaster_util_JniUtils_loadConf(JNIEnv *env, jobject obj, jstring conf, jint type) {
    if (!init) return 0;

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

    _is_net = _all_https = 0;
    if (_mode.find("net") == 0) _is_net = 1;
    else if (_mode.find("wap_https") == 0) _all_https = 1;

    _key_h = _key_s = 0;
    if (_first_h.find("[K]") != string::npos) _key_h = 1;
    if (_first_s.find("[K]") != string::npos) _key_s = 1;

    if (_first_h.length() > 1 && _first_s.length() > 1)
        return 1;
    else
        return 0;

}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getConfString(JNIEnv *env, jobject obj, jint type) {

    switch (type) {
        case KEY:
            return env->NewStringUTF(DEFAULTKEY);
        case URL:
            return env->NewStringUTF(DEFAULTURL);
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
    if (!init) return 1;

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

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getHost(JNIEnv *env, jobject obj, jstring str) {

    const char *s = env->GetStringUTFChars(str, NULL);
    string ns = s;
    string host = getHost(ns);
    env->ReleaseStringUTFChars(str, s);
    return env->NewStringUTF(host.c_str());
}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getCoonHeader(JNIEnv *env, jobject obj, jstring host,
                                              jstring port) {

    const char *tHost = env->GetStringUTFChars(host, NULL);

    string hosts = tHost;
    string ns = _first_s + "\r\n";

    if (hosts.find(':') == string::npos) {
        const char *tPort = env->GetStringUTFChars(port, NULL);
        hosts += ':';
        hosts += tPort;
        env->ReleaseStringUTFChars(port, tPort);
    }

    if (_key_s) {
        time_t t_time;
        string s_time;
        stringstream stream;

        time(&t_time);
        stream << t_time;
        stream >> s_time;
        s_time += "000";
        string urls = "https://";
        if (endWith(hosts.c_str(), ":443"))
            urls += hosts.substr(0, hosts.length() - 4) + "/";
        else
            urls += hosts + "/";
        //LOGI("CONNECT请求 : %s", urls.c_str());
        string dhost = hosts, dport = "";
        size_t find = hosts.find(':');
        if (find != string::npos) {
            dhost = hosts.substr(0, find);
            dport = hosts.substr(find + 1);
            if (dport.compare("443") == 0) {
                dport = "";
            }
        }

        jstring a = env->NewStringUTF("13072257727");
        jstring b = env->NewStringUTF(urls.c_str());
        jstring c = env->NewStringUTF("00000000000/1");
        jstring d = env->NewStringUTF(s_time.c_str());
        jstring e = env->NewStringUTF(dhost.c_str());
        jstring f = env->NewStringUTF(dport.c_str());


        jclass c_utils = env->FindClass("cn/EGGMaster/util/Utils");
        jmethodID m_getKey = env->GetStaticMethodID(c_utils, "getKey",
                                                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        jstring result = (jstring) env->CallStaticObjectMethod(c_utils, m_getKey, a, b, c, d, e, f);
        const char *c_result = env->GetStringUTFChars(result, NULL);

        replaceAll(ns, "[T]", s_time);
        replaceAll(ns, "[K]", c_result);

        env->ReleaseStringUTFChars(result, c_result);
    }

    replaceAll(ns, "[U]", "/");
    replaceAll(ns, "[H]", hosts);
    replaceAll(ns, "[M]", "CONNECT");
    replaceAll(ns, "[V]", "HTTP/1.1");

    //LOGI("CONNECT请求 : %s", ns.c_str());
    jstring newReq = env->NewStringUTF(ns.c_str());
    env->ReleaseStringUTFChars(host, tHost);
    return newReq;
}

JNIEXPORT jstring JNICALL
Java_cn_EGGMaster_util_JniUtils_getHttpHeader(JNIEnv *env, jobject obj, jstring header) {

    const char *tHeader = env->GetStringUTFChars(header, NULL);

    string method, host, url, version, cHeader = tHeader, ns = _first_h;

    size_t n_a = cHeader.find("\r\n");
    if (n_a != string::npos) {
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
    }

    if (_key_h) {
        time_t t_time;
        string s_time;
        stringstream stream;

        time(&t_time);
        stream << t_time;
        stream >> s_time;
        s_time += "000";
        string urls = "http://";
        if (endWith(host.c_str(), ":80"))
            urls += host.substr(0, host.length() - 3) + url;
        else
            urls += host + url;
        //LOGI("HTTP请求 : %s", urls.c_str());
        string dhost = host, dport = "";
        size_t find = host.find(':');
        if (find != string::npos) {
            dhost = host.substr(0, find);
            dport = host.substr(find + 1);
            if (dport.compare("80") == 0) {
                dport = "";
            }
        }

        jstring a = env->NewStringUTF("13072257727");
        jstring b = env->NewStringUTF(urls.c_str());
        jstring c = env->NewStringUTF("00000000000/1");
        jstring d = env->NewStringUTF(s_time.c_str());
        jstring e = env->NewStringUTF(dhost.c_str());
        jstring f = env->NewStringUTF(dport.c_str());

        jclass c_utils = env->FindClass("cn/EGGMaster/util/Utils");
        jmethodID m_getKey = env->GetStaticMethodID(c_utils, "getKey",
                                                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        jstring result = (jstring) env->CallStaticObjectMethod(c_utils, m_getKey, a, b, c, d, e, f);
        const char *c_result = env->GetStringUTFChars(result, NULL);

        replaceAll(ns, "[T]", s_time);
        replaceAll(ns, "[K]", c_result);

        env->ReleaseStringUTFChars(result, c_result);
    }

    replaceAll(ns, "[M]", method);
    replaceAll(ns, "[H]", host);
    replaceAll(ns, "[V]", version);
    replaceAll(ns, "[U]", url);
    ns += cHeader;
    //LOGI("HTTP请求 : %s", ns.c_str());
    jstring newReq = env->NewStringUTF(ns.c_str());
    env->ReleaseStringUTFChars(header, tHeader);
    return newReq;
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
    if (strcmp(tversion, VERSION) != 0) {
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
}