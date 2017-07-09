#include <string.h>
#include "encrypt.h"

int encrypt_check;

const char *RELEASE_SIGN[] = {
/* KEY 1 */
        "308202bb308201a3a003020102020472f658fe300d06092a864886f70d01010b0500300e310c300a0603550403130346616e301e170d3137303431373030343630335a170d3432303431313030343630335a300e310c300a0603550403130346616e30820122300d06092a864886f70d01010105000382010f003082010a0282010100a063ed0997695dc270ff316159e95060757a4a649bfe3f2a425e5e95b0834048732e794998ba76ef140a6bb846e29b55d5ab5795970132e107f3c51a82f94055ef940fcbb354ca1f07296b4d1be3bb142931a04e78ecbef0439d29a3de5f4b0def5751d37604dbb2e1670474c16d6280d2d8af96256f85014a692bb52311dce9f6815bf2bf15238766cf0d5794b63d3c2a4553e3232ea79e2120d28a563fb735a15a081bc64629eeca395db002e5ef1d0bd25fc40f63a9dba7d7e3b6dedb60fa862c60a547abbe0e81472d808ba1bce8029766a003987e439c4e8d6311c6c8219ca5c5de27d208f72f8d8f1300de39481c3925f78009b0b5bab9286c1d3ed6450203010001a321301f301d0603551d0e0416041459d8a4f0b44f43b5060a5b89bf494e71345360c4300d06092a864886f70d01010b0500038201010077b77f6fb8dd8afa661110514607214568ec6133a7deab8a489db360bdb16553eb4ea52e1cc6b53c3c87ab222095832924115ad38cff8c1ccef50",
/* KEY 2 */
        "308201dd30820146020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b3009060355040613025553301e170d3137303632343038303635395a170d3437303631373038303635395a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330819f300d06092a864886f70d010101050003818d0030818902818100afaf0b3a82a111e2911b41fcaecb16ba038e5b6c51b1adb770ed0273f7a707a2dd6fe9fcbaa77b7cfde21edecfc17d1f358ef638068b3392f8a44d1aeaa3d3704cd0a8d987ad747348522b936a3d4458f705ba56e6adb66e93d0342b6f04233158dd40e8ffa4b516ad1d2e6dd54b94740b32b7f4e07b2b7ed1940f1307d9c0570203010001300d06092a864886f70d010105050003818100310d819b113c5951c12b7bad07fb3db5afec23af9ddce39c6e278f141e8fc08b0abe5d8445cf727aa39beb28118aaddb4935bd564656140ce4f8188e8aada9c79e190d6544eaee9d7088d5bc304ae551d93a6a818d4885444bb7422618eb13c0fe419132294051444ba480137ce55d8ce1b238ff7d94996017135d0ad823bb9d"
};
const int RELEASE_SIGN_TOKEN[] = {
/* TOKEN 1 */       0x5FCAA431,
/* TOKEN 2 */       0x5993F63C
};

int ec_init(JNIEnv *env, jobject contextObject) {
    // verify application
    jclass native_class = env->GetObjectClass(contextObject);
    jmethodID pm_id = env->GetMethodID(native_class, "getPackageManager",
                                       "()Landroid/content/pm/PackageManager;");
    jobject pm_obj = env->CallObjectMethod(contextObject, pm_id);
    jclass pm_clazz = env->GetObjectClass(pm_obj);

    jmethodID package_info_id = env->GetMethodID(pm_clazz, "getPackageInfo",
                                                 "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jclass native_classs = env->GetObjectClass(contextObject);
    jmethodID mId = env->GetMethodID(native_classs, "getPackageName", "()Ljava/lang/String;");
    jstring pkg_str = static_cast<jstring>(env->CallObjectMethod(contextObject, mId));

    jobject pi_obj = env->CallObjectMethod(pm_obj, package_info_id, pkg_str, 64);

    jclass pi_clazz = env->GetObjectClass(pi_obj);

    jfieldID signatures_fieldId = env->GetFieldID(pi_clazz, "signatures",
                                                  "[Landroid/content/pm/Signature;");
    jobject signatures_obj = env->GetObjectField(pi_obj, signatures_fieldId);
    jobjectArray signaturesArray = (jobjectArray) signatures_obj;
    jsize size = env->GetArrayLength(signaturesArray);
    jobject signature_obj = env->GetObjectArrayElement(signaturesArray, 0);
    jclass signature_clazz = env->GetObjectClass(signature_obj);
    jmethodID string_id = env->GetMethodID(signature_clazz, "toCharsString",
                                           "()Ljava/lang/String;");
    jstring str = static_cast<jstring>(env->CallObjectMethod(signature_obj, string_id));
    char *c_msg = (char *) env->GetStringUTFChars(str, 0);

    int token = 0xFFFFFFFF;
    char *sign = c_msg;
    while (*sign != 0) {
        token = (token & 0xFFFFFF00) | ((*(sign++)) & 0xFF);
        token =
                ((token & 0x000000FF) ^ ((token & 0xFF000000) >> 24)) |
                ((token & 0x0000FF00) ^ ((token & 0x000000FF) << 8)) |
                ((token & 0x00FF0000) ^ ((token & 0x0000FF00) << 8)) |
                ((token & 0xFF000000) ^ ((token & 0x00FF0000) << 8));
    }

    for (int t = 0; t < sizeof(RELEASE_SIGN) / sizeof(RELEASE_SIGN[0]); t++) {
        // verify sign
        int len = (int) strlen(RELEASE_SIGN[t]);
        if (len > 512 && strncmp(c_msg, RELEASE_SIGN[t], (size_t) len) == 0) {
            if (sizeof(RELEASE_SIGN_TOKEN) / sizeof(RELEASE_SIGN_TOKEN[0]) >= t) {
                // verify sign token
                if (token == RELEASE_SIGN_TOKEN[t]) {
                    encrypt_check = EC_TRUE;
                    break;
                }
            }
        }
    }
#ifdef SIGN_DEBUG
    if (!EC_SUCCESS) {
        encrypt_check = EC_TRUE;// skip
        LOGI("SIGN STRING\n%s", c_msg);
        LOGI("SIGN TOKEN\n0x%8X", token);
    } else {
        LOGE("Please Undefine SIGN_DEBUG in Core.cpp !!!");
        exit(0);
    }
#endif
    return EC_SUCCESS;
}