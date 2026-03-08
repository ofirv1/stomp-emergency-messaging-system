#include "../include/StompEncoderDecoder.h"

StompEncoderDecoder::StompEncoderDecoder() : currentMessage("") {}

std::string StompEncoderDecoder::encode(const std::string& command, const std::map<std::string, std::string>& headers, const std::string& body) 
{
    return buildFrame(command, headers, body);
}

std::string StompEncoderDecoder::encodeEvent(const Event& event, const std::string& user) 
{
    std::map<std::string, std::string> headers;
    headers["destination"] = "/" + event.get_channel_name();

    std::ostringstream body;
    body << "user: " << user << "\n";
    body << "city: " << event.get_city() << "\n";
    body << "event name: " << event.get_name() << "\n";
    body << "date time: " << event.get_date_time() << "\n";
    body << "general information:\n";

    for (const auto& info : event.get_general_information()) 
    {
        body << "\t" << info.first << ": " << info.second << "\n";
    }

    body << "description:\n" << event.get_description() << "\n";

    return buildFrame("SEND", headers, body.str());
}

std::string StompEncoderDecoder::decodeNextByte(char nextByte) 
{
    if (nextByte == '\0') 
    {
        std::string completedMessage = currentMessage;
        currentMessage.clear();
        return completedMessage;
    } 
    else 
    {
        currentMessage += nextByte;
        return "";
    }
}

void StompEncoderDecoder::reset() 
{
    currentMessage.clear();
}

std::string StompEncoderDecoder::buildFrame(const std::string& command, const std::map<std::string, std::string>& headers, const std::string& body) 
{
    std::ostringstream frame;
    frame << command << "\n";

    for (const auto& header : headers)
    {
        frame << header.first << ":" << header.second << "\n";
    }

    frame << "\n";
    if (!body.empty()) 
    {
        frame << body << "\n";
    }

    frame << '\0';
    return frame.str();
}
