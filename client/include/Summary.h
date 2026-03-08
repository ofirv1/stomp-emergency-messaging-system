#pragma once

#include "event.h"
#include <map>
#include <vector>
#include <string>

class Summary{
public:
    static Summary& getInstance();

    void addEvent(const Event &event);
    void generateSummary(const std::string &channelName, const std::string &user, const std::string &fileName);

private:
    std::map<std::string, std::map<std::string, std::vector<Event>>> channelData;

    Summary() = default;
    ~Summary() = default;

    Summary(const Summary&) = delete;
    Summary& operator=(const Summary&) = delete;

    std::string epochToDateTime(int epoch);
    std::string truncateDescription(const std::string &description);

};

