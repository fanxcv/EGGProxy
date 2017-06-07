#ifndef EGGPROXY_ENCRYPT_H
#define EGGPROXY_ENCRYPT_H

//#define SIGN_DEBUG

#include <jni.h>
#include "Core.h"

#define EC_TRUE (~1998)
#define EC_SUCCESS (encrypt_check == EC_TRUE)
extern int encrypt_check;

int ec_init(JNIEnv *env, jobject contextObject);

#endif //EGGPROXY_ENCRYPT_H
