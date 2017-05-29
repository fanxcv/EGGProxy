#include <string.h>
#include <stdlib.h>
#include "encrypt.h"
#include "Core.h"

const char* RELEASE_SIGN[] = {
/* KEY 1 */
"308202bb308201a3a00302010202045cd79e21300d06092a864886f70d01010b0500300d310b30090603550406130238363020170d3137303532373037353230305a180f33303136303932373037353230305a300d310b300906035504061302383630820122300d06092a864886f70d01010105000382010f003082010a0282010100997db6532b8b375dca13eabeecfc70b83c814d3ccca180c09f8610cc58549254e8a438554c73c1517a91648572a460702aaa3dee7ff83a0648dbb4b2d2c5f14527fb14146dc108ef942cb9baaf2c1d9c76813da6755a73ca424f59540ce37d42c85b7c661516b065ba0cecbc5cb06230f173de19e952b17afb57c48b81d471cc2ed4564e446071e44cb018a616e19bad47b6e0e5ef2c4c917ea5f774a74e318f3c5099153573cbee3eb3a8abff55edbfb99b264e7d9cd668d03d29802272317ab97fee29db8beb04ce07953d0019c1e791b9e6b074a199ff237613e97c6f2d7eaa6c64b5c658a9f2899783b2781dd2681160f746de682b1969a3f87679aafa0b0203010001a321301f301d0603551d0e0416041446697e3c23fd32b52208132a314ac8c3d8beedac300d06092a864886f70d01010b050003820101003d1327bd4e328ab458a6369b7bbd1cc56ac4f437a5b7d611ab6a2247d21432ea0b198229092dfc2996757d37d712564e4d7ccaf3882cae541abd9"
/* KEY 2 */
// TODO: add new key
};
const int RELEASE_SIGN_TOKEN[] = {
/* TOKEN 1 */       0x5CC1FF62
/* TOKEN 2 */       // TODO: add new token
};

JNIEXPORT jint JNICALL Java_cn_EGGMaster_util_JniUtils_ecInit(JNIEnv *env,
                      jobject thiz,jobject contextObject){
    // verify application
    jclass native_class = env->GetObjectClass(contextObject);
    jmethodID pm_id = env->GetMethodID(native_class, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject pm_obj = env->CallObjectMethod(contextObject, pm_id);
    jclass pm_clazz = env->GetObjectClass(pm_obj);

    jmethodID package_info_id = env->GetMethodID(pm_clazz, "getPackageInfo","(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jclass native_classs = env->GetObjectClass(contextObject);
    jmethodID mId = env->GetMethodID(native_classs, "getPackageName", "()Ljava/lang/String;");
    jstring pkg_str = static_cast<jstring>(env->CallObjectMethod(contextObject, mId));

    jobject pi_obj = env->CallObjectMethod(pm_obj, package_info_id, pkg_str, 64);

    jclass pi_clazz = env->GetObjectClass(pi_obj);

    jfieldID signatures_fieldId = env->GetFieldID(pi_clazz, "signatures", "[Landroid/content/pm/Signature;");
    jobject signatures_obj = env->GetObjectField(pi_obj, signatures_fieldId);
    jobjectArray signaturesArray = (jobjectArray)signatures_obj;
    jsize size = env->GetArrayLength(signaturesArray);
    jobject signature_obj = env->GetObjectArrayElement(signaturesArray, 0);
    jclass signature_clazz = env->GetObjectClass(signature_obj);
    jmethodID string_id = env->GetMethodID(signature_clazz, "toCharsString", "()Ljava/lang/String;");
    jstring str = static_cast<jstring>(env->CallObjectMethod(signature_obj, string_id));
    char *c_msg = (char*)env->GetStringUTFChars(str,0);

    int token = 0xFFFFFFFF;
    char* sign = c_msg;
    while(*sign!=0){
        token = (token&0xFFFFFF00) | ((*(sign++))&0xFF);
        token =
                ((token&0x000000FF)^((token&0xFF000000)>>24)) |
                ((token&0x0000FF00)^((token&0x000000FF)<<8)) |
                ((token&0x00FF0000)^((token&0x0000FF00)<<8)) |
                ((token&0xFF000000)^((token&0x00FF0000)<<8));
    }

    for(int t = 0; t < sizeof(RELEASE_SIGN)/sizeof(RELEASE_SIGN[0]) ; t++){
        // verify sign
        int len = (int)strlen(RELEASE_SIGN[t]);
        if(len > 512 && strncmp(c_msg, RELEASE_SIGN[t], (size_t) len) == 0){
            if(sizeof(RELEASE_SIGN_TOKEN)/sizeof(RELEASE_SIGN_TOKEN[0]) >= t){
                // verify sign token
                if(token == RELEASE_SIGN_TOKEN[t]){
                    encrypt_check = EC_TRUE;
                    break;
                }
            }
        }
    }
    #ifdef SIGN_DEBUG
        if(!EC_SUCCESS){
            encrypt_check = EC_TRUE;// skip
            LOGI("SIGN STRING\n%s",c_msg);
            LOGI("SIGN TOKEN\n0x%8X",token);
        }else{
            LOGE("Please Undefine SIGN_DEBUG in Core.cpp !!!");
            exit(0);
        }
    #endif
    return EC_SUCCESS;
}