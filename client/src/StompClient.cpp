#include "../include/ConnectionHandler.h"
#include "../include/StompEncoderDecoder.h"
#include "../include/event.h"
#include "../include/Summary.h"
#include <iostream>
#include <thread>
#include <string>
#include <map>
#include <atomic>
#include <sstream>
#include <fstream>
#include <vector>
#include <cstring>

std::atomic<bool> interrupted(false);
std::map<std::string, int> subscriptions; // Maps channel names to subscription IDs

void handleServerMessages(ConnectionHandler& connectionHandler, StompEncoderDecoder& encoderDecoder) 
{
    std::string response;
    while (!interrupted) 
    {
        if (!connectionHandler.getLine(response)) 
        {
            std::cerr << "Server disconnected. Exiting...\n";
            interrupted = true;
            break;
        }

        std::string decodedMessage;
        for (char nextByte : response) 
        {
            std::string tempMessage = encoderDecoder.decodeNextByte(nextByte);
            if (!tempMessage.empty()) 
            {
                decodedMessage += tempMessage;
            }
        }
        response = "";
        if (!decodedMessage.empty()) 
        {
            std::cout << "Decoded Server Message: " << decodedMessage << std::endl;
        }
    }
}


int main(int argc, char* argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " <host> <port>\n";
        return -1;
    }

    std::string host = argv[1];
    short port = std::atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    StompEncoderDecoder encoderDecoder;

    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::thread serverThread(handleServerMessages, std::ref(connectionHandler), std::ref(encoderDecoder));

    static int receiptId = 0;
    static int subscriptionId = 0;

    while (!interrupted) {
        char inputBuffer[1024];
        std::cin.getline(inputBuffer, sizeof(inputBuffer)); // Read user input
        std::string command(inputBuffer);

        // Login Command
        if (command.find("login") == 0) {
            std::istringstream iss(command.substr(6)); // Skip "login "
            std::string hostPort, username, password;
            iss >> hostPort >> username >> password;

            size_t colonPos = hostPort.find(':');
            if (colonPos == std::string::npos) {
                std::cerr << "Invalid host:port format. Usage: login {host:port} {username} {password}" << std::endl;
                continue;
            }

            host = hostPort.substr(0, colonPos);
            port = std::stoi(hostPort.substr(colonPos + 1));

            std::map<std::string, std::string> headers = {
                {"accept-version", "1.2"},
                {"host", "stomp.cs.bgu.ac.il"},
                {"login", username},
                {"passcode", password}
            };
            std::string connectFrame = encoderDecoder.encode("CONNECT", headers, "");
            connectionHandler.sendLine(connectFrame);
        }
        // Join Command
        else if (command.find("join ") == 0) {
            std::string channelName = command.substr(5); // Extract everything after "join "
            if (channelName.empty()) {
                std::cerr << "Invalid command format. Usage: join {channel_name}" << std::endl;
                continue;
            }

            subscriptionId++;
            receiptId++;
            subscriptions[channelName] = subscriptionId; // Store the subscription ID for this channel

            std::map<std::string, std::string> headers = {
                {"id", std::to_string(subscriptionId)},
                {"destination", "/" + channelName},
                {"receipt", std::to_string(receiptId)}
            };

            std::string subscribeFrame = encoderDecoder.encode("SUBSCRIBE", headers, "");
            connectionHandler.sendLine(subscribeFrame);

            std::cout << "Joined channel " << channelName << std::endl;
        }
        // Exit Command
        else if (command.find("exit ") == 0) {
            std::string channelName = command.substr(5); // Extract everything after "exit "
            if (channelName.empty()) {
                std::cerr << "Invalid command format. Usage: exit {channel_name}" << std::endl;
                continue;
            }

            if (subscriptions.find(channelName) == subscriptions.end()) {
                std::cerr << "You are not subscribed to channel: " << channelName << std::endl;
                continue;
            }

            int id = subscriptions[channelName]; // Retrieve the correct subscription ID
            receiptId++;

            std::map<std::string, std::string> headers = {
                {"id", std::to_string(id)},
                {"receipt", std::to_string(receiptId)}
            };

            std::string unsubscribeFrame = encoderDecoder.encode("UNSUBSCRIBE", headers, "");
            connectionHandler.sendLine(unsubscribeFrame);

            subscriptions.erase(channelName); // Remove the channel from the map
            std::cout << "Exited channel " << channelName << std::endl;
        }
        // Send Command
        else if (command.find("send ") == 0) {
            char destination[256];
            char body[768];
            std::sscanf(command.c_str(), "send %s %[^\n]", destination, body);

            std::map<std::string, std::string> headers = {{"destination", destination}};
            std::string sendFrame = encoderDecoder.encode("SEND", headers, body);
            connectionHandler.sendLine(sendFrame);
        }
        // Report Command
        else if (command.find("report ") == 0) {
            char filename[256];
            std::sscanf(command.c_str(), "report %s", filename);

            std::ifstream file(filename);
            if (!file) {
                std::cerr << "File not found: " << filename << std::endl;
                continue;
            }

            names_and_events parsedData = parseEventsFile(filename);
            for (const Event& event : parsedData.events) {
                std::map<std::string, std::string> headers = {
                    {"destination", "/" + parsedData.channel_name}
                };
                std::string body = encoderDecoder.encodeEvent(event, "currentUser"); // Replace with actual user
                std::string sendFrame = encoderDecoder.encode("SEND", headers, body);
                connectionHandler.sendLine(sendFrame);
            }
        }
        // Summary Command
        else if (command.find("summary ") == 0) {
            char channelName[256], user[256], filename[256];
            std::sscanf(command.c_str(), "summary %s %s %s", channelName, user, filename);

            Summary::getInstance().generateSummary(channelName, user, filename);
        }
        // Logout Command
        else if (command == "logout") {
            receiptId++;
            std::map<std::string, std::string> headers = {
                {"receipt", std::to_string(receiptId)}
            };
            std::string disconnectFrame = encoderDecoder.encode("DISCONNECT", headers, "");
            connectionHandler.sendLine(disconnectFrame);
            interrupted = true;
            break;
        }
        // Unknown Command
        else {
            std::cerr << "Unknown command: " << command << std::endl;
        }
    }

    serverThread.join();

    return 0;
}
