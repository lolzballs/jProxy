The keys.dat file stores the secret keys and the RSA keys which it maps to.

The keys.dat file should repeat the following sequence, depending on how many keys or users there are to be stored.

| Length | Description                                                                 |
| ------:|:--------------------------------------------------------------------------- |
|      2 | The length of the username (n)                                              |
|      n | The username encoded using UTF-8                                            |
|    256 | The secret key (salt)                                                       |
|    296 | The client's RSA public key, encoded using `PKCS8EncodedKeySpec`            |
|   1216 | The server's RSA private key, encoded using `PKCS8EncodedKeySpec`           |
