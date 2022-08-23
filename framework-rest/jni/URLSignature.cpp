#include <stdlib.h>

#include <openssl/hmac.h>
#include <openssl/sha.h>

#include "URLSignature.h"


#ifdef FM_URL_SIGN_NO_ENCODING

/*
 * 如果没有使能encoding,则直接返回原字符串
 */
string* encodeUrl(const char * const ptrBytes) {
    return new string(ptrBytes);
}

#else

/*
 * 不完全按java URLEncoder#encode 的算法
 * 参考oauth并综合iOS和后端的算法 https://dev.twitter.com/oauth/overview/percent-encoding-parameters
 */
inline bool notNeedEncoding(char c) {
    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
        return true;
    }

    if (c >= '0' && c <= '9') {
        return true;
    }

    switch (c) {
//        case ' ':  // encoding a space to a + is done in the encode() method
        case '-':
        case '_':
        case '.':
        case '*':
        case '~':
        case '/':
        case ':':
        case '$':
            return true;
        default:
            break;
    }

    return false;
}


inline char* toHex(unsigned char c) {
    char *hex = (char*) malloc(4);
    sprintf(hex, "%%%02X", int(c));
    return hex;
}

string* encodeUrl(const char * const ptrBytes) {
    string *buf = new string;

    char *byte = (char*) ptrBytes;
    for (; *byte != '\0'; ++byte){
        char c = *byte;
        if (notNeedEncoding(c)) {
//            if (c == ' ') {
//                c = '+';
//            }
            buf->push_back(c);
        } else {
            // convert to external encoding before hex conversion
            char *hex = toHex((unsigned char) c);
            buf->append(hex);
            free(hex);
        }
    }

    return buf;
}

#endif

bool compreParamString (string * const first, string * const second) {
    return *first <= *second;
}

string* encodeParams(t_params_ptr const ptrParams) {
    string *ptrStrParams = new string();
    ptrParams->sort(compreParamString);  // key/value pair should be encoded before sorting

    for (t_params_itr itr = ptrParams->begin(); itr != ptrParams->end(); ++itr) {
        if (itr != ptrParams->begin()) {
            ptrStrParams->push_back('&');
        }
        ptrStrParams->append(**itr);
    }

    string *buf = encodeUrl(ptrStrParams->c_str());
    delete ptrStrParams;
    return buf;
}

char* sign(const char * const msg, const char * const key) {
    unsigned char* digest = HMAC(EVP_sha1(), key, strlen(key), (unsigned char*) msg, strlen(msg), NULL, NULL);

    // Be careful of the length of string with the choosen hash engine. SHA1 produces a 20-byte hash value which rendered as 40 characters.
    // Change the length accordingly with your choosen hash engine
    char *signature = (char*) malloc(41);
    for(int i = 0; i < 20; ++i) {
        sprintf(&signature[i * 2], "%02x", (unsigned int) digest[i]);
    }

    return signature;
}

char* sha1Digest(const uint8_t * const msg, const size_t len) {
    unsigned char* digest = SHA1(msg, len, NULL);
    char *hash = (char*) malloc(41);
    for(int i = 0; i < 20; ++i) {
        sprintf(&hash[i * 2], "%02X", (unsigned int) digest[i]);
    }

    return hash;
}
