# Project: Chat server [p1]

* Author: Tahsin Imtiaz & Golam Mortuza(Team-17)
* Class: CS555 Spring 2020 [Distributed System]

## Overview

In this a project of a simplified chat server that can support multiple clients over the Internet. This is project is 
based on one of the earlier popular protocol is IRC (Internet Relay Chat), which is fully described in RFC 1459. 
RFCs (Request For Comments) are the official documents of Internet specifications, communications protocols, procedures, and events.

## Manifest

|Filename               | Description                                                                  |
|-----------------------|------------------------------------------------------------------------------|
|README.md              | This file                                                                    |
|ChatServer.java        | Contains necessary code for chatServer                              |
|ChatClient.java        | Contains necessary code for a individual client                                                       |
|Constant.java          | Contains all the constant that are requied for running the server and client. Like the command that the client will use|
|IRCMessage.java        | Blueprint of the message object that will be passed between server and client                |
|ServerConnection.java  | Contains the code to handle each individual client in the server.                                        |
|ServerDatabase         | Store necessary information to run the server specially channelList and user connected with the channel   |
|Makefile               | Contains the makefile of this project                                        |

## Building the project

Run the following command to compile the project:  
 `$ make`  
Run the following command to start the ChatServer:  
 `$ java ChatServer -p [portNumber] -d [debugLevel]`  
You can use any free port available in the machine. For example `5005`. debugLevel should take value either `0/1`.
If it's set to `0` then server won't show any log message. But if it's set to `1` then server will show the log message.  
Run the following command to run the `ChatClient`:  
`$ java ChatClient`  
You need to connect with the server first to run the available client command. Use `/help` command to see the available 
client command or to see how to connect with the server.  
Run the following command to remove the compailed file from the directory:  
 `$ make clean`  

## Testing

All the files functionality of this project were tested manually. All the corner and edge cases were also tested. 
And there is no bug that we know of.


## Reflection and Self Assessment

Our first approach for this project was to use TCP protocol. Things got complicated when we tried to send a message to 
all client connected to a channel. We had an idea of solving this like keeping all the channel in a hashtable with their 
participating client's nickname and their server socket. Then if a message arrive to a particular channel then loop through 
the channel hashtable and send the message to all connected user using their stored socket. However we didn't implement
this idea. As we thought using multicast would make it more easier. In our multicast implementation server will assign a
free unique port for each channel. Whenever a client wants to joined a channel server just send the channel port number 
where client can send/receive message. Which makes the implementation lot easier and simpler.   
Implementing the shutdown hook as also a challenging task for us. As None of us have done this before.  
If no client is active for 5 minutes server shutdown itself. For implementing this we used regular java timer. Everytime
a client sent/receive a message to any of the multicast channel server gets a notification and server cancel the previous
timer and creates a new timer.
Both of us(Golam and Tahsin) contributed equally for this project. Where we first brainstrom our idea about it and create 
abstract classes together. 
