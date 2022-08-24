#include <jni.h>
#include "URLSignature.h"
#include "logging.h"

typedef char* (*GET_STR)();
typedef bool (*VALIDATOR)(char*);

static char *IL_PKG_PREFIX = NULL;
static char *IL_FINGERPRINT = NULL;
static char *IL_STAGE_FINGERPRINT = NULL;
static char *CRYPTOWALLET_PKG_PREFIX = NULL;
static char *CUBE_PKG_PREFIX = NULL;
static char *CUBE_FINGERPRINT = NULL;
static char *CMTWALLET_PKG_PREFIX = NULL;
static char *MARS_PKG_PREFIX = NULL;
static char *MARS_FINGERPRINT = NULL;

static jclass CLZ_CONTEXT;
static jclass CLZ_PKG_MGR;
static jclass CLZ_PKG_INFO;
static jclass CLZ_SIGNATURE;
static jmethodID MID_GET_PKG_NAME;
static jmethodID MID_GET_PKG_MGR;
static jmethodID MID_GET_PKG_INFO;
static jmethodID MID_TO_BYTE_ARRAY;
static jfieldID FID_GET_SIGNATURES;
static jfieldID FID_SIGNATURES;

/*
 * Make STL string from java byte array, to fix Unicode issues on Samsung devices
 */
inline char* getStringFromByteArray(JNIEnv *&env, jbyteArray &bytes) {
    const char *ptrBytes = (const char*) env->GetByteArrayElements(bytes, NULL);
    const int len = env->GetArrayLength(bytes);

    char *str = (char*) malloc((len + 1) * sizeof(char));  // 1 for '\0'
    strncpy(str, ptrBytes, len);
    str[len] = '\0';

    env->ReleaseByteArrayElements(bytes, (jbyte*) ptrBytes, JNI_ABORT);
    return str;
}

// byte数组按char*处理，如果其中包含0会被截断，因此此处按uint8处理
inline uint8_t *extractBytes(JNIEnv *&env, jbyteArray &bytes) {
    jbyte *ptrBytes = env->GetByteArrayElements(bytes, NULL);
    const int len = env->GetArrayLength(bytes);

    uint8_t *data = (uint8_t *) malloc(len * sizeof(uint8_t));
    memcpy(data, ptrBytes, len);
    env->ReleaseByteArrayElements(bytes, (jbyte *) ptrBytes, JNI_ABORT);
    return data;
}

/*
 * release the given UTF string, if still valid
 */
inline void releaseUTFChars(JNIEnv *&env, jstring &jstr, const char *&cstr) {
    if (cstr != NULL) {
        env->ReleaseStringUTFChars(jstr, cstr);
    }
}

inline char *getIlPkgPrefix() {
    if (IL_PKG_PREFIX == NULL) {
        int len = 21;
        //com.example.instalive
        uint8_t ptrBytes[] = {
                99, 111, 109, 46, 101, 120, 97, 109, 112, 108, 101,
                46, 105, 110, 115, 116, 97, 108, 105, 118, 101,
        };
        IL_PKG_PREFIX = (char *) malloc(len + 1);
        memcpy(IL_PKG_PREFIX, ptrBytes, len);
        IL_PKG_PREFIX[len] = '\0';
    }

    return IL_PKG_PREFIX;
}

inline char *getFmFingerprint() {
    if (IL_FINGERPRINT == NULL) {
        uint8_t ptrBytes[] = {
                0xDB, 0xB9, 0x25, 0x9D, 0x66, 0xBD, 0x1E, 0xEA, 0xF9, 0xD8,
                0xEE, 0x7A, 0x25, 0x48, 0xCC, 0x61, 0xAA, 0x60, 0xB8, 0xB5,
        };
        IL_FINGERPRINT = (char *) malloc(41);
        for (int i = 0; i < 20; ++i) {
            sprintf(&IL_FINGERPRINT[i * 2], "%02X", ptrBytes[i]);
        }
    }

    return IL_FINGERPRINT;
}

inline char *getFmStageFingerprint() {
    if (IL_STAGE_FINGERPRINT == NULL) {
        uint8_t ptrBytes[] = {
                0x06, 0xE9, 0x13, 0xF5, 0xB9, 0xDA, 0xD0, 0x33, 0xD7, 0xA3,
                0x3B, 0x23, 0xF2, 0xEB, 0x9F, 0x27, 0x7A, 0xC8, 0xEA, 0xBF,
        };
        IL_STAGE_FINGERPRINT = (char *) malloc(41);
        for (int i = 0; i < 20; ++i) {
            sprintf(&IL_STAGE_FINGERPRINT[i * 2], "%02X", ptrBytes[i]);
        }
    }

    return IL_STAGE_FINGERPRINT;
}

inline char *getCryptoWalletPkgPrefix() {
    if (CRYPTOWALLET_PKG_PREFIX == NULL) {
        // bytes of 'com.noomiwallet.noomiandroid'
        int len = 28;
        uint8_t ptrBytes[] = {
                99, 111, 109, 46, 110, 111, 111, 109, 105, 119, 97,
                108, 108, 101, 116, 46, 110, 111, 111, 109, 105,
                97, 110, 100, 114, 111, 105, 100,
        };
        CRYPTOWALLET_PKG_PREFIX = (char *) malloc(len + 1);
        memcpy(CRYPTOWALLET_PKG_PREFIX, ptrBytes, len);
        CRYPTOWALLET_PKG_PREFIX[len] = '\0';
    }

    return CRYPTOWALLET_PKG_PREFIX;
}

inline char *getCubePkgPrefix() {
    if (CUBE_PKG_PREFIX == NULL) {
        // bytes of 'io.cybermiles.cybercube'
        int len = 23;
        uint8_t ptrBytes[] = {
                105,111,46,99,121,98,101,114,109,105,108,101,
                115,46,99,121,98,101,114,99,117,98,101
        };
        CUBE_PKG_PREFIX = (char *) malloc(len + 1);
        memcpy(CUBE_PKG_PREFIX, ptrBytes, len);
        CUBE_PKG_PREFIX[len] = '\0';
    }

    return CUBE_PKG_PREFIX;
}

inline char *getCubeFingerprint() {
    if (CUBE_FINGERPRINT == NULL) {
        uint8_t ptrBytes[] = {
                0x41, 0x79, 0x1C, 0x9B, 0x8F, 0xAF, 0x15, 0xE1, 0xAC, 0xD5,
                0xAA, 0xF5, 0x92, 0x10, 0xFD, 0x42, 0x46, 0x7D, 0x82, 0x77,
        };
        CUBE_FINGERPRINT = (char *) malloc(41);
        for (int i = 0; i < 20; ++i) {
            sprintf(&CUBE_FINGERPRINT[i * 2], "%02X", ptrBytes[i]);
        }
    }

    return CUBE_FINGERPRINT;
}

inline char *getCmtWalletPkgPrefix() {
    if (CMTWALLET_PKG_PREFIX == NULL) {
        // bytes of 'io.cybermiles.cmtwallet'
        int len = 23;
        uint8_t ptrBytes[] = {
                105,111,46,99,121,98,101,114,109,105,108,101,
                115,46,99,109,116,119,97,108,108,101,116
        };
        CMTWALLET_PKG_PREFIX = (char *) malloc(len + 1);
        memcpy(CMTWALLET_PKG_PREFIX, ptrBytes, len);
        CMTWALLET_PKG_PREFIX[len] = '\0';
    }

    return CMTWALLET_PKG_PREFIX;
}

inline char *getMarsPkgPrefix() {
    if (MARS_PKG_PREFIX == NULL) {
        // bytes of 'com.newsclapper.video'
        int len = 18;
        uint8_t ptrBytes[] = {
                99,111,109,46,110,101,119,115,99,108,97,112,112,101,114,46,118,105,100,101,111
        };
        MARS_PKG_PREFIX = (char *) malloc(len + 1);
        memcpy(MARS_PKG_PREFIX, ptrBytes, len);
        MARS_PKG_PREFIX[len] = '\0';
    }

    return MARS_PKG_PREFIX;
}

inline char *getVenusPkgPrefix() {
    if (MARS_PKG_PREFIX == NULL) {
        // bytes of 'com.fambase.venus'
        int len = 17;
        uint8_t ptrBytes[] = {
                99,111,109,46,102,97,109,98,97,115,101,46,118,101,110,117,115
        };
        MARS_PKG_PREFIX = (char *) malloc(len + 1);
        memcpy(MARS_PKG_PREFIX, ptrBytes, len);
        MARS_PKG_PREFIX[len] = '\0';
    }
    return MARS_PKG_PREFIX;
}

inline char *getMarsFingerprint() {
    if (MARS_FINGERPRINT == NULL) {
        uint8_t ptrBytes[] = {
                0xC7, 0xA6, 0x11, 0xE8, 0x59, 0x1D, 0x0C, 0x60, 0x64, 0x5D,
                0x25, 0x07, 0x03, 0x45, 0x88, 0xB8, 0x36, 0xBD, 0xE8, 0x2A,
        };
        MARS_FINGERPRINT = (char *) malloc(41);
        for (int i = 0; i < 20; ++i) {
            sprintf(&MARS_FINGERPRINT[i * 2], "%02X", ptrBytes[i]);
        }
    }

    return MARS_FINGERPRINT;
}

inline char *getMarsPlayFingerprint() {
    if (MARS_FINGERPRINT == NULL) {
        uint8_t ptrBytes[] = {
                0x3B, 0xCC, 0xAF, 0xD1, 0xC8, 0x7C, 0x92, 0x8C, 0x7B, 0xE3,
                0x65, 0xD5, 0xED, 0x9A, 0xF8, 0xD4, 0xC7, 0x20, 0xE1, 0xFA,
        };
        MARS_FINGERPRINT = (char *) malloc(41);
        for (int i = 0; i < 20; ++i) {
            sprintf(&MARS_FINGERPRINT[i * 2], "%02X", ptrBytes[i]);
        }
    }

    return MARS_FINGERPRINT;
}

inline char *getMarsAmazonFingerprint() {
    if (MARS_FINGERPRINT == NULL) {
        //63:2B:A0:2E:73:B2:86:A5:4E:9B:A6:95:1F:D7:B9:EC:FB:95:90:81
        uint8_t ptrBytes[] = {
                0x63, 0x2B, 0xA0, 0x2E, 0x73, 0xB2, 0x86, 0xA5, 0x4E, 0x9B,
                0xA6, 0x95, 0x1F, 0xD7, 0xB9, 0xEC, 0xFB, 0x95, 0x90, 0x81,
        };
        MARS_FINGERPRINT = (char *) malloc(41);
        for (int i = 0; i < 20; ++i) {
            sprintf(&MARS_FINGERPRINT[i * 2], "%02X", ptrBytes[i]);
        }
    }

    return MARS_FINGERPRINT;
}

inline char *getVenusPlayFingerprint() {
    if (MARS_FINGERPRINT == NULL) {
        //71:D1:B5:58:25:D2:FF:5C:AE:1F:E7:B7:BA:34:56:40:ED:B1:C6:28
        uint8_t ptrBytes[] = {
                0x71, 0xD1, 0xB5, 0x58, 0x25, 0xD2, 0xFF, 0x5C, 0xAE, 0x1F,
                0xE7, 0xB7, 0xBA, 0x34, 0x56, 0x40, 0xED, 0xB1, 0xC6, 0x28,
        };
        MARS_FINGERPRINT = (char *) malloc(41);
        for (int i = 0; i < 20; ++i) {
            sprintf(&MARS_FINGERPRINT[i * 2], "%02X", ptrBytes[i]);
        }
    }

    return MARS_FINGERPRINT;
}

inline bool validateFingerprint(char *hash) {
    char *ptrFp1 = getFmFingerprint();
    char *ptrFp2 = getFmStageFingerprint();
    return 0 == strcmp(hash, ptrFp1) || 0 == strcmp(hash, ptrFp2);
}

inline bool validateCubeFingerprint(char *hash) {
    char *ptrFp1 = getCubeFingerprint();
    return 0 == strcmp(hash, ptrFp1);
}

inline bool validateMarsFingerprint(char *hash) {
    char *ptrFp1 = getMarsFingerprint();
    return 0 == strcmp(hash, ptrFp1);
}

inline bool validateMarsPlayFingerprint(char *hash) {
    char *ptrFp1 = getMarsPlayFingerprint();
    return 0 == strcmp(hash, ptrFp1);
}

inline bool validateMarsAmazonFingerprint(char *hash) {
    char *ptrFp1 = getMarsAmazonFingerprint();
    return 0 == strcmp(hash, ptrFp1);
}

inline bool validateVenusPlayFingerprint(char *hash) {
    char *ptrFp1 = getVenusPlayFingerprint();
    return 0 == strcmp(hash, ptrFp1);
}

inline bool checkAuthorized(JNIEnv *&env,
                            jobject &context,
                            GET_STR fpGetPkgName,
                            VALIDATOR fpValidateFingerprint) {
#ifdef FM_URL_SIGN_NO_VERIFICATION
    // 测试时跳过package/fingerprint校验
    return true;
#else
    jobject pkgMgr = env->CallObjectMethod(context, MID_GET_PKG_MGR);
    jstring pkgName = (jstring) env->CallObjectMethod(context, MID_GET_PKG_NAME);

    const char *ptrPkgName = env->GetStringUTFChars(pkgName, 0);
    const char *ptrFmPkgPrefix = fpGetPkgName();
    LOGD("signUrl: %s, prefix: %s", ptrPkgName, ptrFmPkgPrefix);
    if (0 != strncmp(ptrPkgName, ptrFmPkgPrefix, strlen(ptrFmPkgPrefix))) {
        LOGD("signUrl: package name not match: %s, prefix: %s", ptrPkgName, ptrFmPkgPrefix);
        env->ReleaseStringUTFChars(pkgName, ptrPkgName);
        return false;
    }
    env->ReleaseStringUTFChars(pkgName, ptrPkgName);

    jint flagGetSignatures = env->GetStaticIntField(CLZ_PKG_MGR, FID_GET_SIGNATURES);
    jobject pkgInfo = env->CallObjectMethod(pkgMgr, MID_GET_PKG_INFO, pkgName, flagGetSignatures);
    jobjectArray signatures = (jobjectArray) env->GetObjectField(pkgInfo, FID_SIGNATURES);
    jsize cnt = env->GetArrayLength(signatures);
    if (cnt <= 0) {
        LOGD("signUrl: no signatures found");
        return false;
    }

    for (int i = 0; i < cnt; ++i) {
        jobject signature = env->GetObjectArrayElement(signatures, i);
        jbyteArray bytes = (jbyteArray) env->CallObjectMethod(signature, MID_TO_BYTE_ARRAY);
        const int len = env->GetArrayLength(bytes);
        const uint8_t *ptrBytes = extractBytes(env, bytes);

        char *fingerprint = sha1Digest(ptrBytes, len);
        LOGD("signUrl: package fingerprint: %d %s", len, fingerprint);

        if (!fpValidateFingerprint(fingerprint)) {
            delete ptrBytes;
            free(fingerprint);
            return false;
        }

        delete ptrBytes;
        free(fingerprint);
    }

    return true;
#endif
}

/*
 * convert java list object to a STL list of parameters
 */
t_params_ptr toParamList(JNIEnv *&env, jobjectArray &paramNames, jobjectArray &paramValues) {
    t_params_ptr ptrParams = new t_params;
    jsize keyCnt = env->GetArrayLength(paramNames);
    jsize valueCnt = env->GetArrayLength(paramValues);
    jsize paramCnt = min(keyCnt, valueCnt);

    for (int i = 0; i < paramCnt; ++i) {
        jstring k = (jstring) env->GetObjectArrayElement(paramNames, i);
        jbyteArray valueBytes = (jbyteArray) env->GetObjectArrayElement(paramValues, i);

        const char *ptrKey = env->GetStringUTFChars(k, 0);
        const char *ptrValue = getStringFromByteArray(env, valueBytes);

        if (ptrKey != NULL && ptrValue != NULL) {
            string *ptrEncodedKey = encodeUrl(ptrKey);
            string *ptrEncodedValue = encodeUrl(ptrValue);
            // LOGW("%s: %s", ptrKey, ptrEncodedValue->c_str());

            string *ptrKv = new string(*ptrEncodedKey);
            ptrKv->push_back('=');
            ptrKv->append(*ptrEncodedValue);
            ptrParams->push_back(ptrKv);

            delete ptrEncodedKey;
            delete ptrEncodedValue;
        }

        releaseUTFChars(env, k, ptrKey);
        delete ptrValue;
    }

    return ptrParams;
}

jstring signUrl(JNIEnv *&env,
                jclass &type,
                jobject &context,
                jstring &method,
                jstring &url,
                string *&ptrEncodedData,
                GET_STR fpGetKey,
                GET_STR fpGetSecret,
                GET_STR fpGetPkgName,
                VALIDATOR fpValidateFingerprint) {
    if (fpGetPkgName != NULL && fpValidateFingerprint != NULL &&
        !checkAuthorized(env, context, fpGetPkgName, fpValidateFingerprint)) {
//        LOGW("signUrl: not authorized, returning empty signature");
        return env->NewStringUTF("");
    }

    const char *ptrMethod = env->GetStringUTFChars(method, 0);
    const char *ptrUrl = env->GetStringUTFChars(url, 0);
    const char *secret = fpGetSecret();
    const char *key = fpGetKey();

    if (ptrMethod == NULL || ptrUrl == NULL) {
        releaseUTFChars(env, method, ptrMethod);
        releaseUTFChars(env, url, ptrUrl);
//        LOGW("signUrl: method or url is invalid, returning empty signature");
        return env->NewStringUTF("");
    }

    string *ptrEncodedUrl = encodeUrl(ptrUrl);

    // build the message and digest it
    char *msg = (char*) malloc(strlen(ptrMethod) +
                                       ptrEncodedUrl->length() +
                                       ptrEncodedData->length() +
                                       strlen(secret) + 3);  // 2 '&' + null
    strcpy(msg, ptrMethod);
    strcat(msg, "&");
    strcat(msg, ptrEncodedUrl->c_str());
    strcat(msg, "&");
    strcat(msg, ptrEncodedData->c_str());
    strcat(msg, secret);

    char *signature = sign(msg, key);
    jstring sig = env->NewStringUTF(signature);

    // release memory
    delete ptrEncodedUrl;
    free(msg);
    free(signature);
    releaseUTFChars(env, method, ptrMethod);
    releaseUTFChars(env, url, ptrUrl);

    return sig;
}

jstring signUrl(JNIEnv *&env,
                jclass &type,
                jobject &context,
                jstring &method,
                jstring &url,
                jobjectArray &paramNames,
                jobjectArray &paramValues,
                GET_STR fpGetKey,
                GET_STR fpGetSecret,
                GET_STR fpGetPkgName,
                VALIDATOR fpValidateFingerprint) {
    t_params *ptrParams = toParamList(env, paramNames, paramValues);
    string *ptrStrParams = encodeParams(ptrParams);

    jstring sig = signUrl(env, type, context, method, url, ptrStrParams, fpGetKey, fpGetSecret, fpGetPkgName, fpValidateFingerprint);

    // release memory
    for (t_params_itr itr = ptrParams->begin(); itr != ptrParams->end(); ++itr) {
        delete *itr;
    }
    delete ptrParams;
    delete ptrStrParams;
    return sig;
}

extern "C" {

/*
 * Implementation of UrlSignature#signUrl
 * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/design.html#wp615">Resolving Native Method Names</a>
 */
JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signUrl(JNIEnv *env,
                                                       jclass type,
                                                       jobject context,
                                                       jstring method,
                                                       jstring url,
                                                       jobjectArray paramNames,
                                                       jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
        getUrlSignatureKey, getEncodedUrlSignatureKey, getIlPkgPrefix, validateFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signMarsUrl(JNIEnv *env,
                                                       jclass type,
                                                       jobject context,
                                                       jstring method,
                                                       jstring url,
                                                       jobjectArray paramNames,
                                                       jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getMarsPkgPrefix, validateMarsFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signMarsPlayUrl(JNIEnv *env,
                                                           jclass type,
                                                           jobject context,
                                                           jstring method,
                                                           jstring url,
                                                           jobjectArray paramNames,
                                                           jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getMarsPkgPrefix, validateMarsPlayFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signMarsAmazonUrl(JNIEnv *env,
                                                               jclass type,
                                                               jobject context,
                                                               jstring method,
                                                               jstring url,
                                                               jobjectArray paramNames,
                                                               jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getMarsPkgPrefix, validateMarsAmazonFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signVenusUrl(JNIEnv *env,
                                                            jclass type,
                                                            jobject context,
                                                            jstring method,
                                                            jstring url,
                                                            jobjectArray paramNames,
                                                            jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getVenusPkgPrefix, validateMarsFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signVenusPlayUrl(JNIEnv *env,
                                                            jclass type,
                                                            jobject context,
                                                            jstring method,
                                                            jstring url,
                                                            jobjectArray paramNames,
                                                            jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getVenusPkgPrefix, validateVenusPlayFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signWebUrl(JNIEnv *env,
                                                          jclass type,
                                                          jobject context,
                                                          jstring method,
                                                          jstring url,
                                                          jobjectArray paramNames,
                                                          jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
        getWebUrlSignatureKey, getEncodedWebUrlSignatureKey, getIlPkgPrefix, validateFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signMarsWebUrl(JNIEnv *env,
                                                          jclass type,
                                                          jobject context,
                                                          jstring method,
                                                          jstring url,
                                                          jobjectArray paramNames,
                                                          jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getMarsPkgPrefix, validateMarsFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signVenusWebUrl(JNIEnv *env,
                                                            jclass type,
                                                            jobject context,
                                                            jstring method,
                                                            jstring url,
                                                            jobjectArray paramNames,
                                                            jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getVenusPkgPrefix, validateMarsFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signMarsWebPlayUrl(JNIEnv *env,
                                                              jclass type,
                                                              jobject context,
                                                              jstring method,
                                                              jstring url,
                                                              jobjectArray paramNames,
                                                              jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
                   getMarsUrlSignatureKey, getMarsEncodedUrlSignatureKey, getMarsPkgPrefix, validateMarsPlayFingerprint);
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signCollectUrl(JNIEnv *env,
                                                              jclass type,
                                                              jobject context,
                                                              jbyteArray param) {
    if (!checkAuthorized(env, context, getIlPkgPrefix, validateFingerprint)) {
       return env->NewStringUTF("");
    }
    const char *data = getStringFromByteArray(env, param);
    const char *secret = getUrlCollectSignatureKey();
    jstring sig = env->NewStringUTF(sign(data, secret));

    delete data;

    return sig;
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signCubeUrl(JNIEnv *env,
                                                           jclass type,
                                                           jobject context,
                                                           jstring method,
                                                           jstring url,
                                                           jbyteArray body) {
    const char *ptrBody = getStringFromByteArray(env, body);
    string *ptrEncodedData = encodeUrl(ptrBody);
    jstring sig = signUrl(env, type, context, method, url, ptrEncodedData,
        getCubeSignatureKey, getEncodedCubeSignatureKey, getCubePkgPrefix, validateCubeFingerprint);
    // release memory
    delete ptrBody;
    delete ptrEncodedData;
    return sig;
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signCmtWalletUrl(JNIEnv *env,
                                                           jclass type,
                                                           jobject context,
                                                           jstring method,
                                                           jstring url,
                                                           jbyteArray body) {
    const char *ptrBody = getStringFromByteArray(env, body);
    string *ptrEncodedData = encodeUrl(ptrBody);
    jstring sig = signUrl(env, type, context, method, url, ptrEncodedData,
        getCmtWalletSignatureKey, getEncodedCmtWalletSignatureKey, getCmtWalletPkgPrefix, validateFingerprint);
    // release memory
    delete ptrBody;
    delete ptrEncodedData;
    return sig;
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signCryptoWalletBody(JNIEnv *env,
                                                                    jclass type,
                                                                    jobject context,
                                                                    jstring method,
                                                                    jstring url,
                                                                    jbyteArray body) {
    const char *ptrBody = getStringFromByteArray(env, body);
    string *ptrEncodedData = encodeUrl(ptrBody);
    jstring sig = signUrl(env, type, context, method, url, ptrEncodedData,
        getCryptoWalletUrlSignatureKey, getEncodedCryptoWalletUrlSignatureKey,
        getCryptoWalletPkgPrefix, validateFingerprint); // 目前fingerprint与5miles相同
    // release memory
    delete ptrBody;
    delete ptrEncodedData;
    return sig;
}

JNIEXPORT jstring JNICALL
Java_com_venus_framework_rest_UrlSignature_signCryptoWalletUrl(JNIEnv *env,
                                                                   jclass type,
                                                                   jobject context,
                                                                   jstring method,
                                                                   jstring url,
                                                                   jobjectArray paramNames,
                                                                   jobjectArray paramValues) {
    return signUrl(env, type, context, method, url, paramNames, paramValues,
        getCryptoWalletUrlSignatureKey, getEncodedCryptoWalletUrlSignatureKey,
        getCryptoWalletPkgPrefix, validateFingerprint); // 目前fingerprint与5miles相同
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    vm->GetEnv((void **) &env, JNI_VERSION_1_4);

    CLZ_CONTEXT = (jclass) env->NewGlobalRef(env->FindClass("android/content/Context"));
    CLZ_PKG_MGR = (jclass) env->NewGlobalRef(env->FindClass("android/content/pm/PackageManager"));
    CLZ_PKG_INFO = (jclass) env->NewGlobalRef(env->FindClass("android/content/pm/PackageInfo"));
    CLZ_SIGNATURE = (jclass) env->NewGlobalRef(env->FindClass("android/content/pm/Signature"));
    MID_GET_PKG_NAME = env->GetMethodID(CLZ_CONTEXT, "getPackageName", "()Ljava/lang/String;");
    MID_GET_PKG_MGR = env->GetMethodID(CLZ_CONTEXT,
                                       "getPackageManager",
                                       "()Landroid/content/pm/PackageManager;");
    MID_GET_PKG_INFO = env->GetMethodID(CLZ_PKG_MGR,
                                        "getPackageInfo",
                                        "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    FID_GET_SIGNATURES = env->GetStaticFieldID(CLZ_PKG_MGR, "GET_SIGNATURES", "I");
    FID_SIGNATURES = env->GetFieldID(CLZ_PKG_INFO, "signatures", "[Landroid/content/pm/Signature;");
    MID_TO_BYTE_ARRAY = env->GetMethodID(CLZ_SIGNATURE, "toByteArray", "()[B");

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL
JNI_OnUnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    vm->GetEnv((void **) &env, JNI_VERSION_1_4);

    env->DeleteGlobalRef(CLZ_CONTEXT);
    env->DeleteGlobalRef(CLZ_PKG_MGR);
    env->DeleteGlobalRef(CLZ_PKG_INFO);
    env->DeleteGlobalRef(CLZ_SIGNATURE);
}

}
