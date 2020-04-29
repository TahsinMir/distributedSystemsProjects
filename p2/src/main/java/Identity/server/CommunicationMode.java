package Identity.server;

//server communication mode
public enum CommunicationMode
{
	ELECTION_REQUIRED,
	ELECTION_RUNNING,
	COORDINATOR_ELECTED;
}