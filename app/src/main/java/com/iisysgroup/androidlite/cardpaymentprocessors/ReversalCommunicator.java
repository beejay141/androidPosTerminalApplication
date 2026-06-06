package com.iisysgroup.androidlite.cardpaymentprocessors;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.iisysgroup.androidlite.App;
import com.iisysgroup.poslib.ISO.GTMS.GtmsKeyProcessor;
import com.iisysgroup.poslib.ISO.POSVAS.PosvasKeyProcessor;
import com.iisysgroup.poslib.ISO.common.Constants;
import com.iisysgroup.poslib.ISO.common.IsoAdapter;
import com.iisysgroup.poslib.ISO.common.IsoReversalProcessData;
import com.iisysgroup.poslib.ISO.common.IsoTimeManager;
import com.iisysgroup.poslib.ISO.common.IsoTransactionExecutor;
import com.iisysgroup.poslib.ISO.common.IsoUtility;
import com.iisysgroup.poslib.ISO.common.PostRequest;
import com.iisysgroup.poslib.ISO.common.TlvHelper;
import com.iisysgroup.poslib.commons.TLV;
import com.iisysgroup.poslib.commons.TLVParser;
import com.iisysgroup.poslib.commons.TripleDES;
import com.iisysgroup.poslib.commons.Utility;
import com.iisysgroup.poslib.commons.dukpt.DUKPTUtil;
import com.iisysgroup.poslib.commons.dukpt.StringUtil;
import com.iisysgroup.poslib.commons.emv.EmvCard;
import com.iisysgroup.poslib.host.Host;
import com.iisysgroup.poslib.host.HostInteractor;
import com.iisysgroup.poslib.host.entities.ConnectionData;
import com.iisysgroup.poslib.host.entities.KeyHolder;
import com.iisysgroup.poslib.host.entities.TransactionResult;
import com.iisysgroup.poslib.utils.TransactionData;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import org.jpos.iso.ISOMsg;

import java.net.SocketTimeoutException;
import java.util.List;

public class ReversalCommunicator {
    protected static final String posDataCode = "510101511344101";
    protected static final String posEntryMode = "051";
    protected static final String posConditionCode = "00";
    protected static final String posPinCaptureMode = "12";
    protected static final String amountTransactionFee = "D00000000";
    protected Context context;
    protected ConnectionData connectionData;
    protected TransactionData transactionData;
    protected IsoTimeManager timeMgr = new IsoTimeManager();
    protected TransactionResult transactionResult;
    protected String amount;
    protected String track2Data;
    protected String iccData;
    protected String pan;
    protected String acquiringInstitutionIdCode;
    protected String merchantType;
    protected String merchantNameLocation;
    protected String cardAcceptorIdCode;
    protected String serviceCode;
    protected String terminalID;
    protected String retrievalRefNumber;
    protected String fromAccountType;
    protected String processingCode;
    protected String expiryDate;
    protected String panSequenceNumber = "001";
    protected String transmissionDateTime;
    protected String timeLocalTransaction;
    protected String dateLocalTransaction;
    protected String transactionCurrencyCode = "566";
    protected String sequenceNumber;
    protected KeyHolder keysh;
    protected String host;



    private String mSessionKey, mPinKey;

    public ReversalCommunicator(Context cont, TransactionData emvCard, ConnectionData connectingData, KeyHolder keyHold,TransactionResult transactionRes){
        this.context = cont;
        this.connectionData = connectingData;
        this.transactionData = emvCard;
        this.keysh = keyHold;
        this.amount = this.transactionData.getInputData().getAmount() + "";
        this.track2Data = this.transactionData.getEmvCard().getTrack2Data();
        this.iccData = this.transactionData.getEmvCard().getIccData();
        this.fromAccountType = this.transactionData.getInputData().getAccountType().ordinal() + "0";
        this.pan = TlvHelper.getPan(this.track2Data);
        this.expiryDate = TlvHelper.getExpiryDate(this.track2Data);
        this.serviceCode = TlvHelper.getServiceCode(this.track2Data);
        this.acquiringInstitutionIdCode = TlvHelper.getAcquiringInstitutionIdCode(this.track2Data);
        this.merchantType = this.transactionData.getConfigData().getConfigData("08004").toString();
        this.merchantNameLocation = this.transactionData.getConfigData().getConfigData("52040").toString();
        this.cardAcceptorIdCode = this.transactionData.getConfigData().getConfigData("03015").toString();
        this.transactionCurrencyCode = this.transactionData.getConfigData().getConfigData("05003").toString();
        this.terminalID = this.connectionData.getTerminalID();
        this.pullPanSequenceNumber();
        this.track2Data = this.track2Data.replace("F", "").replace("f", "");
        this.transmissionDateTime = this.timeMgr.getLongDate();
        this.timeLocalTransaction = this.timeMgr.getTime();
        this.dateLocalTransaction = this.timeMgr.getShortDate();
        this.retrievalRefNumber = Utility.padLeft(this.transmissionDateTime, 12, '0');
        this.sequenceNumber = this.retrievalRefNumber.substring(6);
//        this.transactionResult = new TransactionResult();
      Log.i("reversal transactionRes >>>>", new Gson().toJson(transactionRes));

        this.transactionResult = transactionRes;

        host = ((App)((Activity)context).getApplication()).getHostKey();
        String decryptedKey = "";
        Log.d("reverse host",host);

        this.mSessionKey = keyHold.getSessionKey();
        this.mPinKey = keyHold.getPinKey();

    }

    private void pullPanSequenceNumber() {
        int index = iccData.toUpperCase().indexOf("5F34");
        if (index >= 0)
            panSequenceNumber = iccData.substring(index + 6, index + 8);
    }

    public  boolean rollBackTransaction() {

        try {
            String processingCode = Constants.IsoTransactionType.REVERSAL + fromAccountType + "00";

            String messageReasonCode = IsoReversalProcessData.ReversalReasonCode.TIMEOUT_AWAITING_RESPONSE.toString();

            String amount = (transactionData.getInputData().getAmount()
                    + transactionData.getInputData().getAdditionalAmount()) + "";

            String acqCode = transactionData.getEmvCard().getTrack2Data().substring(0, 6).toUpperCase();
            acqCode = Utility.padLeft(acqCode, 11, '0');

            String originalDataElements = "0200" + Utility.padLeft(this.sequenceNumber + "", 6, '0')
                    + this.transactionResult.isoTransmissionDateTime
                    + acqCode + "00000000000";


            IsoMessage isoMsge = new IsoMessage();
            isoMsge.setType(0x420);
//            isoMsge.setType(0x200);
            isoMsge.setField(2, new IsoValue<String>(IsoType.LLVAR, pan));
            isoMsge.setField(3, new IsoValue<String>(IsoType.ALPHA, processingCode, 6));
            isoMsge.setField(4, new IsoValue<String>(IsoType.ALPHA, Utility.padLeft(amount, 12, '0'), 12));
            isoMsge.setField(7, new IsoValue<String>(IsoType.ALPHA, transmissionDateTime, 10));
            isoMsge.setField(11, new IsoValue<String>(IsoType.NUMERIC, Utility.padLeft(sequenceNumber + "", 6, '0'), 6));
            isoMsge.setField(12, new IsoValue<String>(IsoType.ALPHA, timeLocalTransaction, 6));
            isoMsge.setField(13, new IsoValue<String>(IsoType.ALPHA, dateLocalTransaction, 4));
            isoMsge.setField(14, new IsoValue<String>(IsoType.ALPHA, expiryDate, 4));
            isoMsge.setField(18, new IsoValue<String>(IsoType.ALPHA, merchantType, 4));
            isoMsge.setField(22, new IsoValue<String>(IsoType.ALPHA, posEntryMode, 3));
            isoMsge.setField(23, new IsoValue<String>(IsoType.ALPHA, getPanSequenceNumber(), 3));
            isoMsge.setField(25, new IsoValue<String>(IsoType.ALPHA, posConditionCode, 2));
            isoMsge.setField(26, new IsoValue<String>(IsoType.ALPHA, posPinCaptureMode, 2));
            isoMsge.setField(28, new IsoValue<String>(IsoType.ALPHA, amountTransactionFee, 9));
            isoMsge.setField(32, new IsoValue<String>(IsoType.LLVAR, acquiringInstitutionIdCode));
            isoMsge.setField(35, new IsoValue<String>(IsoType.LLVAR, track2Data));
            isoMsge.setField(37, new IsoValue<String>(IsoType.ALPHA, retrievalRefNumber, 12));
            isoMsge.setField(40, new IsoValue<String>(IsoType.ALPHA, serviceCode, 3));
            isoMsge.setField(41, new IsoValue<String>(IsoType.ALPHA, terminalID, 8));
            isoMsge.setField(42, new IsoValue<String>(IsoType.ALPHA, cardAcceptorIdCode, 15));
            isoMsge.setField(43, new IsoValue<String>(IsoType.ALPHA, merchantNameLocation, 40));
            isoMsge.setField(49, new IsoValue<String>(IsoType.ALPHA, transactionCurrencyCode, 3));

            String pinBlock =  getDecryptedPinBlock(transactionData.getEmvCard().getPinInfo(),transactionData.getKeyHolder());
            if (!pinBlock.isEmpty())
                isoMsge.setField(52, new IsoValue<String>(IsoType.ALPHA, pinBlock, 16));

            isoMsge.setField(55, new IsoValue<String>(IsoType.LLLVAR, iccData));

            isoMsge.setField(123, new IsoValue<String>(IsoType.LLLVAR, "511201513144001"));
            isoMsge.setField(128, new IsoValue<String>(IsoType.ALPHA, "", 40));




            isoMsge.setField(56, new IsoValue<String>(IsoType.LLLVAR, messageReasonCode));
            isoMsge.setField(90, new IsoValue<String>(IsoType.ALPHA, originalDataElements, 42));
            isoMsge.setField(95, new IsoValue<String>(IsoType.ALPHA, "000000000000000000000000D00000000D00000000", 42));


            String isoMessage = new String(isoMsge.writeData()).trim();
            String hash;

//            String sessionKey=getSessionKey();

            if (mSessionKey == null){
                String sessionKey=getSessionKey();
                Log.d("reversal sessionKey  >>>>>>",sessionKey);

                hash = TripleDES.generateHash256Value(isoMessage,getSessionKey());
            } else {
                hash = TripleDES.generateHash256Value(isoMessage, keysh.getSessionKey());
                Log.d("reversal sessionKey  >>>>>>",mSessionKey);

            }



            Log.d("reversal isoMessage  >>>>>>",isoMessage);


            Log.d("reversal hash  >>>>>>",hash);


            isoMessage = isoMessage + hash;

            Log.d("reversal isoMessage + hash  >>>>>>",isoMessage);


            String response = IsoTransactionExecutor.execute(context, IsoAdapter.prepareByteStream(isoMessage.getBytes("UTF-8")), connectionData);


            Log.d("reversal com iso response  >>>>>>",response);

            ISOMsg isoMsg = new IsoAdapter(context).processISOBitStream(response);

            String responseCode = IsoAdapter.getResponseDataFromIndex(isoMsg, Constants.RESPONSE_CODE);
            transactionResult.responseCode = "06"; //It's a roll back, so set response code to be Error(06)

            if (responseCode.equals("00")) {
                Log.d("reversal com request ok returning true  >>>>>>",responseCode.toString());

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("reversal com exception >>>>>",e.toString());

        }

        return false;
    }

    protected String getPanSequenceNumber() {
        return Utility.padLeft(panSequenceNumber, 3, '0');
    }

    protected String getDecryptedPinBlock(EmvCard.PinInfo pinInfo, KeyHolder keyHolder) {
        String reEncryptedPinBlock = "";

        try {
            if (pinInfo == null) {
                return reEncryptedPinBlock;
            } else {
                String pinKey;
                String decryptingKey;
                if (this.mPinKey == null) {
                    if (pinInfo.getPinBlock() != null) {
                        pinKey = this.decryptKeyWithMasterKey(keyHolder.getPinKey(), keyHolder.getMasterKey(), keyHolder.isTestPlatform());
                        decryptingKey = PosvasKeyProcessor.decryptWithTransportKey(StringUtil.toHexString(pinInfo.getKey()));
                        String clearPinBlock = TripleDES.threeDesDecrypt(StringUtil.toHexString(pinInfo.getPinBlock()), decryptingKey);
                        reEncryptedPinBlock = Utility.tripleDesEncrypt(pinKey, clearPinBlock);
                    }

                    return reEncryptedPinBlock;
                } else {
                    pinKey = PosvasKeyProcessor.decryptWithTransportKey(StringUtil.toHexString(pinInfo.getKey()));
                    decryptingKey = TripleDES.threeDesDecrypt(StringUtil.toHexString(pinInfo.getPinBlock()), pinKey);
                    reEncryptedPinBlock = Utility.tripleDesEncrypt(this.mPinKey, decryptingKey);
                    return reEncryptedPinBlock;
                }
            }
        } catch (Exception var7) {
            throw new RuntimeException(var7);
        }
    }

    protected String getSessionKey(){
        if (mSessionKey == null){
            KeyHolder keyHolder = transactionData.getKeyHolder();
            return decryptKeyWithMasterKey(keyHolder.getSessionKey(), keyHolder.getMasterKey(),
                    keyHolder.isTestPlatform());
        } else {

            return mSessionKey;
        }
    }

    protected String decryptKeyWithMasterKey(String encryptedKey, String encryptedMasterKey, boolean isTestPlaform){
        try {
//            String  host = ((App)((Activity)context).getApplication()).getHostKey();
            String decryptedKey = "";
//            Log.d("reverse host",host);
            switch (host){

                case  "TAMS" :
//                    Tam.decryptKey(encryptedKey, decryptMasterKey(encryptedMasterKey, isTestPlaform));

                    break;
                case "POSVAS" :
                {
                    Log.d("reverse keys","enkey "+encryptedKey + "tmk "+encryptedMasterKey);
                    decryptedKey= PosvasKeyProcessor.decryptKey(encryptedKey, decryptMasterKey(encryptedMasterKey, isTestPlaform));
                    Log.d("sess", decryptedKey);
                    break;
                }

                     default:
                         decryptedKey= GtmsKeyProcessor.decryptKey(encryptedKey, decryptMasterKey(encryptedMasterKey, isTestPlaform));

            }
//            "TAMS" -> TamsHost(this)
//            "POSVAS" -> PosvasHost(this)
//            else -> GtmsHost(this)




            return  decryptedKey;

        } catch (Exception e) {
            Log.d("reverse exp",e.toString());
            e.printStackTrace();
            return "";
        }
    }

    protected String decryptMasterKey(String encryptedMasterKey, boolean isTestPlatform){

            try {
                String dKey = "";
                switch (host){

                    case  "TAMS" :
//                    Tam.decryptKey(encryptedKey, decryptMasterKey(encryptedMasterKey, isTestPlaform));

                        break;
                    case "POSVAS" :
                        dKey = PosvasKeyProcessor.getMasterKey(encryptedMasterKey, isTestPlatform);
                        break;

                    default:
                        dKey = GtmsKeyProcessor.getMasterKey(encryptedMasterKey, isTestPlatform);

                }
                return dKey;

            } catch (Exception e) {
                Log.d("reverse master",e.toString());
                e.printStackTrace();
                return "";
            }

    }

}
