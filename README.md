# jProxy

[![Join the chat at https://gitter.im/lolzballs/jProxy](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/lolzballs/jProxy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Secure proxy in Java.

## Encryption

The server and client exchange RSA 2048 bit public keys. After exchange, server generates an AES key
and sends it to the client, encrypted via the previously sent RSA keys. The rest of the transmissions are sent
via AES encryption.
