# Project: Distributed Identity Server(Phase II) [p3]

* Author: Tahsin Imtiaz & Golam Mortuza(Team-17)
* Class: CS555 Spring 2020 [Distributed System]
* Demo of the project: https://drive.google.com/file/d/1k7mWU9K0QM4kFVsOU-o9dEn6l6M_Y9BO/view?usp=sharing

## Overview

In this assignment, We ran multiple copies of the Identity servers. We tested the copies of the server using Docker.
The main purpose of having multiple servers is to increase reliability. We implement the bully algorithm for the servers
to select a coordinator, and we also implemented sequential consistency. All the requirements from p2 is inherited and
working properly as well as our election algorithm and data synchronization and communication between servers.

## Manifest

|Filename               | Description                                                                                          |
|-----------------------|------------------------------------------------------------------------------------------------------|
|README.md              | This file                                                                                            |
|IdServer.java          | Contains necessary code for IdServer                                                                 |
|IdClient.java          | Contains necessary code for an individual client                                                     |
|Constants.java         | Contains all the constants that are requied for running the server.                                  |
|IdServerInterface.java | Contains the functions that will be used by the clients to communicate with the server               |
|CommunicationMode.java | Contains the communication modes enumeration for election and communication between the servers      |
|Database.java          | Contains the functions for getting connection and dealing with data operations in the database       |
|syncObject.java        | Contains functions for message passing between the servers(e.g. lamport time, who is coordinator etc)|
|User.java              | Contains the structure and functions to transfer data between client and server and between servers  |
|Makefile               | Contains the makefile of this project                                                                |

## Building the project

Run the following command to create a docker container:  
 `$ sudo docker build -t p3 .`  
Run the following command to start the IdServer:  
 `$ sudo docker run -it p3 idserver -v -n 5005`  
You can use any other free port available in the machine. For example `5005`. If verbose option is mentioned, the server will print all log messages.

The IdClient uses command line to connect to the IdServer, does one operation and then quits. We can run the IdClient by running:  
 `$ sudo docker run -it p3 idclient --server-list <serverlist> <query>`

Serverlist is file that contains the list of available server. By default client will use the availble serverlist that
is given in the source directory. However, it can be replace by --server-list command

Here <query> is the command line query that the IdClient wants to execute. It must support at least six types of command line queries as follows:  
	
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
1) The tests from p2(Identity server phase I) are also applied here on this part of the project
Every scenario was examined manually.
Some of the scenarios that we checked are:
- The SSL was tested by providing the wrong password for the javax.net.ssl.trustStorePassword and also by changing the 
  valid certificate as expected the server didn't work either of the cases.
- Create was check with no real name and no password.
- Lookup was checked with valid login name, invalid login name and after server close and re-run.
- Reverse lookup was checked with valid uuid, invalid uuid and after server close and re-run.
- Modify was checked with valid and invalid password.
- Delete command was tested for both with password, without password and with wrong password.
- Get was check with all possible types.

2) To test the IdServer's election algorithm and synchronization, we tested the following,

- Check whether a Server looks for other servers and request an election the first time it comes online(pass).
- Check whether a Server elects a coordinator when its the only instance running and when there are multiple  servers running(pass).
- Check whether an already elected Coordinator participates in the electon initiated by a new server(Pass).
- Check whether the server with the most recent information(lamport, and uuid if lamport time is equal) is elected(pass).
- Check whether a server synchronizes with other servers when it stops/crashes and comes back to life afterwards(pass).
- Check Whether all the client requests are handled by the coordinator or forwarded to the coordinator to be handled(pass).
- Check Whether clients get the updated information although the previous coordinator is offline(pass).

## Reflection and Self Assessment
As mentioned in the testing scenarios above, when there are multiple servers running, they elecet a coordinator by executing an election
using the bully algorthim. The election communication was implemented using multicasing. The backup servers repeatedly sync with the
coordinator. Data was stored in a database so server crash won't be a bigdeal. server can sync their database using lamport time.
Client will have a list of available server. Client will iterover the server untill he gets back reply. Doesn't matter 
whichever server client will connect, only the coordinator will provide client the services. So in the backend all server
will invoke the coordinator method. So client doesn't need the coordinator information. As soon as coordinator provide a
service, he will broadcast the service to all by attaching the lamport time. Everybody will update their database based 
the lamport time activity. So server will make a checkpoint with the changes of lamport time. If a new server joins it will
take the lamport timestamp history from coordinator and update itself. All the RMI method was done using SSL.

Both Golam and Tahsin contributed for this project. Following is their specific contributions:
Golam: synced database with lamport timestamps
Tahsin: Implemented the election algorithm(bully)
