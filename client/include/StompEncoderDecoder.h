#pragma once
#include <string>
#include <map>
#include <sstream>
#include "event.h"

class StompEncoderDecoder {
public:
    StompEncoderDecoder();

    std::string encode(const std::string& command, const std::map<std::string, std::string>& headers, const std::string& body);

    std::string encodeEvent(const Event& event, const std::string& user);

    std::string decodeNextByte(char nextByte);

    void reset();

private:
    std::string currentMessage;

    std::string buildFrame(const std::string& command, const std::map<std::string, std::string>& headers, const std::string& body);
};
