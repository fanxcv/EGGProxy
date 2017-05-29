#ifndef EGGPROXY_ENCRYPT_H
#define EGGPROXY_ENCRYPT_H

//#define SIGN_DEBUG

#include <jni.h>

#define EC_TRUE (~1998)
#define EC_SUCCESS (encrypt_check ^ EC_TRUE == 0)
extern int encrypt_check;

void ec_init(JNIEnv *env);

#endif //EGGPROXY_ENCRYPT_H
