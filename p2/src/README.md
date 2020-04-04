# Project: Identity Server [p2]

* Author: Tahsin Imtiaz & Golam Mortuza(Team-17)
* Class: CS555 Spring 2020 [Distributed System]

## Overview

In this assignment, we implemented a RMI based identity server. The client can connect to this server and submit a new login name request. The server checks its database and responds back either with a Universally Unique ID (UUID) if the new login id hasn’t been taken by anyone before or returns back with an error. As part of the request to create a new login name, the client must submit a real user name as well. The server also permits reverse lookups, where a client submits a UUID and ask for the login name and other information associated with that UUID. The server periodically saves its state on to the disk so that it can survive crashes and shutdowns.

## Manifest

|Filename               | Description                                                                  |
|-----------------------|------------------------------------------------------------------------------|
|README.md              | This file                                                                    |
|IdServer.java          | Contains necessary code for IdServer                                         |
|IdClient.java          | Contains necessary code for an individual client                             |
|Constant.java          | Contains all the constants that are requied for running the server. e.g. the responses the server will return|
|IdServerInterface.java | Contains the functions that will be used by the clients to communicate with the server|
|Makefile               | Contains the makefile of this project                                        |

## Building the project

Run the following command to compile the project:  
 $ make  
Run the following command to start the ChatServer:  
 $ java ChatServer --numport <port#> --verbose <verboseLevel#>  
You can use any free port available in the machine. For example `5005`. verbose should take value either `0/1`.
If it's set to `0` then server won't show any log messages. But if it's set to `1` then server will show the log messages.

The IdClient uses command line to connect to the IdServer, does one operation and then quits. We can run the IdClient by running:
 $ java IdClient --server <serverhost> [--numport <port#>] <query>

Here <query> is the command line query that the IdClient want to execute. It must support at least six types of command line queries as follows:
	--create <loginname> [<real name>] [--password <password>]
		the client requests to create a new login name.
	--lookup <loginname>
		the client looks for user information for the certain loginname.
	--reverse-lookup <UUID>
		the client looks for user information for the certain UUID.
	--modify <oldloginname> <newloginname> [--password <password>]
		the client requests a modification of the loginname with the new loginname.
	--delete <loginname> [--password <password>]
		the client requests to delete the information with the particular loginname.
	--get users|uuids|all
		the client requests information of all user. Either it is only the login names, or uuids or full information list

Run the following command to remove the compiled file from the directory:  
 `$ make clean`  

## Testing



## Reflection and Self Assessment


Golam:
Tahsin: