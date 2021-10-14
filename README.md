# fusioncloud-sdk-java

### Overview

***

This repository contains a websocket client and security components to make it easy for developers to connect and communicate with Unify - a platform created by DataMesh.

### Getting Started

***

##### Configuration
The code requires some configurations to be present for it to work properly:

`provider.identification={provider ID}`  
`application.name={application name}`  
`software.version={software version}`  
`certification.code={certification code}`  

`certificate.location={root CA location}`  
`server.domain={domain/server URI}`  
`socket.protocol={protocol version}`

`key.value={KEK provided by DMG}`  
`key.identifier={SpecV2TestMACKey or SpecV2ProdMACKey}`  
`key.version={version}`  

These must be written in a *properties* file and added as argument with key `config.location` at startup (e.g., `-Dconfig.location=src/main/resources/config.properties`)

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
