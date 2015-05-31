# jProxy KSF (Key Storage Format)

## Server

Each user shall have it's own .dat file containing its username, and RSA keys.
The name of the file shall be the secret salt.

| Length | Description                                                                 |
| ------:|:--------------------------------------------------------------------------- |
|      2 | The length of the username (n)                                              |
|      n | The username encoded using UTF-8                                            |
|    256 | The secret key (salt)                                                       |
|    296 | The client's RSA public key, encoded using `PKCS8EncodedKeySpec`            |

## Client

The client will have a file which contains the private key of the client, encoded with `PKCS8EncodedKeySpec`, followed by the secret key.
The file shall be named `key.dat`.

| Length | Description                                                                 |
| ------:|:--------------------------------------------------------------------------- |
|    296 | The client's RSA public key, encoded using `PKCS8EncodedKeySpec`            |
|     32 | The secret key (salt)                                                       |
