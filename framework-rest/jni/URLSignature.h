#pragma once

#include <string>
#include <list>

using namespace std;

static char *URL_SIGNATURE_KEY = NULL;
static char *URL_ENCODED_SIGNATURE_KEY = NULL;  // encoded secret following a '&'
static char *CRYPTOWALLET_URL_SIGNATURE_KEY = NULL;
static char *CRYPTOWALLET_URL_ENCODED_SIGNATURE_KEY = NULL;  // encoded secret following a '&'
static char *WEB_URL_SIGNATURE_KEY = NULL;
static char *WEB_URL_ENCODED_SIGNATURE_KEY = NULL;  // encoded secret following a '&'
static char *URL_COLLECT_SIGNATURE_KEY = NULL;  // encoded secret following a '&'
static char *URL_CUBE_SIGNATURE_KEY = NULL;
static char *URL_CUBE_ENCODED_SIGNATURE_KEY = NULL;  // encoded secret following a '&'
static char *URL_CMT_WALLET_SIGNATURE_KEY = NULL;
static char *URL_CMT_WALLET_ENCODED_SIGNATURE_KEY = NULL;  // encoded secret following a '&'

typedef list<string*> t_params;
typedef t_params *t_params_ptr;
typedef t_params::const_iterator t_params_itr;

inline void initStrConfig(char **out, uint8_t *bytes, size_t len) {
    *out = (char*) malloc((len + 1) * sizeof(char));  // 1 for '\0'
    memcpy(*out, bytes, len);
    (*out)[len] = '\0';
}

inline char* getUrlSignatureKey() {
    if (URL_SIGNATURE_KEY == NULL) {
        // bytes of 'Ut$pRI67KYQD'
        int len = 12;
        uint8_t ptrBytes[] = {0x55, 0x74, 0x24, 0x70, 0x52, 0x49, 0x36, 0x37, 0x4B, 0x59, 0x51, 0x44};
        initStrConfig(&URL_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_SIGNATURE_KEY;
}

inline char* getEncodedUrlSignatureKey() {
    if (URL_ENCODED_SIGNATURE_KEY == NULL) {
        // bytes of '&Ut$pRI67KYQD'
        int len = 13;
        uint8_t ptrBytes[] = {0x26, 0x55, 0x74, 0x24, 0x70, 0x52, 0x49, 0x36, 0x37, 0x4B, 0x59, 0x51, 0x44};
        initStrConfig(&URL_ENCODED_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_ENCODED_SIGNATURE_KEY;
}

inline char* getMarsUrlSignatureKey() {
    if (URL_SIGNATURE_KEY == NULL) {
        // bytes of 'Ut$pRI67KYQD'
        int len = 12;
        uint8_t ptrBytes[] = {0x55, 0x74, 0x24, 0x70, 0x52, 0x49, 0x36, 0x37, 0x4B, 0x59, 0x51, 0x44};
        initStrConfig(&URL_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_SIGNATURE_KEY;
}

inline char* getMarsEncodedUrlSignatureKey() {
    if (URL_ENCODED_SIGNATURE_KEY == NULL) {
        // bytes of '&Ut$pRI67KYQD'
        int len = 13;
        uint8_t ptrBytes[] = {0x26, 0x55, 0x74, 0x24, 0x70, 0x52, 0x49, 0x36, 0x37, 0x4B, 0x59, 0x51, 0x44};
        initStrConfig(&URL_ENCODED_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_ENCODED_SIGNATURE_KEY;
}

inline char* getCryptoWalletUrlSignatureKey() {
    if (CRYPTOWALLET_URL_SIGNATURE_KEY == NULL) {
        // bytes of 'cmt-to-the-moon'
        int len = 15;
        uint8_t ptrBytes[] = {99, 109, 116, 45, 116, 111, 45, 116, 104, 101, 45, 109, 111, 111, 110};
        initStrConfig(&CRYPTOWALLET_URL_SIGNATURE_KEY, ptrBytes, len);
    }

    return CRYPTOWALLET_URL_SIGNATURE_KEY;
}

inline char* getEncodedCryptoWalletUrlSignatureKey() {
    if (CRYPTOWALLET_URL_ENCODED_SIGNATURE_KEY == NULL) {
        // bytes of '&cmt-to-the-moon'
        int len = 16;
        uint8_t ptrBytes[] = {38, 99, 109, 116, 45, 116, 111, 45, 116, 104, 101, 45, 109, 111, 111, 110};
        initStrConfig(&CRYPTOWALLET_URL_ENCODED_SIGNATURE_KEY, ptrBytes, len);
    }

    return CRYPTOWALLET_URL_ENCODED_SIGNATURE_KEY;
}

inline char* getWebUrlSignatureKey() {
    if (WEB_URL_SIGNATURE_KEY == NULL) {
        // bytes of '76CP6j7*y8bm'
        int len = 12;
        uint8_t ptrBytes[] = {55, 54, 67, 80, 54, 106, 55, 42, 121, 56, 98, 109};
        initStrConfig(&WEB_URL_SIGNATURE_KEY, ptrBytes, len);
    }

    return WEB_URL_SIGNATURE_KEY;
}

inline char* getEncodedWebUrlSignatureKey() {
    if (WEB_URL_ENCODED_SIGNATURE_KEY == NULL) {
        // bytes of '&76CP6j7*y8bm'
        int len = 13;
        uint8_t ptrBytes[] = {38, 55, 54, 67, 80, 54, 106, 55, 42, 121, 56, 98, 109};
        initStrConfig(&WEB_URL_ENCODED_SIGNATURE_KEY, ptrBytes, len);
    }

    return WEB_URL_ENCODED_SIGNATURE_KEY;
}

inline char* getUrlCollectSignatureKey() {
    if (URL_COLLECT_SIGNATURE_KEY == NULL) {
        // bytes of 'becffe1d356e9650'
        int len = 16;
        uint8_t ptrBytes[] = {98, 101, 99, 102, 102, 101, 49, 100, 51, 53, 54, 101, 57, 54, 53, 48};
        initStrConfig(&URL_COLLECT_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_COLLECT_SIGNATURE_KEY;
}

inline char* getCubeSignatureKey() {
    if (URL_CUBE_SIGNATURE_KEY == NULL) {
        // bytes of '3f73e9a415bd2f530809f3f03a7bc0755b3cfd88'
        int len = 40;
        uint8_t ptrBytes[] = {51,102,55,51,101,57,97,52,49,53,98,100,50,102,53,51,48,56,48,57,102,51,102,48,51,97,55,98,99,48,55,53,53,98,51,99,102,100,56,56};
        initStrConfig(&URL_CUBE_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_CUBE_SIGNATURE_KEY;
}

inline char* getEncodedCubeSignatureKey() {
    if (URL_CUBE_ENCODED_SIGNATURE_KEY == NULL) {
        // bytes of '&3f73e9a415bd2f530809f3f03a7bc0755b3cfd88'
        int len = 41;
        uint8_t ptrBytes[] = {38,51,102,55,51,101,57,97,52,49,53,98,100,50,102,53,51,48,56,48,57,102,51,102,48,51,97,55,98,99,48,55,53,53,98,51,99,102,100,56,56};
        initStrConfig(&URL_CUBE_ENCODED_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_CUBE_ENCODED_SIGNATURE_KEY;
}

inline char* getCmtWalletSignatureKey() {
    if (URL_CMT_WALLET_SIGNATURE_KEY == NULL) {
        // bytes of '0eac39006ae11211c4b75b3e46edd71e273cb813'
        int len = 40;
        uint8_t ptrBytes[] = {48,101,97,99,51,57,48,48,54,97,101,49,49,50,49,49,99,52,98,55,53,98,51,101,52,54,101,100,100,55,49,101,50,55,51,99,98,56,49,51};
        initStrConfig(&URL_CMT_WALLET_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_CMT_WALLET_SIGNATURE_KEY;
}

inline char* getEncodedCmtWalletSignatureKey() {
    if (URL_CMT_WALLET_ENCODED_SIGNATURE_KEY == NULL) {
        // bytes of '&0eac39006ae11211c4b75b3e46edd71e273cb813'
        int len = 41;
        uint8_t ptrBytes[] = {38,48,101,97,99,51,57,48,48,54,97,101,49,49,50,49,49,99,52,98,55,53,98,51,101,52,54,101,100,100,55,49,101,50,55,51,99,98,56,49,51};
        initStrConfig(&URL_CMT_WALLET_ENCODED_SIGNATURE_KEY, ptrBytes, len);
    }

    return URL_CMT_WALLET_ENCODED_SIGNATURE_KEY;
}

/*
 * 将输入的字节列表按 application/x-www-form-urlencoded 格式编码
 * @see URLEncoder#encode
 */
string* encodeUrl(const char* const);

/*
 * 将给定的参数列表(键值对)排序并按 application/x-www-form-urlencoded 格式编码
 */
string* encodeParams(t_params_ptr const);

/*
 * 对给定的文本进行签名(hash摘要),目前使用 HMAC-SHA1 算法
 */
char* sign(const char* const, const char* const);

/*
 * 生成SHA1摘要
 */
char* sha1Digest(const uint8_t * const, const size_t);
