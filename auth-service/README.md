#Authentication Service

##Getting Started
- Install the [common] package which contains the DTOs and Enums to your local Maven repository

##Environment Variables:

| Variable                 | Type   | Description                                    |
|--------------------------|--------|------------------------------------------------|
| APP_SERVER_PORT          | String | Port where the app runs                        |
| DATABASE_URL             | String | JDBC URL to the database                       |
| DB_USERNAME              | String | Database username                              |
| DB_PASSWORD              | String | Database password                              |
| JWT_JKS                  | String | Base64 string of a keystore file               |
| JWT_PASSWORD             | String | Passphrase for the keystore file               |
| STORAGE_SERVICE_ENDPOINT | String | Endpoint url for storage service               |
| API_KEY                  | String | The api key to access unprotected endpoints    |
| API_SECRET               | String | The api secret to access unprotected endpoints |

##How to generate Keystore(.jks) file
`keytool -genkeypair -alias mariana -keyalg RSA -keypass mariana -keystore mariana.jks -storepass mariana`

* `-storepass` and `-keypass` should be the same, any random string would suffice
* `-alias` should only be `mariana` as this is statically set on `AuthServerConfiguration#jwtAccessTokenConverter`
* `-keystore` name can be random

##Convert keystore file to Base64 string
`cat mariana.jks | base64`

##Getting the public key of the keystore file
`keytool -list -rfc --keystore mariana.jks | openssl x509 -inform pem -pubkey`

* Public key will be used as a verifier on the resource servers.