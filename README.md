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

Construct an instance of `FusionClient` using the configuration provided by DataMesh. See the [Fusion Cloud API](https://datameshgroup.github.io/fusion/docs/getting-started#sale-system-settings) for instructions on how to manage settings.

```

FusionClient fusionClient;
fusionClient = new FusionClient(useTestEnvironment: true | false);
    saleID = "<<Provided by DataMesh>>";
    poiID = "<<Provided by DataMesh>>";
    kek = "<<Provided by DataMesh>>";
fusionClient.setSettings(saleID, poiID, kek); 

```

Other required settings:

```

String providerIdentification = "<<Provided by DataMesh>>";
String applicationName = "<<Provided by DataMesh>>";
String softwareVersion = "<<Your POS version>>";
String certificationCode = "<<Provided by DataMesh>>";

```


Socket connection is managed for every transaction request. 
To send a transaction request, call `fusionClient.sendMessage(request, serviceID);`
- The serviceID of the transaction should be UUID, it can also be generated using `MessageHeaderUtil.generateServiceID();`. 
- The request is built using [Java Fusion SDK](https://github.com/datameshgroup/fusionsatellite-sdk-java).
- Sample code:


```
// Declare serviceID
String serviceID = MessageHeaderUtil.generateServiceID();
// Build LoginRequest
SaleSoftware saleSoftware = new SaleSoftware.Builder()//
                .providerIdentification(providerIdentification)//
                .applicationName(applicationName)//
                .softwareVersion(softwareVersion)//
                .certificationCode(certificationCode)//
                .build();

        SaleTerminalData saleTerminalData = new SaleTerminalData.Builder()//
                .terminalEnvironment(TerminalEnvironment.SemiAttended)//
                .saleCapabilities(Arrays.asList(SaleCapability.CashierStatus, SaleCapability.CustomerAssistance,
                        SaleCapability.PrinterReceipt))//
                .build();

        LoginRequest loginRequest = new LoginRequest.Builder()//
                .dateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()))//
                .saleSoftware(saleSoftware)//
                .saleTerminalData(saleTerminalData)//
                .operatorLanguage("en")//
                .build();
                
//Send LoginRequest to host             
fusionClient.sendMessage(loginRequest, serviceID);
```

To listen for the transaction response or any other messages from the host, loop on `fusionClient.readMessage();`. This will return a SaleToPOI datatype that can then parsed. Sample code:

```
SaleToPOI saleToPOI = fusionClient.readMessage();

MessageCategory messageCategory;
        if (saleToPOI instanceof SaleToPOIResponse) {
            SaleToPOIResponse response = (SaleToPOIResponse) saleToPOI;
            response.getMessageHeader();
            messageCategory = response.getMessageHeader().getMessageCategory();
            Response responseBody = null;
            switch (messageCategory) {
                case Event:
                    EventNotification eventNotification = response.getEventNotification();
                    log("Event Details: " + eventNotification.getEventDetails());
                    break;
                case Login:
                    if(response.getLoginResponse() != null) {
                        response.getLoginResponse().getResponse();
                        responseBody = response.getLoginResponse().getResponse();
                        if (responseBody.getResult() != null) {
                            log(String.format("Login Result: %s ", responseBody.getResult()));

                            if (responseBody.getResult() != ResponseResult.Success) {
                                log(String.format("Error Condition: %s, Additional Response: %s",
                                        responseBody.getErrorCondition(), responseBody.getAdditionalResponse()));
                            }
                        }
                        waitingForResponse = false;
                    }
                    break;

                default:
                    log(messageCategory + " received during Payment response message handling.");
                    break;
            }
        } else
            log("Unexpected response message received.");

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


For more details on how to use the methods, please refer to the [sample app](https://github.com/datameshgroup/fusioncloud-sdk-java-demo).
