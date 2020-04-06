# Project: Identity Server [p2]

* Author: Tahsin Imtiaz & Golam Mortuza(Team-17)
* Class: CS555 Spring 2020 [Distributed System]
* Demo of the project: https://drive.google.com/file/d/1k7mWU9K0QM4kFVsOU-o9dEn6l6M_Y9BO/view?usp=sharing

## Overview

In this assignment, we implemented a RMI based identity server. The client can connect to this server and submit a new 
login name request. The server checks its database and responds back either with a Universally Unique ID (UUID) if the 
new login id hasn’t been taken by anyone before or returns back with an error. As part of the request to create a new 
login name, the client must submit a real user name as well. The server also permits reverse lookups, where a client 
submits a UUID and ask for the login name and other information associated with that UUID. The server periodically saves
 its state on to the disk so that it can survive crashes and shutdowns.

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
 `$ make`  
Run the following command to start the ChatServer:  
 `$ make idserver ARGS="--v -n 5179"`  
You can use any free port available in the machine. For example `5005`. verbose should take value either `0/1`.
If it's set to `0` then server won't show any log messages. But if it's set to `1` then server will show the log messages.

The IdClient uses command line to connect to the IdServer, does one operation and then quits. We can run the IdClient by running:  
 `$ make idclient ARGS=" --server <serverhost> [--numport <port#>] <query>"`

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
No unit test or integration test was written for this project. However every scenario was examined manually.
The SSL was tested by providing the wrong password for the javax.net.ssl.trustStorePassword and also by changing the 
valid certificate as expected the server didn't work either of the cases. Each query was tested manually for every 
scenario. For example delete command was tested for both with password, without password and with wrong password.


## Reflection and Self Assessment
Remote Method Invocation in java is much easier to work with than using TCP or UDP protocol. It seems like using running
the command in the same machine. After registering the RMI and running the server it never felt like we are calling the
method from a different machine. The overall project was pretty easy to implement. Initially server write client data 
into a Hashtable then write it on the database. So even if the server shutdown it can always get the data from the database.
So server crush won't be a big deal for this case.
Both Golam and Tahsin contributed equally for this project. Following is their specific contribution
Golam: Implemnt RMI with SSL certificate, implement client method on client side, Command line argument parser
Tahsin: Handle client request on server side, handling database, testing