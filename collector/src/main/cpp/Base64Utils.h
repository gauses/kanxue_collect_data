#ifndef BASE64UTILS_H
#define BASE64UTILS_H

#include <string>

class Base64Utils {
public:
    static std::string Encode(const uint8_t* data, size_t length);
};

#endif // BASE64UTILS_H 