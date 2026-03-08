#include "../include/Summary.h"
#include <algorithm>
#include <iomanip>
#include <sstream>
#include <iostream>
#include <fstream>
#include <string>

using namespace std;

Summary& Summary::getInstance() 
{
    static Summary instance;
    return instance;
}

void Summary::addEvent(const Event &event)
{
    channelData[event.get_channel_name()][event.getEventOwnerUser()].push_back(event);
}

void Summary::generateSummary(const string &channelName, const string &user, const string &fileName) 
{
    if (channelData.find(channelName) == channelData.end()) 
    {
        cerr << "Error: Channel " << channelName << " does not exist." << endl;
        return;
    }

    const auto &userEvents = channelData.at(channelName);

    if (userEvents.find(user) == userEvents.end()) 
    {
        cerr << "Error: No events found for user " << user << " in channel " << channelName << "." << endl;
        return;
    }

    const auto &events = userEvents.at(user);

    ofstream outFile(fileName);
    if (!outFile.is_open()) 
    {
        cerr << "Error: Could not open file " << fileName << " for writing." << endl;
        return;
    }

    outFile << "Channel " << channelName << endl;
    outFile << "Stats:" << endl;
    outFile << "Total: " << events.size() << endl;

    int activeCount = 0;
    int forcesArrivalCount = 0;
    for (const auto &event : events) 
    {
        if (event.get_general_information().at("active") == "true")
        {
            activeCount++;
        }
        if (event.get_general_information().at("forces_arrival_at_scene") == "true") 
        {
            forcesArrivalCount++;
        }
    }

    outFile << "active: " << activeCount << endl;
    outFile << "forces arrival at scene: " << forcesArrivalCount << endl;
    outFile << endl;

    vector<Event> sortedEvents = events;
    sort(sortedEvents.begin(), sortedEvents.end(), [](const Event &a, const Event &b) {
        if (a.get_date_time() < b.get_date_time()) {
            return true;
        } else if (a.get_date_time() > b.get_date_time()) {
            return false;
        } else {
            return a.get_name() < b.get_name();
        }
    });

    outFile << "Event Reports:" << endl;
    int reportNumber = 1;
    for (const auto &event : sortedEvents) 
    {
        outFile << "Report_" << reportNumber++ << ":" << endl;
        outFile << "city: " << event.get_city() << endl;
        outFile << "date time: " << epochToDateTime(event.get_date_time()) << endl;
        outFile << "event name: " << event.get_name() << endl;
        outFile << "summary: " << truncateDescription(event.get_description()) << endl;
        outFile << endl;
    }

    outFile.close();
    cout << "Summary successfully written to " << fileName << endl;
}

string Summary::epochToDateTime(int epoch) 
{
    time_t rawTime = static_cast<time_t>(epoch);
    struct tm *timeInfo = localtime(&rawTime);
    char buffer[20];
    strftime(buffer, sizeof(buffer), "%d/%m/%y %H:%M", timeInfo);
    return string(buffer);
}

string Summary::truncateDescription(const string &description) 
{
    if (description.length() > 27) 
    {
        return description.substr(0, 27) + "...";
    }
    return description;
}
