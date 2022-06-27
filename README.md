**IMPLEMENTATION OF DISTRIBUTED DATA STORE**:

This project is a simplified Primary-based Remote-write protocol to implement sequential consistency for distributed data store. The distributed data store is replicated at one primary server and multiple
backup servers. For simplicity, the data store maintains just one integer variable with initial value of 0;
the servers will run on the same computer as different processes.

**The primary server** -

When the primary server starts, it takes one integer argument: PrimaryPort. Then, it (1) sets up an
integer variable (i.e., the primary replica of the data store), (2) creates a TCP server socket with port
PrimaryPort, and (3) waits at the port for requests from clients or backup servers. It needs to handle the
following types of requests coming to the port:

1. **READ** request from client – When receiving this request, it returns the current value of its
   replica of the data store variable.
2. **WRITE:newValue** request from client – When receiving this request, it (1) updates its own
   replica of the data store to newValue, (2) requests each of the backup server to update its replica
   of the data store and gets acknowledgement from it, and then (3) replies to the requesting client.
3. **JOIN:backupPort** request from a backup server – When receiving this request, it (1)
   records the requesting backup server’s server socket port number (i.e., backupPort), and then (2)
   sends acknowledgement to the requesting backup server.
4. **UPDATE:newValue** request from a backup server -- When receiving this request, it first
   acts the same as receiving **WRITE:newValue** to have all the data store replicas to update to **newValue**. Then, it acknowledges the requesting backup server of the completion of update.

**Backup servers** - 

When a backup server starts, it takes two integer argument: BackupPort and PrimaryPort. Then, it (1)
sets up an integer variable (i.e., a backup replica of the data store), (2) sends a JOIN:BackupPort request
to the primary server’s server socket at port PrimaryPort, creates a TCP server socket at port
BackupPort, and (3) waits at port BackupPort for requests from clients or the primary server. It needs
to handle the following types of requests coming to the port:

1. **READ** request from client – When receiving this request, it returns the current value of its
   replica of the data store variable.
2. **WRITE:newValue** request from client – When receiving this request, it sends
   **UPDATE:newValue** request to the primary server, and waits for its acknowledgement. Then, it
   sends acknowledgement to the requesting client.
3. **UPDATE:newValue** request from the primary server -- When receiving this request, it
   updates its replica of data store to **newValue** and then replies acknowledgement to the primary
   server.

**The Client** -
Once started, it accepts and passes commands you input to
the servers accordingly.
