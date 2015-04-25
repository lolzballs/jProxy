# Initalization Sequence

The initialization sequence is a series of steps which authenticates and sets up a secure connection
between the client and the server.

The stages of the initialization sequence are:
* [Handshake](#handshake)
* [Login](#login)
* [Encryption exchange](#encryption-exchange)

After the initalization sequence, the client and server are free to start Data Tunnelling.

## Handshake
The handshake stage is a crucial part of the jProxy protocol.
This lets the server know what version the client is connecting with and further details.

| Direction | Size (bytes)  | Description  |
| ----------|:--------------| ------|
| C -> S    | 6      | The string "jProxy" encoded with UTF-8 |
| C -> S    | 1      | The major version of jProxy            |
| C -> S    | 1      | The minor version of jProxy            |
| S -> C    | 6      | The string "jProxy" encoded with UTF-8 |
| S -> C    | 1      | A status code: 0 = _No Error_, 1 _Unsupported Version_ |

If _No Error_ proceed to _**Login**_ stage.

## Login
The login stage authenticates the user, to prevent malicious connections.
This step is required to allow only authorized people from accessing the server.

| Direction | Size (bytes)  | Description  |
| ----------|:--------------| ------|
| C -> S    | 2      | The username string length, encoded as an unsigned short (**u**) |
| C -> S    | **u**  | The username string, encoded with UTF-8              |
| S -> C    | 64     | The stored salt (**storeSalt**) for the user         |
| S -> C    | 64     | The randomly generated salt (**salt**)               |
| C -> S    | 64     | sha256(sha256(**storeSalt** + password) + **salt**)  |
| S -> C    | 1      | Return code: 0 = _No Error_, 1 = _Invalid U/P combo_ |

If _No Error_ proceed to _**Encryption Exchange**_ stage.

## Encryption Exchange
This stage is a crucial part of the security of the system.
In this stage, the client and server exchange RSA keys and the AES key, 
which are used to provide a secure connection for data tunnelling.

| Direction | Size (bytes)  | Description  |
| ----------|:--------------| ------|
| C -> S    | 294    | 2048-bit client RSA **PUBLIC** key  |
| S -> C    | 294    | 2048-bit server RSA **PUBLIC** key  |
| S -> C    | 256    | 1028-bit AES key encrypted with client RSA key |

If no errors occur, then the server/client can start _**Data Tunneling**_


# Data Tunnelling

Data Tunnelling is the main portion of the jProxy protocol. Data Tunnelling is where all the TCP, UDP and ICMP packets get sent and forwarded.

This entire section is encrypted, and as such there is an overhead for every packet sent here. All packets sent in the data tunnelling portion will be contained within the payload of this packet.

# Overhead Encrypted Packet
| Direction        | Size (bytes)  | Description  |
| ---------------- |:--------------| ------------ |
| Bidirectional    | 1             | This indicates the payload length divided by 16 (AES requires this) |
| Bidirectional    | **n** * 16    | The payload, encrypted with AES |

## TCP

To initiate a TCP connection, the client must:

| Direction | Size (bytes)  | Description  |
| --------- |:--------------| ------------ |
| C -> S    | 1    | (Magic value) 0x41 |
| C -> S    | 4    | The IP Address to connect to |
| C -> S    | 2    | The port to connect to       |
| C -> S    | 2    | The client ID to be used for this connection |

The server, upon receiving this request, will attempt to connect. After success or failure, the server will send a TCP connection response to the client:

| Direction | Size (bytes)  | Description  |
| --------- |:--------------| ------------ |
| S -> C    | 1    | (Magic value) 0x41 |
| S -> C    | 1    | The connection status code (1=_OK_, 0=_Other_, -1=_Connection Refused_, -2=_Timed Out_, -3=_Unreachable_) |
| S -> C    | 2    | The client ID to be used for this connection |

If the connection is broken, the server will send a connection death packet to the client:

| Direction | Size (bytes)  | Description  |
| --------- |:--------------| ------------ |
| S -> C    | 1    | (Magic value) 0x42 |
| S -> C    | 2    | The client ID for this connection |
