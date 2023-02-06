# fusioncloud-sdk-java

## How to include

    implementation "com.datameshgroup.fusion:fusion-cloud:1.0.3"

### Overview

***

This repository contains a websocket client and security components to make it easy for developers to connect and communicate with Unify - a platform created by DataMesh.

### Getting Started

***

##### Usage
See the [DataMesh Fusion API](https://datameshgroup.github.io/fusion) documentation for a full description of schema and workflows.
The [fusioncloud-sdk-java-demo](https://github.com/datameshgroup/fusioncloud-sdk-java-demo) application provides sample code for using this library.

Construct an instance of `FusionCloudConfig` using the configuration provided by DataMesh. See the [Fusion API](https://datameshgroup.github.io/fusion/#getting-started-design-your-integration-sale-system-settings) for instructions on how to manage settings.

```
FusionClientConfig fusionClientConfig;
fusionClientConfig = new FusionClientConfig(isTestEnvironment: true | false);
    fusionClientConfig.saleID = "<<Provided by DataMesh>>";
    fusionClientConfig.poiID = "<<Provided by DataMesh>>";
    fusionClientConfig.providerIdentification = "<<Provided by DataMesh>>";
    fusionClientConfig.applicationName = "<<Provided by DataMesh>>";
    fusionClientConfig.softwareVersion = "<<Your POS version>>";
    fusionClientConfig.certificationCode = <<Provided by DataMesh>>";
    fusionClientConfig.kekValue = "<<Provided by DataMesh>>";
```
Construct an instance of `FusionClient` using `FusionCloudConfig` to connect tot the web socket

```
FusionClient fusionClient = new FusionClient();
fusionClient.init(fusionClientConfig);
```

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


Message requests and responses from the websocket server are added to `BlockingQueue<SaleToPOIRequest> inQueueRequest` and `BlockingQueue<SaleToPOIResponse> inQueueResponse`, respectively.

For more details on how to use the methods, please refer to the [sample app](https://github.com/datameshgroup/fusioncloud-sdk-java-demo).
