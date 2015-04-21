# jProxy
Secure proxy in Java.

## Encryption

The server and client exchange RSA 2048 bit public keys. After exchange, server generates an AES key
and sends it to the client, encrypted via the previously sent RSA public key by the client. The rest of the transmissions are sent
via AES encryption.

