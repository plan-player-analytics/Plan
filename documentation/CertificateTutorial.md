![Player Analytics](https://puu.sh/t8vin.png)


# HTTPS Configuration


## How to configure Plan to use SSL Encryption:
- Obtain a Java Keystore file (More below)
- Place the keystore file in the /plugins/Plan/ folder
(If you have a keystore file elsewhere on the machine, you can specify the full path to the file.)
- Change the config settings under **WebServer**:

Config.Setting | Description
------------------- | ---------------
Certificate.KeyStorePath | The absolute path to the certificate file, you can directly use “KEYSTORE.FILE” if the certificate is placed in the Plan folder, if it’s elsewhere, use the direct path to it.
Certificate.KeyPass | The password of the certificate
Certificate.StorePass | The password of the keystore
Certificate.Alias | The alias of the keystore

# Obtaining a Java Keystore File

## How to get a certificate issued by Let’s Encrypt:
See here:
https://letsencrypt.org/getting-started/

## How to convert the certificate files into a Java KeyStore:

Execute keytool command in the Command Prompt (Windows) or in the Shell (Linux)
                                          
`keytool -importcert -file $CERTIFICATE -keystore $KEYSTORE -alias “$ALIAS”` 

After executing the command, you need to enter the password of the keystore currently being created.

Notes:
The password for the keystore must be at least 6 characters long.

**NOTE FOR WINDOWS USERS:**  
The keytool command is only going to work, when the Command Prompt is opened in `$JAVA_HOME\jre$VERSION\lib\security`
You can manage that by going to that path using 
`cd $JAVA_HOME\jre$VERSION\lib\security`

## If you’re unable to obtain a certificate

If you do not have a domain for certificate registration, or can not create a self signed one, Plan.jar contains a self signed RSA 2048 certificate that can be used.

Open Plan.jar in any archive manager (Like WinRAR) and drop the Certificate.keystore file in the /plugins/Plan/ folder.
Set the config settings as follows so that the Certificate works.

Config.Setting | Value
------------------- | ---------------
Certificate.KeyStorePath | Cert.keystore
Certificate.KeyPass | MnD3bU5HpmPXag0e
Certificate.StorePass | wDwwf663NLTm73gL
Certificate.Alias | DefaultPlanCert

Self signed certificates cause browsers to display security warning:
![Certificate Warning](http://puu.sh/wY5Gs/91883c1ced.jpg)

You can ignore this warning. [“Dangers” of self signed certificates, Article](https://www.globalsign.com/en/ssl-information-center/dangers-self-signed-certificates/)

### Displaying a Green HTTPS Lock with Cloudflare

Requirements:
- A cloudflare account
- A domain with full access

If you wish to bypass the security warning (not seeing that the connection isn’t private), you can use [Cloudflare](https://www.cloudflare.com)

- Connect your domain to Cloudflare
- Add a DNS record of the type “A” which points to your Server IP
- Go to “Crypto” -> “SSL” and set the option to “Flexible”

Notes:
If you only want to use HTTPS on the Analytics site, you can use the “Page Rules”
[Page Rules Tutorial](https://support.cloudflare.com/hc/en-us/articles/218411427-Page-Rules-Tutorial)

It is recommend to activate “Automatic HTTPS Rewrites” under “Crypto” to be able to use http://LINK.TLD as well.
This removes the need to write “https://” at the beginning of the address.
