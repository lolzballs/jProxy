Each user shall have it's own .dat file containing its username, and RSA keys.
The name of the file shall be the secret salt.

| Length | Description                                                                 |
| ------:|:--------------------------------------------------------------------------- |
|      2 | The length of the username (n)                                              |
|      n | The username encoded using UTF-8                                            |
|    296 | The client's RSA public key, encoded using `PKCS8EncodedKeySpec`            |
|   1216 | The server's RSA private key, encoded using `PKCS8EncodedKeySpec`           |
