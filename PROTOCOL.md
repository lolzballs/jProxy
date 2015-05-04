# jProxy Protocol 2.0

All numbers are little-endian.

## Packet Description

All of the following descriptors will be from the perspective of the client.

| Direction                          | Length          | Description                                           |
|:---------------------------------- | ---------------:|:----------------------------------------------------- |
| **s**=send, **r**=recv, **b**=both | Length in bytes | A short description about what this field is used for |


## Initialization Sequence

The initialization sequence is a series of steps which authenticates and sets up a secure connection between the client and the server.

The stages of the initialization sequence are:
* [Handshake](#handshake)
* [Login](#login-and-encryption-exchange)

After the initialization sequence, the client and server are free to start Data Tunnelling.

### Handshake

The handshake stage is a crucial part of the jProxy protocol. This lets the server know what version the client is connecting with and further details.

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| s |      6 | Magic Value "jProxy"                                                                |
| s |      1 | Client Major Version                                                                |
| s |      1 | Client Minor Version                                                                |
| r |      6 | Magic Value "jProxy"                                                                |
| r |      1 | Status (0=Accepted, 1=Version Mismatch)                                             |

If *No Error* proceed to Login stage.

### Login and Encryption Exchange

The login stage authenticates the user, to prevent malicious connections.
This step is required to allow only authorized people from accessing the server.

In this stage, the client and server also exchange RSA and AES keys, giving encryption for Data Tunnelling.

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| s |    296 | 2048-bit RSA Client PUBLIC Key                                                      |
| s |     32 | Hash of Client PUBLIC Key with Shared Secret Salt                                   |
| r |      1 | Status (0=Accepted, 1=Invalid Key)                                                  |
| r |     32 | 256-bit AES Key encrypted with Client PUBLIC Key (if status is accepted)            |


## Data Tunnelling

Data Tunnelling is the main portion of the jProxy protocol.
Data Tunnelling allows for the forwarding of TCP, UDP, and ICMP packets.

### Overhead Encrypted Packet

All the payloads below will be encrypted using the following format.
The packet data will be encrypted into a multiple of 16 bytes (as per AES),
which goes into the Payload field of the Overhead Encrypted Packet.

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| b |      1 | Number of blocks (n)                                                                |
| b | n * 16 | Encrypted Data: Payload                                                             |


### TCP Connect

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| s |      1 | Magic Value: 1                                                                      |
| s |      2 | Connection Identifier                                                               |
| s |      2 | Remote Port                                                                         |
| s |      1 | Type of Address (1=IPv4, 2=IPv6, 3=DNS)                                             |
| s |      1 | Address Length (n)                                                                  |
| s |      n | Address Data                                                                        |
| r |      1 | Magic Value: 1                                                                      |
| r |      2 | Connection Identifier                                                               |
| r |      1 | Status (0=OK, 1=Refused, 2=Timed out...)                                            |
| r |      2 | Response Time in ms                                                                 |

### TCP Packet

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| b |      1 | Magic Value: 21                                                                     |
| b |      2 | Client Identifier                                                                   |
| b |      2 | Data Length (n)                                                                     |
| b |      n | Data                                                                                |

### TCP Disconnect

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| b |      1 | Magic Value: 41                                                                     |
| b |      2 | Client Identifier                                                                   |
| b |      1 | Reason (0=UNKNOWN, 1=RESET, ...)                                                    |



### UDP Associate

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| s |      1 | Magic Value: 2                                                                      |
| s |      2 | Connection Identifier                                                               |
| s |      2 | Local Port                                                                          |
| s |      2 | Remote Port                                                                         |
| s |      1 | Type of Address (1=IPv4, 2=IPv6, 3=DNS)                                             |
| s |      1 | Address Length (n)                                                                  |
| s |      n | Address Data                                                                        |
| r |      1 | Magic Value: 2                                                                      |
| r |      2 | Connection Identifier                                                               |
| r |      1 | Status (0=Successful, 1=Local Port not available)                                   |
| r |      2 | Local Port                                                                          |

### UDP Packet

| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| b |      1 | Magic Value: 22                                                                     |
| b |      2 | Connection Identifier                                                               |
| b |      2 | Data Length (n)                                                                     |
| b |      n | Data                                                                                |

### UDP Dissociate
| Direction | Length | Description                                                                 |
|:--------- | ------:|:--------------------------------------------------------------------------- |
| s |      1 | Magic Value: 42                                                                     |
| s |      2 | Connection Identifier                                                               |
