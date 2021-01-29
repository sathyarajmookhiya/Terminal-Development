package uk.co.transaxiom.android.txandroidlib.load;

import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import uk.co.transaxiom.android.txandroidlib.Utils;
import uk.co.transaxiom.android.txandroidlib.card.BaseTransaction;
import uk.co.transaxiom.android.txandroidlib.card.DisplayTransaction;
import uk.co.transaxiom.android.txandroidlib.card.nCounterKey;
import uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu.APDUFactory;
import uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu.CommandAPDU;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.terminal.common.AppletConstants;
import uk.co.transaxiom.terminal.common.NcResponseAPDU;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;
import uk.co.transaxiom.terminal.payment.entity.FileControlInformation;
import uk.co.transaxiom.terminal.payment.entity.Purse;


public class AndroidLoadWrapper {

    private final byte[] selectTestAppCmd = {0x00, (byte) 0xA4, 0x04, 0x00, 0x09, (byte) 0xA0, 0x00, 0x00, 0x05, 0x44, (byte) 0x0F, 0x01, (byte) 0xFF, 0x01};

    public static final String fakeLoadFci = "4F09A0000005440F01FF02851E10300001BC3FFCB0FF0FDCF00CDE555DCD1235EE81C794654742500025009000";

    private final nCounterKey[] liveKeys = new nCounterKey[]{ //Issuer KeyID + Issuer key; always the same for all n-Counters from a given Issuer
            new nCounterKey(new byte[]{(byte) 0xFF, 0x00, 0x00, 0x01}, new byte[]{(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x00}),
            new nCounterKey(new byte[]{(byte) 0xFF, 0x00, 0x00, 0x02}, new byte[]{(byte) 0xAA, (byte) 0x55, (byte) 0xCC, (byte) 0x66, (byte) 0xEE, (byte) 0x77, (byte) 0x00, (byte) 0x88, (byte) 0x22, (byte) 0x99, (byte) 0x44, (byte) 0xAA, (byte) 0x66, (byte) 0xBB, (byte) 0x88, (byte) 0xCC}),
            new nCounterKey(new byte[]{(byte) 0xFF, 0x00, 0x00, 0x03}, new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF}),
            new nCounterKey(new byte[]{(byte) 0xFF, 0x00, 0x00, 0x04}, new byte[]{(byte) 0x9A, (byte) 0xAB, (byte) 0xBC, (byte) 0xCD, (byte) 0xDE, (byte) 0xEF, (byte) 0xF0, (byte) 0x01, (byte) 0x12, (byte) 0x23, (byte) 0x34, (byte) 0x45, (byte) 0x56, (byte) 0x67, (byte) 0x78, (byte) 0x89})};


    private static AndroidLoadWrapper instance = new AndroidLoadWrapper();

    private AndroidLoadWrapper() {
    }

    public static AndroidLoadWrapper getInstance() {

        return instance;
    }

    /**
     * Add value to the card. Used to reset a card by erasing the transaction store
     *
     * @param card:      the current card
     * @param loadValue: the value to be loaded in pennies
     * @param reset:     true or false whether this is a resetting load or not
     * @return Boolean true or false whether the operation was successful
     * @throws Exception
     */
    public boolean addValueNewWay(IsoDep card, long loadValue, boolean reset, boolean isBcd) throws Exception {
//        byte[] loadingKey = { (byte) 0xC2, (byte) 0x6C, (byte) 0x43, (byte) 0xA0, (byte) 0x4B, (byte) 0x1C, (byte) 0x31, (byte) 0x73,
//		      (byte) 0xBf, (byte) 0x22, (byte) 0xA7, (byte) 0x91, (byte) 0x55, (byte) 0xCB, (byte) 0x81, (byte) 0xA5 };

        card.setTimeout(10000);
//		String tmp = "8527D84C7464583A3EC5B34F43E32E7C";
//		String tmp = "B74AC1F665A1078615CD1FBE85AE19C2";
        String tmp = "BB7E0091A87E3286DF6AD0D422133C45";
        byte[] loadKeyTest = BinaryUtils.decode(tmp);

        byte[] loadAppletAID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x05, 0x44, 0x0F, 0x10, (byte) 0xFF, 0x01};

        APDUFactory factory = APDUFactory.getInstance();
        CommandAPDU cmd = factory.createCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x04, loadAppletAID);
        //1. select load applet
        Log.v("AddNewValueNewWay", "Send:" + BinaryUtils.encode(cmd.getData()));
        byte[] resp = card.transceive(cmd.getData());
        Log.v("AddNewValueNewWay", "Recv:" + BinaryUtils.encode(resp));
        NcResponseAPDU response = new NcResponseAPDU(resp);
        if (!response.isError()) {
            //2. extract FCI info
            LoadFileControlInformation loadFCI = new LoadFileControlInformation(response.getData());

            Log.v("AddNewValueNewWay", "New load cmd response:");
            Log.v("AddNewValueNewWay", response.getHexString());
            Log.v("AddNewValueNewWay", loadFCI.toString());

            //3. sign transaction ID with loading key
            SecretKey sKey1 = new SecretKeySpec(loadKeyTest, "AES");
            IvParameterSpec iv1 = new IvParameterSpec(new byte[16]);
            Cipher cipher1 = Cipher.getInstance("AES/CBC/NoPadding");
            cipher1.init(Cipher.ENCRYPT_MODE, sKey1, iv1);

            byte[] signingKey = cipher1.doFinal(loadFCI.getTransactionId());

            //4. build command data to sign: currency code (3 bytes) + amount (3 bytes) + nonce (4 bytes) + padding (6 bytes)
            byte[] commandData = new byte[16];

            byte[] Amount = new byte[11];
            byte[] loadAmount = new byte[8];
            if (isBcd) {
                Amount = new byte[6];
                loadAmount = new byte[3];
                loadAmount = BinaryUtils.intToBCD3BytesArray((int) loadValue);
            } else {
                loadAmount = BinaryUtils.getLongAs64Bits(loadValue);
            }
//	        byte[] currencyCode = loadFCI.getCurrencyCode();
//	        byte[] currencyCode = new byte[]{0x4F, 0x4D, 0x43}; //OMC
//	        byte[] currencyCode = new byte[]{0x41, 0x49, 0x44}; //AID
            byte[] currencyCode = new byte[]{0x47, 0x42, 0x50}; //GBP
            System.arraycopy(currencyCode, 0, Amount, 0, 3);


            System.arraycopy(loadAmount, 0, Amount, 3, loadAmount.length);

            System.arraycopy(Amount, 0, commandData, 0, Amount.length);

            System.arraycopy(loadFCI.getNonce(), 0, commandData, Amount.length, 4);

            if (reset) {
                commandData[Amount.length + 4] = 0x00;
            } else {
                commandData[Amount.length + 4] = 0x01;
            }

            if (isBcd) {
                commandData[11] = 0x05;
                commandData[12] = 0x05;
                commandData[13] = 0x05;
                commandData[14] = 0x05;
                commandData[15] = 0x05;
            }


            //5. sign command data with signing key
            SecretKey sKey2 = new SecretKeySpec(signingKey, "AES");
            IvParameterSpec iv2 = new IvParameterSpec(new byte[16]);
            Cipher cipher2 = Cipher.getInstance("AES/CBC/NoPadding");
            cipher2.init(Cipher.ENCRYPT_MODE, sKey2, iv2);

            Log.v("AddValueNewWay", "Load command: " + Utils.getHexString(commandData));

            byte[] signedCommand = cipher2.doFinal(commandData);
            Log.v("AddValueNewWay", "Load command (signed): " + Utils.getHexString(signedCommand));

            //6. build the command APDU
            byte[] loadingKeyId = new byte[]{(byte) 0x87, (byte) 0x33, (byte) 0x2A, (byte) 0xDD};
//			byte[] loadingKeyId = new byte[]{0x53, (byte) 0xEF, (byte) 0xA5, (byte) 0x82};
//			byte[] loadingKeyId = new byte[]{0x50, (byte) 0xDA, (byte) 0x18, (byte) 0x11};
            byte[] command = new byte[20];
            System.arraycopy(loadingKeyId, 0, command, 0, loadingKeyId.length);
            System.arraycopy(signedCommand, 0, command, 4, 16);

            //7. send APDU to card and brace for impact!
            NcResponseAPDU loadResponse = sendLoadMessage(card, command);
            if (!loadResponse.isError()) {

                Log.v("AddValueNewWay", "Load cmd response:");
                Log.v("AddValueNewWay", loadResponse.getHexString());
                byte[] cardResponse = parseLoadResponse(loadResponse.getData());
                //8. decode response
                Cipher decryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
                decryptCipher.init(Cipher.DECRYPT_MODE, sKey2, iv2);
                byte[] appletResponse = decryptCipher.doFinal(cardResponse);
                //9. parse 4 bytes nonce + 11 bytes (MSB from Transaction ID) + status byte (0x00 if OK!)
                Log.v("AddValueNewWay", "LoadResponse Message - decyphered: " + Utils.getHexString(appletResponse));
                byte[] responseNonce = new byte[4];
                byte[] responseTxID = new byte[10];
                byte[] status = new byte[]{appletResponse[14], appletResponse[15]};

                System.arraycopy(appletResponse, 0, responseNonce, 0, 4);
                System.arraycopy(appletResponse, 4, responseTxID, 0, 10);
                byte[] txID = new byte[11];
                System.arraycopy(loadFCI.getTransactionId(), 0, txID, 0, 10);//just copy 11 MSBs
                Log.w("CHECKINGRESPONSESTATUS", "status:" + BinaryUtils.encode(status));
                if (Arrays.equals(status, new byte[]{0x00, 0x16})) {
                    Log.v("AddValueNewWay", "LOAD VALUE SUCCESFULL");
                    return true;
                }

            } else {
                Log.v("AddValueNewWay", loadResponse.getMsg());
                Log.v("AddValueNewWay", loadResponse.getHexString());
            }
        } else {
            Log.v("AddValueNewWay", response.getMsg());
            Log.v("AddValueNewWay", response.getHexString());
        }
        return false;
    }


    public boolean blockPurse(IsoDep card) throws Exception {
        AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
        FileControlInformation purse = wrapper.getPurseFCI(card, (byte) 0);
        Log.d("wewewe", "Purse: ");
        Log.d("wewewe", " = " + purse.getLongBalance());


        byte[] payload = {0x00, 0x00, 0x00, 0x00,
                0x65, 0x08, 0x65, 0x08,
                0x00, 0x01, 0x02, 0x03,
                0x04, 0x05, 0x06, 0x07};

        String key6 = "DD2C6FECA4BC9315C332F5D500798B9F";
        String keyId = "3A698943";
        byte[] signingKey = BinaryUtils.decode(key6);
        byte[] signingKeyId = BinaryUtils.decode(keyId);

        //3. sign transaction ID with loading key
        SecretKey sKey1 = new SecretKeySpec(signingKey, "AES");
        IvParameterSpec iv1 = new IvParameterSpec(new byte[16]);
        Cipher cipher1 = Cipher.getInstance("AES/CBC/NoPadding");
        cipher1.init(Cipher.ENCRYPT_MODE, sKey1, iv1);

        byte[] signedPayload = cipher1.doFinal(payload);
        byte[] command = new byte[24];
        command[0] = (byte) 0xBD;
        command[1] = (byte) 0xE7;
        System.arraycopy(signingKeyId, 0, command, 2, signingKeyId.length);

        command[6] = 0x65;
        command[7] = 0x08;
        System.arraycopy(signedPayload, 0, command, 8, signedPayload.length);

        APDUFactory factory = APDUFactory.getInstance();
        CommandAPDU cmd = factory.createCommand((byte) 0xD4, (byte) 0xEE, (byte) 0x00, (byte) 0x00, command);

        Log.d("wewewe", BinaryUtils.encode(cmd.getData()));
        byte[] response = card.transceive(cmd.getData());
        Log.d("wewewe", BinaryUtils.encode(response));

        return false;
    }


    public boolean unblockPurse(IsoDep card) throws Exception {
        AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
        FileControlInformation purse = wrapper.getPurseFCI(card, (byte) 1);


        byte[] payload = {0x00, 0x00, 0x00, 0x00,
                0x5B, (byte) 0x94, 0x5B, (byte) 0x94,
                0x00, 0x01, 0x02, 0x03,
                0x04, 0x05, 0x06, 0x07};

        String key6 = "DD2C6FECA4BC9315C332F5D500798B9F";
        String keyId = "3A698943";
        byte[] signingKey = BinaryUtils.decode(key6);
        byte[] signingKeyId = BinaryUtils.decode(keyId);

        //3. sign transaction ID with loading key
        SecretKey sKey1 = new SecretKeySpec(signingKey, "AES");
        IvParameterSpec iv1 = new IvParameterSpec(new byte[16]);
        Cipher cipher1 = Cipher.getInstance("AES/CBC/NoPadding");
        cipher1.init(Cipher.ENCRYPT_MODE, sKey1, iv1);

        byte[] signedPayload = cipher1.doFinal(payload);
        byte[] command = new byte[24];
        command[0] = (byte) 0xBD;
        command[1] = (byte) 0xE7;
        System.arraycopy(signingKeyId, 0, command, 2, signingKeyId.length);

        command[6] = 0x5B;
        command[7] = (byte) 0x94;
        System.arraycopy(signedPayload, 0, command, 8, signedPayload.length);

        APDUFactory factory = APDUFactory.getInstance();
        CommandAPDU cmd = factory.createCommand((byte) 0xD4, (byte) 0xEE, (byte) 0x00, (byte) 0x00, command);

        Log.d("wewewe", BinaryUtils.encode(cmd.getData()));
        byte[] response = card.transceive(cmd.getData());
        Log.d("wewewe", BinaryUtils.encode(response));

        return false;
    }

//	public boolean addValueNewWay(IsoDep card, int loadValue, boolean reset) throws Exception {
////      byte[] loadingKey = { (byte) 0xC2, (byte) 0x6C, (byte) 0x43, (byte) 0xA0, (byte) 0x4B, (byte) 0x1C, (byte) 0x31, (byte) 0x73,
////		      (byte) 0xBf, (byte) 0x22, (byte) 0xA7, (byte) 0x91, (byte) 0x55, (byte) 0xCB, (byte) 0x81, (byte) 0xA5 };
//		
////      byte[] omLoadingKey = { (byte) 0xb7, 0x4a, (byte) 0xc1, (byte) 0xf6, 0x65, (byte) 0xa1, 0x07, (byte) 0x86, 0x15, (byte) 0xcd,
////      		0x1f, (byte) 0xbe, (byte) 0x85, (byte) 0xae, 0x19, (byte) 0xc2};
//		
//		card.setTimeout(10000);
//      
//      byte[] loadAppletAID = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x05, 0x44, 0x0F, 0x10, (byte) 0xFF, 0x01};
//      
//      APDUFactory factory = APDUFactory.getInstance();
//      CommandAPDU cmd = factory.createCommand((byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x04, loadAppletAID);
//		//1. select load applet
//      Log.v("AddNewValueNewWay", "Send:"+BinaryUtils.encode(cmd.getData()));
//      byte[] resp = card.transceive(cmd.getData());
//      Log.v("AddNewValueNewWay", "Recv:"+BinaryUtils.encode(resp));
//		ResponseAPDU response = new ResponseAPDU(resp);
//		if (!response.isError())
//		{
//			//2. extract FCI info
//			LoadFileControlInformation loadFCI = new LoadFileControlInformation(response.getData());
//			byte[] loadCmd = generateLoadAPDU(loadFCI, loadValue);
//			Log.v("AddNewValueNewWay", "Send:"+BinaryUtils.encode(loadCmd));
//			byte[] loadResp = card.transceive(loadCmd);
//			Log.v("AddNewValueNewWay", "Recv:"+BinaryUtils.encode(loadResp));
//			
//			ResponseAPDU loadResponse = new ResponseAPDU(loadResp);
//			if (!loadResponse.isError()) {
//
//				Log.v("AddValueNewWay", "Load cmd response:");
//				Log.v("AddValueNewWay", loadResponse.getHexString());
//				byte[] cardResponse = parseLoadResponse(loadResponse.getData());
//				//8. decode response
//				
//			    Cipher decryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
//			    decryptCipher.init(Cipher.DECRYPT_MODE, sKey2, iv2);
//			    byte[] appletResponse = decryptCipher.doFinal(cardResponse);
//				//9. parse 4 bytes nonce + 11 bytes (MSB from Transaction ID) + status byte (0x00 if OK!)
//			    Log.v("AddValueNewWay", "LoadResponse Message - decyphered: " + Utils.getHexString(appletResponse));
//			    byte[] responseNonce = new byte[4];
//			    byte[] responseTxID = new byte[10];
//			    byte[] status = new byte[]{appletResponse[14], appletResponse[15]};
//			    
//			    System.arraycopy(appletResponse, 0, responseNonce, 0, 4);
//			    System.arraycopy(appletResponse, 4, responseTxID, 0, 10);
//			    byte[] txID = new byte[11];
//			    System.arraycopy(loadFCI.getTransactionId(), 0, txID, 0, 10);//just copy 11 MSBs
//			    Log.w("CHECKINGRESPONSESTATUS", "status:"+BinaryUtils.encode(status));
//			    if(Arrays.equals(status, new byte[]{0x00, 0x16})) {
//		    			Log.v("AddValueNewWay", "LOAD VALUE SUCCESFULL");
//		    			return true;
//		    		}
//			    
//			}
//			else {
//				Log.v("AddValueNewWay", loadResponse.getMsg());
//				Log.v("AddValueNewWay", loadResponse.getHexString());
//			}
//		}
//		else {
//			Log.v("AddValueNewWay", response.getMsg());
//			Log.v("AddValueNewWay", response.getHexString());
//		}
//		return false;
//	}


    /**
     * Retrieve the load response from the response APDU sent by the card
     *
     * @param data: the response APDU sent by the card
     * @return Load Response as a byte array
     */
    private byte[] parseLoadResponse(byte[] data) {
        byte[] result = new byte[AppletConstants.LOAD_RESPONSE_LENGTH];
        byte[] id = new byte[]{data[0], data[1]};
        int offset = 2;

        if (Arrays.equals(id, AppletConstants.LOAD_RESPONSE_ID)) {
            System.arraycopy(data, offset, result, 0, AppletConstants.LOAD_RESPONSE_LENGTH);
        }
        return result;
    }

    /**
     * Read the transaction store of a specific Tx Purse from the card
     *
     * @param card:  the current card
     * @param infix: the Purse number to read from
     * @return List of DisplayTransaction ready to be displayed
     */
    public List<DisplayTransaction> readAllRecords(IsoDep card, byte infix) {
        Log.v("TXTerminalWrapper", "Reading all records");
        List<DisplayTransaction> result = new ArrayList<DisplayTransaction>();
        try {
            if (!card.isConnected())
                card.connect();
            card.setTimeout(5000);
            List<BaseTransaction> transactionStore = readRecordsFromCard(card, infix);
            if (!transactionStore.isEmpty()) {
                Log.v("TXTerminalWrapper", "transactionStore not empty!");
                result = sortTransactions(transactionStore);
                return result;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Send ReadRecord command to the card
     *
     * @param card: the current card
     * @return ResponseAPDU containing the record encoded as a byte array
     */
    public NcResponseAPDU ReadRecord(IsoDep card) {
        byte[] cmd = {0x00, (byte) 0x82, 0x00, 0x02, 0x00};
        try {
            NcResponseAPDU result = Transceive(card, "ReadRecord", cmd, 3);
            Log.d("READRECORDAPDU", "response = " + result.getMsg());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("NCApplet", "Exception: " + e.getMessage() + " in ReadRecord \n");
            return null;
        }
    }

    /**
     * Retrieve all the records from the transaction store of a specific purse
     *
     * @param card:  the current card
     * @param infix: the Purse number to read from
     * @return List of BaseTransaction to be parsed into transactions ready to be displayed
     * @throws Exception
     */
    private List<BaseTransaction> readRecordsFromCard(IsoDep card, byte infix) throws Exception {
        AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
        List<BaseTransaction> transactionStore = new ArrayList<BaseTransaction>();

        boolean recordAvailable = true;
        byte[] arrayOfBytes = new byte[3];
        Purse purse = wrapper.getCardSpecificPurseDetails(card, infix);
        ;
        NcResponseAPDU response = new NcResponseAPDU(arrayOfBytes);
        if (purse != null) {
            while (recordAvailable) {
                response = ReadRecord(card);
                if (!response.isError()) {
                    BaseTransaction record = new BaseTransaction(response.getData(), 0, purse.isBcdPurse());

                    transactionStore.add(record);
                    Log.v("HandleApplet", "New record added: " + record.getTransactionNumber());
                }
                if (Arrays.equals(response.getStatus(), NcResponseAPDU.RECORD_NOT_FOUND))
                    recordAvailable = false;
            }
        }
        return transactionStore;
    }

    public byte[] generateLoadAPDU(LoadFileControlInformation fci, int amount) throws Exception {

        String tmp = "B74AC1F665A1078615CD1FBE85AE19C2";
        byte[] loadKeyTest = BinaryUtils.decode(tmp);
        byte[] apdu = new byte[27];

        byte[] signingKey = encrypt(loadKeyTest, fci.getTransactionId());

        byte[] commandData = new byte[16];
        byte[] amountBytes = new byte[6];

        byte[] currencyCode = fci.getCurrencyCode();

        System.arraycopy(currencyCode, 0, amountBytes, 0, 3);

        byte[] loadAmount = new byte[3];
        if (amount > 0) {
            loadAmount = BinaryUtils.intToBCD3BytesArray(amount);
        }
        System.arraycopy(loadAmount, 0, amountBytes, 3, 3);
        int offset = 0;
        System.arraycopy(amountBytes, 0, commandData, offset, amountBytes.length);
        offset += amountBytes.length;
        System.arraycopy(fci.getNonce(), 0, commandData, offset, fci.getNonce().length);
        offset += fci.getNonce().length;
        byte[] commandInfix = new byte[]{0x00, 0x05, 0x05, 0x05, 0x05, 0x05};
        System.arraycopy(commandInfix, 0, commandData, offset, commandInfix.length);
        byte[] signedCommand = encrypt(signingKey, commandData);

        offset = 0;
        byte[] commandPrefix = {(byte) 0xD4, (byte) 0xEE, 0x00, 0x00, 0x16, 0x34, (byte) 0x87};
        System.arraycopy(commandPrefix, 0, apdu, offset, commandPrefix.length);
        offset += commandPrefix.length;

        byte[] loadingKeyId = new byte[]{0x50, (byte) 0xDA, (byte) 0x18, (byte) 0x11};
        System.arraycopy(loadingKeyId, 0, apdu, offset, loadingKeyId.length);
        offset += loadingKeyId.length;
        System.arraycopy(signedCommand, 0, apdu, offset, signedCommand.length);

        return apdu;
    }

    private byte[] encrypt(byte[] key, byte[] data) throws Exception {

        SecretKey secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] result = cipher.doFinal(data);

        return result;
    }

    /**
     * Send the load message to the card
     *
     * @param card:    the current card
     * @param command: the load command
     * @return ResponseAPDU containing the load response sent back from the card
     */
    public NcResponseAPDU sendLoadMessage(IsoDep card, byte[] command) {
        byte[] prefix = {(byte) 0xD4, (byte) 0xEE, 0x00, 0x00, 0x16, 0x34, (byte) 0x87};
        byte[] cmd = new byte[prefix.length + command.length];
        System.arraycopy(prefix, 0, cmd, 0, prefix.length);
        System.arraycopy(command, 0, cmd, prefix.length, 20);
        try {

            card.setTimeout(50000);
            NcResponseAPDU result = Transceive(card, "LoadValue", cmd, 3);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("SendLoadMessage", "Exception: " + e.getMessage() + " in sendLoadMessage \n");
        }
        return null;
    }

    /**
     * Transceive a specific command to a card
     *
     * @param card:     the current card
     * @param cmdStr:   the name of the command
     * @param cmd:      the command as a byte array to send to the card
     * @param attempts: the number of attempts to make
     * @return ResponseAPDU containing the response sent back from the card
     * @throws Exception
     */
    private NcResponseAPDU Transceive(IsoDep card, String cmdStr, byte[] cmd, int attempts)
            throws Exception {
        //card.setTimeout(50000); //Sets the timeout for 1 second Prevents a LostTag error
        byte[] ret;
        int attempt = 1;
        Log.v("NCApplet", "\n=====================\n" + cmdStr);
        Log.v("NCApplet", "=====> \n" + Utils.getHexString(cmd, cmd.length));
        while (attempt < attempts + 1) {
            Log.v("NCApplet", String.format("Sending (attempt# %d): %s", attempt, Utils.bytes2hex(cmd)));
            try {
                ret = card.transceive(cmd);
                Log.d("AndroidTerminalWrapper", "response = " + BinaryUtils.encode(ret));
                // If the transceive actually works (ie it doesn't throw an
                // IOException) then we'll have some data that we can check.
                // Work on the basis that if there's an error in this response,
                // it isn't going to be fixed by sending the command again so
                // if the following wants to throw an exception, let it!
                //CheckResponse(cmdStr, ret);
                NcResponseAPDU response = new NcResponseAPDU(ret);
                if (CheckResponse(ret)) {
                    return response;
                } else
                    Log.v("NCApplet", "FAIL: " + Utils.getHexString(ret, ret.length));

            } catch (IOException e) {
                // Do nothing (ie go round the loop again)
                Log.v("NCApplet", e.getMessage());
            }
            attempt++;
        }
        // If we get here, it's 'cos the transceive failed attempts times.
        // Time to give up then!
        throw new Exception("Failed to send " + cmdStr);
    }

    /**
     * Check the if the response sent from the card is not an error
     *
     * @param resp: the apdu sent by the card as a byte array
     * @return Boolean true or false whether the response is valid or is an error
     */
    private boolean CheckResponse(byte[] resp) {
        boolean result = false;
        //byte[] tmp = new byte[rep.length];
        if (resp.length > 1) {
            NcResponseAPDU response = new NcResponseAPDU(resp);
            if (Arrays.equals(response.getStatus(), NcResponseAPDU.OK)) {
                return true;
            }
            return true;
        }
        return result;
    }

    /**
     * Parse the List of BaseTransaction retrieved from the card into ready to be displayed transactions
     *
     * @param transactionStore: List of BaseTransaction retrieved from the card
     * @return List of DisplayTransaction to be displayed
     */
    private ArrayList<DisplayTransaction> sortTransactions(List<BaseTransaction> transactionStore) {
        Log.v("TXTerminalWrapper", "Sorting transactionStore!");
        ArrayList<DisplayTransaction> sortedTransactions = new ArrayList<DisplayTransaction>();
        ArrayList<DisplayTransaction> loads = new ArrayList<DisplayTransaction>();
        ArrayList<DisplayTransaction> payments = new ArrayList<DisplayTransaction>();

        for (int i = 0; i < transactionStore.size(); i++) {
            if (transactionStore.get(i).getRecType() == BaseTransaction.RT_STR_Load
                    || transactionStore.get(i).getRecType() == BaseTransaction.RT_NC_Load2) {
                Log.v("TXTerminalWrapper", "Found a load !");
                DisplayTransaction load = new DisplayTransaction(transactionStore.get(i), transactionStore.get(i).getAmount());
                loads.add(load);
            }
            if (transactionStore.get(i).getRecType() == BaseTransaction.RT_NC_Payment) {
                Log.v("TXTerminalWrapper", "Found a payment !");
                DisplayTransaction payment = new DisplayTransaction(transactionStore.get(i), transactionStore.get(i).getAmount());
                payments.add(payment);
            }
        }

        if (!loads.isEmpty()) {
            Log.v("TXTerminalWrapper", "Loads is not empty !");
            cleanLoads(loads);
            for (int i = 0; i < loads.size(); i++) {
                sortedTransactions.add(loads.get(i));
            }

        }
        if (!payments.isEmpty()) {
            Log.v("TXTerminalWrapper", "Payments is not empty !");
            cleanPayments(payments);
            for (int i = 0; i < payments.size(); i++) {
                sortedTransactions.add(payments.get(i));
            }
        }
        return sortedTransactions;
    }

    /**
     * Clean loads to get specific load value instead of accumulated value
     *
     * @param loads: list of loads to clean
     */
    private void cleanLoads(ArrayList<DisplayTransaction> loads) {
        Log.v("TXTerminalWrapper", "Cleaning loads...");
        for (int i = 0; i < loads.size(); i++) {
            String val1, val2;
            val1 = loads.get(i).getAmount();
            if ((i + 1) < loads.size()) {
                val2 = loads.get(i + 1).getAmount();
            } else {
                val2 = "0.00";
            }
            BigDecimal bd = new BigDecimal(val1);
            bd = bd.movePointRight(2);
            long tmp1 = bd.longValue();

            bd = new BigDecimal(val2);
            bd = bd.movePointRight(2);
            long tmp2 = bd.longValue();

            long tmp3 = tmp1 - tmp2;
            BigDecimal amount = new BigDecimal(tmp3).movePointLeft(2);
            loads.get(i).setAmount(amount.toString());
            Log.v("TXTerminalWrapper", "new Amount = " + amount.toString());
        }
    }

    /**
     * Clean the payments to get specific payment value instead of accumulated value
     *
     * @param payments: list of payments to clean
     */
    private void cleanPayments(ArrayList<DisplayTransaction> payments) {
        Log.v("TXTerminalWrapper", "Cleaning payments...");
        for (int i = 0; i < payments.size(); i++) {
            String val1, val2;
            val1 = payments.get(i).getAmount();
            if ((i + 1) < payments.size()) {
                val2 = payments.get(i + 1).getAmount();
            } else {
                val2 = "0.00";
            }
            Log.v("CleanPayments", "amount = " + val1 + " - " + val2);
            BigDecimal bd = new BigDecimal(val1);
            bd = bd.movePointRight(2);
            Log.v("CleanPayments", "big decimal 1 = " + bd);
            long tmp1 = bd.longValue();
            Log.v("CleanPayments", "tmp1 = " + tmp1);

            bd = new BigDecimal(val2);
            bd = bd.movePointRight(2);
            Log.v("CleanPayments", "big decimal 2 = " + bd);
            long tmp2 = bd.longValue();
            Log.v("CleanPayments", "tmp2 = " + tmp2);

            long tmp3 = tmp1 - tmp2;
            BigDecimal amount = new BigDecimal(tmp3).movePointLeft(2);

            payments.get(i).setAmount(amount.toString());
            Log.v("TXTerminalWrapper", "new Amount = " + amount.toString());
        }
    }
}
