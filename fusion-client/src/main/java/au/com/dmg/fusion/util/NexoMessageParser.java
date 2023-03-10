package au.com.dmg.fusion.util;

import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageClass;
import au.com.dmg.fusion.data.MessageType;
import au.com.dmg.fusion.exception.FusionException;
import au.com.dmg.fusion.request.Request;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.request.aborttransactionrequest.AbortTransactionRequest;
import au.com.dmg.fusion.request.cardacquisitionrequest.CardAcquisitionRequest;
import au.com.dmg.fusion.request.loginrequest.LoginRequest;
import au.com.dmg.fusion.request.logoutrequest.LogoutRequest;
import au.com.dmg.fusion.request.paymentrequest.PaymentRequest;
import au.com.dmg.fusion.request.reconciliationrequest.ReconciliationRequest;
import au.com.dmg.fusion.request.reversalrequest.ReversalRequest;
import au.com.dmg.fusion.request.transactionstatusrequest.TransactionStatusRequest;
import au.com.dmg.fusion.securitytrailer.SecurityTrailer;

public class NexoMessageParser {

    /// ProtocolVersion implemented by this NexoMessageParser.
    public String ProtocolVersion = "3.1-dmg";

    /// Defines if we should be using production or test keys
    public boolean useTestKeyIdentifier;

    private String errorString = null;

    public NexoMessageParser(boolean useTestKeyIdentifier){
        this.useTestKeyIdentifier = useTestKeyIdentifier;
    }

    public SaleToPOIRequest BuildSaleToPOIMessage(String serviceID, String saleID, String poiID, String kek, Request requestMessage) {
        errorString = null;

        if (requestMessage == null){
            throw new FusionException("Invalid request. Message payload must not be null", false);
        }
        if(!isValueValid("Protocol Version", ProtocolVersion) ||
                !isValueValid("Service ID", serviceID) ||
                !isValueValid("Sale ID", saleID) ||
                !isValueValid("POI ID", poiID) ||
                !isValueValid("KEK", kek)){
            throw new FusionException(errorString, false);
        }

        MessageCategory messageCategory = getMessageCategory(requestMessage);

        MessageHeader messageHeader = new MessageHeader.Builder()
                .messageClass(MessageClass.Service)
                .messageCategory(messageCategory)
                .messageType(MessageType.Request)
                .serviceID(serviceID)
                .saleID(saleID)
                .POIID(poiID)
                .build();

        SecurityTrailer securityTrailer = SecurityTrailerUtil.generateSecurityTrailer(messageHeader, requestMessage, kek, useTestKeyIdentifier);
        SaleToPOIRequest saleToPOI = new SaleToPOIRequest.Builder()
                .messageHeader(messageHeader)
                .request(requestMessage)
                .securityTrailer(securityTrailer)
                .build();

        return saleToPOI;
    }

    private boolean isValueValid(String propertyName, String value){
        boolean isValid = true;
        if((value == null) || (value.length() == 0)){
            errorString = "Invalid " + propertyName + ".  Required length is > 0.";
            isValid = false;
        }
        return isValid;
    }

    private MessageCategory getMessageCategory(Request message){
        MessageCategory mc = null;

        if(message instanceof LoginRequest){
            mc = MessageCategory.Login;
        } else if(message instanceof AbortTransactionRequest){
            mc = MessageCategory.Abort;
        } else if(message instanceof CardAcquisitionRequest){
            mc = MessageCategory.CardAcquisition;
        } else if(message instanceof LogoutRequest){
            mc = MessageCategory.Logout;
        } else if(message instanceof PaymentRequest){
            mc = MessageCategory.Payment;
        } else if(message instanceof ReconciliationRequest){
            mc = MessageCategory.Reconciliation;
        } else if(message instanceof ReversalRequest){
            mc = MessageCategory.Reversal;
        } else if(message instanceof TransactionStatusRequest){
            mc = MessageCategory.TransactionStatus;
        }

        return mc;
    }
}
