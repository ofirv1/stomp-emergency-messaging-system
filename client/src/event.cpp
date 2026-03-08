#include "../include/event.h"
#include "../include/json.hpp"
#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <vector>
#include <sstream>
#include <cstring>
#include <stdexcept>

using namespace std;
using json = nlohmann::json;

void split_str(const std::string &str, char delimiter, std::vector<std::string> &out) {
    std::stringstream ss(str);
    std::string token;
    while (std::getline(ss, token, delimiter)) {
        out.push_back(token);
    }
}

static std::string trim(const std::string& s) {
    size_t start = s.find_first_not_of(" \r\n\t");
    if (start == std::string::npos) {
        return "";
    }
    size_t end = s.find_last_not_of(" \r\n\t");
    return s.substr(start, end - start + 1);
}

static int parse_date_time_field(const json& value) {
    if (value.is_number_integer()) {
        return value.get<int>();
    }

    if (value.is_string()) {
        return std::stoi(value.get<std::string>());
    }

    throw std::runtime_error("Invalid date_time format in JSON");
}

Event::Event(std::string channel_name, std::string city, std::string name, int date_time,
             std::string description, std::map<std::string, std::string> general_information)
    : channel_name(channel_name),
      city(city),
      name(name),
      date_time(date_time),
      description(description),
      general_information(general_information),
      eventOwnerUser("")
{
}

Event::~Event()
{
}

void Event::setEventOwnerUser(std::string setEventOwnerUser) {
    eventOwnerUser = setEventOwnerUser;
}

const std::string &Event::getEventOwnerUser() const {
    return eventOwnerUser;
}

const std::string &Event::get_channel_name() const
{
    return this->channel_name;
}

const std::string &Event::get_city() const
{
    return this->city;
}

const std::string &Event::get_name() const
{
    return this->name;
}

int Event::get_date_time() const
{
    return this->date_time;
}

const std::map<std::string, std::string> &Event::get_general_information() const
{
    return this->general_information;
}

const std::string &Event::get_description() const
{
    return this->description;
}

Event::Event(const std::string &frame_body)
    : channel_name(""),
      city(""),
      name(""),
      date_time(0),
      description(""),
      general_information(),
      eventOwnerUser("")
{
    stringstream ss(frame_body);
    string line;
    string eventDescription;
    map<string, string> general_information_from_string;
    bool inGeneralInformation = false;
    bool inDescription = false;

    while (getline(ss, line, '\n')) {
        line = trim(line);

        if (line.empty() && !inDescription) {
            continue;
        }

        if (inDescription) {
            if (!eventDescription.empty()) {
                eventDescription += "\n";
            }
            eventDescription += line;
            continue;
        }

        size_t colonPos = line.find(':');
        if (colonPos == string::npos) {
            continue;
        }

        string key = trim(line.substr(0, colonPos));
        string val = trim(line.substr(colonPos + 1));

        if (key == "user") {
            eventOwnerUser = val;
            continue;
        }

        if (key == "channel name") {
            channel_name = val;
            continue;
        }

        if (key == "city") {
            city = val;
            continue;
        }

        if (key == "event name") {
            name = val;
            continue;
        }

        if (key == "date time") {
            date_time = std::stoi(val);
            continue;
        }

        if (key == "general information") {
            inGeneralInformation = true;
            continue;
        }

        if (key == "description") {
            inGeneralInformation = false;
            inDescription = true;
            continue;
        }

        if (inGeneralInformation) {
            general_information_from_string[key] = val;
        }
    }

    description = eventDescription;
    general_information = general_information_from_string;
}

names_and_events parseEventsFile(std::string json_path)
{
    std::ifstream f(json_path);
    if (!f.is_open()) {
        throw std::runtime_error("Failed to open events file: " + json_path);
    }

    json data = json::parse(f);

    std::string channel_name = data["channel_name"];

    std::vector<Event> events;
    for (auto &event : data["events"])
    {
        std::string name = event["event_name"];
        std::string city = event["city"];
        int date_time = parse_date_time_field(event["date_time"]);
        std::string description = event["description"];

        std::map<std::string, std::string> general_information;
        for (auto &update : event["general_information"].items())
        {
            if (update.value().is_string()) {
                general_information[update.key()] = update.value().get<std::string>();
            } else {
                general_information[update.key()] = update.value().dump();
            }
        }

        events.push_back(Event(channel_name, city, name, date_time, description, general_information));
    }

    names_and_events events_and_names{channel_name, events};
    return events_and_names;
}
