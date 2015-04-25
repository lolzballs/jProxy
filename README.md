# jProxy

[![Join the chat at https://gitter.im/lolzballs/jProxy](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/lolzballs/jProxy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Secure proxy in Java.

## Encryption

The client generates and sends the server 2048-bit RSA public key, with a hash of the public key + the secret key.
The server generates a AES key and sends it to the client, encrypted with RSA, using the public key.

All connections after the AES key is sent will be encrypted.

## Protocol

The jProxy Protocol can be found in the [PROTOCOL.md](./PROTOCOL.md) file.
We are currently on version 2.0.
