# fusioncloud-sdk-java

## How to include

    implementation "com.datameshgroup.fusion:fusion-cloud:1.0.3"

### Overview

***

This repository contains a websocket client and security components to make it easy for developers to connect and communicate with Unify - a platform created by DataMesh.

### Getting Started

***

##### Configuration
The following configuration classes must be initialized with the appropriate values during start up (otherwise a `ConfigurationException` will be thrown when an instance of any of these classes is called):

`FusionClientConfig`
 <strike>- certificateLocation (root CA location e.g., 'src/main/resources/root.crt')</strike>
 - serverDomain (domain/server URI)
 - socketProtocol (defaults to 'TLSv1.2' if not provided)

`KEKConfig`
 - value (KEK provided by DataMesh)
 - keyIdentifier (SpecV2TestMACKey or SpecV2ProdMACKey)
 - keyVersion (version)

`SaleSystemConfig` (static sale system settings - provided by DataMesh)
 - providerIdentification
 - applicationName
 - softwareVersion
 - certificationCode

### Dependencies

***

This project uses the following dependencies:  

- **[Moshi](https://github.com/square/moshi):** a library to parse JSON into Java objects (vice versa)  
- **[Tyrus Standalone Client](https://github.com/eclipse-ee4j/tyrus):** an open source reference implementation of [Java API for WebSocket - JSR 356](https://www.oracle.com/technical-resources/articles/java/jsr356.html)
- **[Java Fusion SDK](https://github.com/datameshgroup/fusionsatellite-sdk-java):** contains all the models necessary to create request and response messages to the Fusion websocket server

### Minimum Required JDK

***

- Java 1.8

> **Note:** Other versions may work as well, but have not been tested.

### Usage

***

The `com.dmg.fusion.client.WebSocketClient.connect(URI)` method expects a valid wss URI to connect to. Unify utilises a self-signed root CA provided by DataMesh. The certificate must be added to the project directory and its location must be specified in the *properties* file (see [Getting Started](#getting-started)) with key `certificate.location`.

**Connecting to websocket server:**  

```java
webSocketClient.connect(new URI(wss://www.cloudposintegration.io/nexodev));
```

**Generate security trailer:**  

```java
securityTrailer = SecurityTrailerUtil.generateSecurityTrailer(messageHeader, loginRequest, KEK);
```

**Build a SaleToPOIRequest:**  

```java
SaleToPOIRequest saleToPOI = new SaleToPOIRequest.Builder()
    .messageHeader(messageHeader)
    .request(loginRequest)
    .securityTrailer(securityTrailer)
    .build();
```

Message requests and responses from the websocket server are added to `BlockingQueue<SaleToPOIRequest> inQueueRequest` and `BlockingQueue<SaleToPOIResponse> inQueueResponse`, respectively.

For more details on how to use the methods, please refer to the [sample app](https://github.com/datameshgroup/fusioncloud-sdk-java-demo).
