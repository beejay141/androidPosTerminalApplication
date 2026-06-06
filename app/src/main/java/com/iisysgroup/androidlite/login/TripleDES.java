package com.iisysgroup.androidlite.login;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

/**
 * This class defines methods for encrypting and decrypting using the Triple DES
 * algorithm and for generating, reading and writing Triple DES keys. It also
 * defines a main() method that allows these methods to be used from the command
 * line.
 */
public class TripleDES {


    /**
     * Read a TripleDES secret key from a byte array
     */
    public static SecretKey readKey(byte[] rawkey) {

        // Read the raw bytes from the keyfile
        try {

            DESedeKeySpec keyspec = new DESedeKeySpec(rawkey);
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
            SecretKey key;
            key = keyfactory.generateSecret(keyspec);
            key = keyfactory.translateKey(key);
            return key;
        } catch (InvalidKeySpecException ex) {

            //LOG.error("Error reading key", ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {

            //LOG.error("Error reading key", ex);
            return null;
        } catch (InvalidKeyException ex) {

            //LOG.error("Error reading key", ex);
            return null;
        }
    }

    public static SecretKey readSingleKey(byte[] rawkey) throws
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException {

        // Read the raw bytes from the keyfile
        DESKeySpec keyspec = new DESKeySpec(rawkey);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyfactory.generateSecret(keyspec);
        return key;
    }

    public static int icclenght(String s) {


        int iLength = s.length();
        int iBuff = iLength / 2;


        //String fil =   String.format("%02d",iBuff);
        return iBuff;
    }

    public static byte[] hexStringToBytes(String s) {


        int iLength = s.length();
        int iBuff = iLength / 2;

        byte[] buff = new byte[iBuff];

        //if(s != null && !s.equals("")){
        ////System.out.println("Attempting Hex 2 Bin Conversion");

        int j = 0;
        for (int i = 0; i < iLength; i += 2) {
            String s1 = s.substring(i, i + 2);
            buff[j++] = (byte) Integer.parseInt(s1, 16);
        }
        // }
        return buff;

    }

    public static String Masterkey(String key1, String key2, String key3) {
        //String key1 =    "5D25072F04832A2329D93E4F91BA23A2";
        //String key2 = "86CBCDE3B0A22354853E04521686863D";
        //String key3 ="F7486869FE6717D22629EF20B84521AF";
        String masterkeyresult;
        byte[] keyB1 = TripleDES.hexToByte(key1 + key1.substring(0, 16));
        byte[] keyB2 = TripleDES.hexToByte(key2 + key2.substring(0, 16));
        byte[] keyB3 = TripleDES.hexToByte(key3 + key3.substring(0, 16));

        for (int i = 0; i < keyB2.length; i++) {
            keyB1[i] = ((byte) (keyB1[i] ^ keyB2[i]));
        }
        SecretKey key = TripleDES.readKey(keyB1);
        masterkeyresult = TripleDES.Decrypt(key, key3);

        return masterkeyresult;
    }


    public static String Sessionkey(String key1, String key2) {

        String sessionkeyresult;
        //String key2 = "1E142DED249ED50FD8C29F15B8E3E81B";
        //String key1 = "DA5E9E2A2F160D3EAD3E1AF1FBF72F15";
        byte[] mkB = TripleDES.hexToByte(key2 + key2.substring(0, 16));
        SecretKey keyse = TripleDES.readKey(mkB);

        sessionkeyresult = TripleDES.Decrypt(keyse, key1);
        return sessionkeyresult;
    }

    public static String encryptKey(String cipherText, String encryptingKey) {
        byte[] mkB = TripleDES.hexToByte(encryptingKey + encryptingKey.substring(0, 16));
        SecretKey keyse = TripleDES.readKey(mkB);

        return Encrypt(keyse, cipherText);
    }


    public static String Pinkey(String key1, String key2) {

        String pinkeyresult;
        //String key2 = "1E142DED249ED50FD8C29F15B8E3E81B";
        //String key1 = "DA5E9E2A2F160D3EAD3E1AF1FBF72F15";
        byte[] mkB = TripleDES.hexToByte(key2 + key2.substring(0, 16));
        SecretKey keyse = TripleDES.readKey(mkB);

        pinkeyresult = TripleDES.Decrypt(keyse, key1);
        return pinkeyresult;
    }


    public static String generateHash256Value(String msg, String key) {
        MessageDigest m = null;
        String hashText = null;
        byte[] actualKeyBytes = hexStringToBytes(key);

        try {
            m = MessageDigest.getInstance("SHA-256");
            m.update(actualKeyBytes, 0, actualKeyBytes.length);
            try {
                m.update(msg.getBytes("UTF-8"), 0, msg.length());
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            hashText = new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        if (hashText.length() < 64) {
            int numberOfZeroes = 64 - hashText.length();
            String zeroes = "";

            for (int i = 0; i < numberOfZeroes; i++)
                zeroes = zeroes + "0";

            hashText = zeroes + hashText;

            ////LOGger.info("Utility :: generateHash256Value :: HashValue with zeroes: " + hashText); 
        }

        return hashText;

    }

    public static String EncryptDES(Key key, String clearComp) throws NoSuchAlgorithmException, IOException,
            NoSuchPaddingException {

        try {

            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            //LOG.debug("" + cipher.getOutputSize(3));
            byte[] clearText = hexToByte(clearComp);

            CipherOutputStream out = new CipherOutputStream(bytes, cipher);
            out.write(clearText);
            out.flush();
            out.close();
            byte[] ciphertext = bytes.toByteArray();
            bytes.flush();
            bytes.close();

            String encrypted = ToHexString(ciphertext);
            //LOG.info("Enc 3: " + ToHexString(ciphertext));
            java.util.Arrays.fill(clearText, (byte) 0);
            java.util.Arrays.fill(ciphertext, (byte) 0);
            return encrypted;
        } catch (InvalidKeyException ex) {

            //LOG.error(" Caused: ", ex);
            return null;
        }
    }

    public static String Encrypt(Key key, String clearComp) {

        try {

            Cipher cipher;
            cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] clearText = hexToByte(clearComp);

            CipherOutputStream out = new CipherOutputStream(bytes, cipher);
            out.write(clearText);
            out.flush();
            out.close();
            byte[] ciphertext = bytes.toByteArray();
            bytes.flush();
            bytes.close();

            String encrypted = ToHexString(ciphertext);
            java.util.Arrays.fill(clearText, (byte) 0);
            java.util.Arrays.fill(ciphertext, (byte) 0);
            return encrypted;
        } catch (IOException ex) {

            //LOG.error(" Caused: ", ex);
            return null;
        } catch (NoSuchPaddingException ex) {

            //LOG.error(" Caused: ", ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {

            //LOG.error(" Caused: ", ex);
            return null;
        } catch (InvalidKeyException ex) {

            //LOG.error("Caused:  ", ex);
            return null;
        }
    }

    public static String Decrypt(Key key, String cipherComp) {

        try {

            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] ciphertext = hexToByte(cipherComp);
            CipherOutputStream out;

            out = new CipherOutputStream(bytes, cipher);
            out.write(ciphertext);
            out.flush();
            out.close();
            byte[] deciphertext = bytes.toByteArray();
            bytes.flush();
            bytes.close();

            String decrypted = ToHexString(deciphertext);
            java.util.Arrays.fill(ciphertext, (byte) 0);
            java.util.Arrays.fill(deciphertext, (byte) 0);
            return decrypted;
        } catch (IOException ex) {
            //LOG.error(" Caused: ", ex);
            return null;
        } catch (NoSuchPaddingException ex) {

            //LOG.error(" Caused: ", ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {

            //LOG.error(" Caused: ", ex);
            return null;
        } catch (InvalidKeyException ex) {

            //LOG.error("Caused:  ", ex);
            return null;
        }
    }

    public static String DecryptDES(Key key, String cipherComp) throws NoSuchAlgorithmException, IOException,
            NoSuchPaddingException {

        try {
            //LOG.info("cipher componnent: " + cipherComp);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] cipherText = hexToByte(cipherComp);

            CipherOutputStream out;
            out = new CipherOutputStream(bytes, cipher);
            out.write(cipherText);
            out.flush();
            out.close();
            byte[] deciphertext = bytes.toByteArray();
            bytes.flush();
            bytes.close();
            //LOG.info("Dec 3: " + ToHexString(deciphertext));

            String decrypted = ToHexString(deciphertext);
            java.util.Arrays.fill(cipherText, (byte) 0);
            java.util.Arrays.fill(deciphertext, (byte) 0);
            return decrypted;
        } catch (InvalidKeyException ex) {

            //LOG.error("Caused: ", ex);
            return null;
        }
    }

    public static String ToHexString(byte[] toAsciiData) {

        String hexString = "";
        for (byte b : toAsciiData) {
            hexString += String.format("%02X", b);
        }
        return hexString;
    }

    public static byte[] hexToByte(String hexString) {

        String str = new String("0123456789ABCDEF");
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0, j = 0; i < hexString.length(); i++) {

            byte firstQuad = (byte) ((str.indexOf(hexString.charAt(i))) << 4);
            byte secondQuad = (byte) str.indexOf(hexString.charAt(++i));
            bytes[j++] = (byte) (firstQuad | secondQuad);
        }
        return bytes;
    }

    public static String toBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    public static String hexToBinary(String hex) {
        // int i = Integer.parseInt(hex, 16);
        //String bin = Integer.toBinaryString(i);
//    return bin;

        String hexf = hex;

        String binAddr = Integer.toBinaryString(Integer.parseInt(hexf, 16));
        String.format("%032", new BigInteger(binAddr));
        return binAddr;
    }


    public static byte[] intToBytes(int x) {
        byte[] bytes = new byte[2];

        for (int i = 0; x != 0; i++, x >>>= 8) {
            bytes[i] = (byte) (x & 0xFF);
        }

        return bytes;
    }

    public static String hexToBin(String hex) {
        String bin = "";
        String binFragment = "";
        int iHex;
        hex = hex.trim();
        hex = hex.replaceFirst("0x", "");

        for (int i = 0; i < hex.length(); i++) {
            iHex = Integer.parseInt("" + hex.charAt(i), 16);
            binFragment = Integer.toBinaryString(iHex);

            while (binFragment.length() < 4) {
                binFragment = "0" + binFragment;
            }
            bin += binFragment;
        }
        return bin;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }

    public static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static final byte[] append(final byte[]... arrays) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (arrays != null) {
            for (final byte[] array : arrays) {
                if (array != null) {
                    out.write(array, 0, array.length);
                }
            }
        }
        return out.toByteArray();
    }

    private static String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static void main(String[] ad) throws IOException {

        String key1 = "10101010101010101010101010101010";
        String key2 = "01010101010101010101010101010101";
        String key3 = "FDCE7989D5C2940DD63EBA867307A401";

        byte[] keyB1 = TripleDES.hexToByte(key1 + key1.substring(0, 16));
        byte[] keyB2 = TripleDES.hexToByte(key2 + key2.substring(0, 16));
        byte[] keyB3 = TripleDES.hexToByte(key3 + key3.substring(0, 16));


        for (int i = 0; i < keyB2.length; i++) {
            keyB1[i] = ((byte) (keyB1[i] ^ keyB2[i]));
        }
        SecretKey key = TripleDES.readKey(keyB1);

        /*String pin="045383FFFFFFFFFF";
         String pan="0000555535120488";

         byte[] pinb=TripleDES.hexToByte(pin);
         byte[] panb=TripleDES.hexToByte(pan);

         for(int i=0; i<pinb.length; i++) {
         pinb[i]=(byte)(pinb[i]^panb[i]);
         }


         //LOG.info("The check value: "+TripleDES.ToHexString(keyB1));
         //LOG.info("The pinblock value: "+TripleDES.Encrypt(key, ToHexString(pinb)));
         *
         * */

        //byte[] keyB3=TripleDES.hexToByte("00112233445566778899AABBCCDDEEFF0011223344556677");
        //SecretKey key=TripleDES.readKey(keyB1);
        //LOG.info("The pinblock value: " + TripleDES.Encrypt(key, "0000000000000000"));

        String MK = TripleDES.Masterkey("5D25072F04832A2329D93E4F91BA23A2", "86CBCDE3B0A22354853E04521686863D", "8C198ADC0ACDD8539F500AE253A018DD");

        //mk r 3859399E3C6B4E089495CBA7F47F49E27
//mk 6437896204BA4F3BFD0B0E7AAD92A213

        //LOG.info("The master keys value: " +MK);


//INFO TripleDES:512) MASTER KEYk value: DAE94CF2DFE058D54A075E584CE99B0B
//(INFO TripleDES:513) PIN KEY value: 61DA9EE626E63454298AC886D354CB4F
//(INFO TripleDES:514) SESSION KEY value: E004043851E6BAC18323D9CE4F750BB0
//(INFO TripleDES:516) HASH value: 0a7e236204367449d029f3196a3e43b720510537270e533a0661828f8e271f85
//(INFO TripleDES:523) HASH value: 0010001000111000000000000000000000000000100000000000000000000101
        String SK = TripleDES.Sessionkey("1BEA821376EE9F7F052ADB648C37E6A9", "6437896204BA4F3BFD0B0E7AAD92A213");
        String PK = TripleDES.Sessionkey("24C28760D48490208CF73D9DBD6EE186", "DAE94CF2DFE058D54A075E584CE99B0B");
        String hsd = TripleDES.generateHash256Value("0200723846D029A08205000000001000002100202050610516384269045999051123000324072607004606072607032400000000100012D12345678111129345061051638426904D1610221010374591412000000000034221  EDC DEMO APPLICATION   SPECTRA TECHNOL566950580800480009F1A0203569F2608F96210F14EE068DA9F2701805F2A0207849F02060000000100009F360200259F37041F7754609F3501219F34034203009F3303E0F8009F03060000000000009A031407039F0902008C9C01009F4104000000489F100706010A03A0A8009F1E08313233343536373882025C008407A00000000310105F3401010155101015113441010A4ED5798BD1DBAFB720A0050FC591B2C91BA9D6AAEB457EE224221E4FC0F3F420390004014010092335424156481EE565334582325312E11CFEDED7B33C8850CC96074F5574AEC2123F7684A", "799B0897E5A1675BC2082F989E804AD9");


        //LOG.info("MASTER KEYk value: " + MK);
        //LOG.info("PIN KEY value: " + PK);
        //LOG.info("SESSION KEY value: " + SK);

        //LOG.info("HASH value: " + hsd);


        String param = "008D303830303232333830303030303038303030303539433030303030333234303732363037303034323433303732363037303332343230333930303034303134303130303932333335343234313536666631636237613230663032613064666538656235376361366462653062616538343566623161633739656533613237343431616237363766303561323531";

//LOG.info("HASH value===========================: "+TripleDES.hexToBin("F23C46812BE082000000000200000021"));


        String test2 = "080022380000008000009A00000912140912000001140912091220390004";

        String test1 = "0200723846D029A08205000000001000002100202050610516384269045999051123000324072607004606072607032400000000100012D12345678111129345061051638426904D1610221010374591412000000000034221  EDC DEMO APPLICATION   SPECTRA TECHNOL566950580800480009F1A0203569F2608F96210F14EE068DA9F2701805F2A0207849F02060000000100009F360200259F37041F7754609F3501219F34034203009F3303E0F8009F03060000000000009A031407039F0902008C9C01009F4104000000489F100706010A03A0A8009F1E08313233343536373882025C008407A00000000310105F3401010155101015113441010A4ED5798BD1DBAFB720A0050FC591B2C91BA9D6AAEB457EE224221E4FC0F3F420390004014010092335424156481EE565334582325312E11CFEDED7B33C8850CC96074F5574AEC2123F7684A" + hsd.toUpperCase();

//Get the lenght of the ISO
        int x = test1.length();
// Convert lenght from int to binary
        String binlng = Integer.toBinaryString(x);
//Convert lenght from binary to hex
        String hexlng = Integer.toHexString(Integer.parseInt(binlng, 2));
        String test = "130482308100008000000000000008000000140930180200000114093018025909303011234567813POS PARAMETER";
        //LOG.info("BINARY VALUES value: " + hexlng+test1);

//Convert ISO from string to hex
        StringBuffer hex1 = new StringBuffer();
        for (int i = 0; i < test2.length(); i++) {
            if (test2.charAt(i) <= 9) {
                hex1.append('0');
            }
            hex1.append(Integer.toHexString(test2.charAt(i)));
        }


        String contenttohex = "";
        for (int i = 0; i < test1.length(); i++) {
            if (test1.charAt(i) <= 9) {
                contenttohex = contenttohex + '0';
            }
            contenttohex = contenttohex + (Integer.toHexString(test1.charAt(i)));
        }
//Format the hex of the lenght to 4 digit i.e 003D

        Calendar time = null;
        time = new GregorianCalendar();
        //LOG.info("HASH value: " +time.get(0));
        //LOG.info("HASH value: " +time.get(1));
        //LOG.info("HASH value: " +time.get(2));
        //LOG.info("HASH value: " +time.get(3));
        //LOG.info("HASH value: " +time.get(4));
        //LOG.info("HASH value: " +time.get(5));
        //LOG.info("HASH value: " +time.get(6));
        //LOG.info("HASH value: " +time.get(7));
        //LOG.info("HASH value: " +time.get(8));
        //LOG.info("HASH value: " +time.get(10));
        //LOG.info("HASH value: " +time.get(11));
        //LOG.info("HASH value: " +time.get(12));
        //LOG.info("HASH value: " +time.get(13));
        //LOG.info("HASH value: " +time.get(14));
        //LOG.info("HASH value: " +time.get(15));


        hexlng = String.format("%4s", hexlng).replace(' ', '0');
        //LOG.info("HASH value: " + hexlng);
        //LOG.info("HASH value: " + hex1);
        //LOG.info("HASH value: " + contenttohex);

        String msg = "ITEX~102";
        String sentence;
        String modifiedSentence;
        String data;
        String data2;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //String hex = "0810023800000280080009121409120000011409120912002039000401F542405ED03E71DE549A376114CF9451EBE00000000000000000000000000000000000000000000000000000000000";
        String hex = hexlng.toUpperCase() + contenttohex;//"003C303830303232333830303030303038303030303039423030303030393132313430393333303030303035313430393333303931323230333930303034";//hexlng.toUpperCase()+contenttohex;//"003C303830303232333830303030303038303030303039423030303030393132313430393333303030303035313430393333303931323230333930303034";//"004630383030323233383030303030303830303030343941303030303039323931313432323530303431313931313432323530393239323033393030313230393239313134323235";//"003C303830303232333830303030303038303030303039413030303030393132313430393132303030303031313430393132303931323230333930303034";
        int width = hex.length();
        int g = 0000000110010000;
//0D267004405B8CF1792A583DD05713B9
        byte[] databyte = TripleDES.hexToByte(hex);
        byte[] tbit0 = new byte[1];
        byte[] tbit2 = new byte[1];
        byte[] tbit4 = new byte[1];
        byte[] tbit6 = new byte[1];
        byte[] tbit8 = new byte[1];
        byte[] tbit10 = new byte[1];
        byte[] tbit12 = new byte[1];
        byte[] tbit14 = new byte[1];


        byte[] tbit16 = new byte[1];
        byte[] tbit18 = new byte[1];
        byte[] tbit20 = new byte[1];
        byte[] tbit22 = new byte[1];
        byte[] tbit24 = new byte[1];
        byte[] tbit26 = new byte[1];
        byte[] tbit28 = new byte[1];
        byte[] tbit30 = new byte[1];
        String bitall = "82308100008000000000000008000000";
        for (int i = 0; i < bitall.length(); i++) {
            if (i == 0) tbit0 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 2) tbit2 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 4) tbit4 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 6) tbit6 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 8) tbit8 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 10) tbit10 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 12) tbit12 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 14) tbit14 = TripleDES.hexToByte(bitall.substring(i, i + 2));


            if (i == 16) tbit16 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 18) tbit18 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 20) tbit20 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 22) tbit22 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 24) tbit24 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 26) tbit26 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 28) tbit28 = TripleDES.hexToByte(bitall.substring(i, i + 2));
            if (i == 30) tbit30 = TripleDES.hexToByte(bitall.substring(i, i + 2));
//System.out.println("2 byte: "+i+"xxx"+bitall.substring(i, i+2)+ String.format("%1s", tbit0));
            i++;
        }

        byte[] finalarray = new byte[16];
        System.arraycopy(tbit0, 0, finalarray, 0, tbit0.length);
        System.arraycopy(tbit2, 0, finalarray, 1, tbit2.length);
        System.arraycopy(tbit4, 0, finalarray, 2, tbit4.length);
        System.arraycopy(tbit6, 0, finalarray, 3, tbit6.length);
        System.arraycopy(tbit8, 0, finalarray, 4, tbit8.length);
        System.arraycopy(tbit10, 0, finalarray, 5, tbit10.length);
        System.arraycopy(tbit12, 0, finalarray, 6, tbit12.length);
        System.arraycopy(tbit14, 0, finalarray, 7, tbit14.length);
        System.arraycopy(tbit16, 0, finalarray, 8, tbit16.length);
        System.arraycopy(tbit18, 0, finalarray, 9, tbit18.length);
        System.arraycopy(tbit20, 0, finalarray, 10, tbit20.length);
        System.arraycopy(tbit22, 0, finalarray, 11, tbit22.length);
        System.arraycopy(tbit24, 0, finalarray, 12, tbit24.length);
        System.arraycopy(tbit26, 0, finalarray, 13, tbit26.length);
        System.arraycopy(tbit28, 0, finalarray, 14, tbit28.length);
        System.arraycopy(tbit30, 0, finalarray, 15, tbit30.length);

        String s = new String(finalarray, 0, 16);
        StringBuilder sb = new StringBuilder(new String(finalarray, 0, finalarray.length - 1));
        StringBuilder sball;
        String alltostring = "ISO00942100010001304" + s + "141002154900000114100215492110023011234567813POS PARAMETER";
        sball = new StringBuilder(new String("ISO00942100010001304" + sb + "141002154900000114100215492110023011234567813POS PARAMETER"));


        try {
            URL url = new URL("http://192.168.0.245/tams/tams/devinterface/newkeys.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "lipman");
            httpURLConnection.setRequestProperty("Terminalt", "203300CM");
            httpURLConnection.setRequestProperty("EOD", "0");
            httpURLConnection.setRequestProperty("Sign", "lipman");
        } catch (MalformedURLException ex) {
        }

        /**
         String kl = TripleDES.toBinary(finalarray);//.bytesToHexString(finalarray);
         //System.out.println("2 byte 14 00 to string "+kl);
         Socket clientSocket = new Socket("192.168.150.4",5000);//192.168.242.1
         DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

         String str = "TAMS~103~02D83032303037323338343644303239413038323035303030303030303031303030303032313030323032303530363130353136333834323639303435393939303531313233303030333234303732363037303034363036303732363037303332343030303030303030313030303132443132333435363738313131313239333435303631303531363338343236393034443136313032323130313033373435393134313230303030303030303030333432323120204544432044454d4f204150504c49434154494f4e2020205350454354524120544543484e4f4c353636393530353830383030343830303039463141303230333536394632363038463936323130463134454530363844413946323730313830354632413032303738343946303230363030303030303031303030303946333630323030323539463337303431463737353436303946333530313231394633343033343230333030394633333033453046383030394630333036303030303030303030303030394130333134303730333946303930323030384339433031303039463431303430303030303034383946313030373036303130413033413041383030394631453038333133323333333433353336333733383832303235433030383430374130303030303030303331303130354633343031303130313535313031303135313133343431303130413445443537393842443144424146423732304130303530464335393142324339314241394436414145423435374545323234323231453446433046334634323033393030303430313430313030393233333534323431353634383145453536353333343538323332353331324531314346454445443742333343383835304343393630373446353537344145433231323346373638344146324239434544423945383046353638303733384137393746354346463335453838334345393146413546464639303141393039354539354433424137333041\n" +
         "RECEIVING: inputStreamjava.net.SocketInputStream@169454d";
         outToServer.writeBytes(str);//.writeChars(str);
         //OutputStreamWriter osw;

         //osw =new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
         //osw.write(str, 0, str.length());

         //outToServer.write(databyte);//tbit8, g, g);.writeUTF(alltostring);//);.write(databyte);
         //System.out.println("RECEIVING: " + hex);
         BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         InputStream inputStream = clientSocket.getInputStream();
         //System.out.println("RECEIVING: inputStream" + inputStream);
         byte[] tit0 = new byte[2];
         SocketUtils.readStreamnibss(inputStream, tit0, 2);

         int lengthExpected = EncodeUtils.convertBCDToInt(tit0);
         byte[] tit = new byte[lengthExpected];
         SocketUtils.readStreamnibss(inputStream, tit, lengthExpected);

         String klo = TripleDES.bytesToHexString(tit);
         **/
        String receive = "0110F23C46D12BE08200000000000000002116539983163961989531100000000002000002251610090061281610090225160159990510000012D0000000106111129345399831639619895D160122100092353500000000008809622120390004039000000000040SUCCESS KITCHEN LTD                     5661889F260840BEB3C7E400C4B39F2701809F100706010A03A4A8009F3704C00A04489F3602004E950500000080009A031502109C01009F02060000000000015F2A02056682025C009F1A0205669F3303E0F8E89F34034102029F1E04280081800155101015113441014181d5f1368e5d52ef49610e0d4bff4f42b3988385adb1987b3faa7a3b703090";


//0810
//0238000002800800
//7 = 0912140912
//11= 000001
//12 = 140912
//13 = 0912
//39 = 00
// 41 = 20390004
//64 = 42EA9B9EC71F94164F1DDD2EAEE42C646F1768

        //LOG.info("FIELD  =============: "+receive.substring(0, 2));
        int bitmaptype = 16;
        //String receive = "";
        String bitm = TripleDES.hexToBin("F23C44D12BE082000000000000000021");//"F23C46D129E082000000000000000021");//receive.substring(4, 20));
        String field = "";
        int num = 1;

        for (int gd = 0; gd < bitm.length(); gd++) {
            if ((gd == 0) && bitm.charAt(gd) == 1) {
                bitmaptype = 32;
            }
            if (bitm.charAt(gd) == '1') {
                field += num + ",";

            }
            num++;
        }
        bitmaptype = 32;
        String[] ary = field.split(",");
        for (int o = 0; o < ary.length; o++) {
            int fvalues = Integer.parseInt(ary[o]);
            //LOG.info("FIELD  =============: "+fvalues);
            switch (fvalues) {

                case 2: {
                    int b = bitmaptype + 4;

                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk);

                    //postBridge.setTransmissionDateTime(receive.substring(b+2, b+e));
                    //LOG.info("FIELD 2 =============: " + receive.substring(b, b + e + 2));
                    break;
                }
                case 3: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e;
                    //System.out.println("11  ====" + receive.substring(i, i + 6));
                    break;
                }
                case 4: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6;
                    //System.out.println("11  ====" + receive.substring(i, i + 12));
                    break;
                }
                case 7: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12;
                    //System.out.println("11  ====" + receive.substring(i, i + 10));
                    break;
                }

                case 11: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10;
                    //System.out.println("11  ====" + receive.substring(i, i + 6));
                    break;
                }

                case 12: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6;
                    //System.out.println("11  ====" + receive.substring(i, i + 6));
                    break;
                }


                case 13: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6;
                    //System.out.println("11  ====" + receive.substring(i, i + 4));
                    break;
                }


                case 14: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4;
                    //System.out.println("11  ====" + receive.substring(i, i + 4));
                    break;
                }


                case 18: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4;
                    //System.out.println("11  ====" + receive.substring(i, i + 4));
                    break;
                }

                case 22: {
                    int b = bitmaptype + 4;

                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4;

                    //System.out.println("11  ====" + receive.substring(i, i + 3));
                    break;
                }


                case 23: {
                    int b = bitmaptype + 4;

                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3;

                    //System.out.println("11  ====" + receive.substring(i, i + 3));
                    break;
                }

//345399831639619895D16012210009235350

                case 25: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3;
                    //System.out.println("11  ====" + receive.substring(i, i + 2));
                    break;
                }

                case 26: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2;
                    //System.out.println("11  ====" + receive.substring(i, i + 2));
                    break;
                }


                case 28: {
                    int b = bitmaptype + 4;

                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2;

                    //System.out.println("11  ====" + receive.substring(i, i + 9));
                    break;
                }


                case 32: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    //System.out.println("11  ====" + receive.substring(i, i + e1));
                    break;
                }


                case 35: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    //System.out.println("11  ====" + receive.substring(i1, i1 + e2));
                    break;
                }


                case 37: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2;
                    //System.out.println("37  ====" + receive.substring(e3, e3 + 12));
                    break;
                }
                case 39: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12;
                    //System.out.println("39  ====" + receive.substring(e3, e3 + 2));
                    break;
                }


                case 40: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2;
                    //System.out.println("40  ====" + receive.substring(e3, e3 + 3));
                    break;
                }


                case 41: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2 + 3;
                    //System.out.println("41  ====" + receive.substring(e3, e3 + 8));
                    break;
                }


                case 42: {
                    int b = bitmaptype + 4;

                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2 + 3 + 8;
                    //System.out.println("42  ====" + receive.substring(e3, e3 + 15));
                    break;
                }


                case 43: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2 + 3 + 8 + 15;
                    //System.out.println("43  ====" + receive.substring(e3, e3 + 40));
                    break;
                }


                case 49: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2 + 3 + 8 + 15 + 40;
                    //System.out.println("49  ====" + receive.substring(e3, e3 + 3));
                    break;
                }


                case 55: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2 + 3 + 8 + 15 + 40 + 3;
                    String lk3 = receive.substring(e3, e3 + 3);
                    int e4 = Integer.parseInt(lk3) + 3;
                    //System.out.println("55  ====" + receive.substring(e3, e3 + e4));
                    break;
                }


                case 123: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2 + 3 + 8 + 15 + 40 + 3;
                    String lk3 = receive.substring(e3, e3 + 3);
                    int e4 = Integer.parseInt(lk3) + 3;
                    int e5 = e3 + e4;
                    String lk4 = receive.substring(e5, e5 + 3);
                    int e6 = Integer.parseInt(lk4) + 3;
                    //System.out.println("123  ====" + receive.substring(e5, e5 + e6));
                    break;
                }


                case 128: {
                    int b = bitmaptype + 4;
                    String lk = receive.substring(b, b + 2);
                    int e = Integer.parseInt(lk) + 2;
                    int i = b + e + 6 + 12 + 10 + 6 + 6 + 4 + 4 + 4 + 3 + 3 + 2 + 2 + 9;
                    String lk1 = receive.substring(i, i + 2);
                    int e1 = Integer.parseInt(lk1) + 2;
                    int i1 = i + e1;
                    String lk2 = receive.substring(i1, i1 + 2);
                    int e2 = Integer.parseInt(lk2) + 2;
                    int e3 = i1 + e2 + 12 + 2 + 3 + 8 + 15 + 40 + 3;
                    String lk3 = receive.substring(e3, e3 + 3);
                    int e4 = Integer.parseInt(lk3) + 3;
                    int e5 = e3 + e4;
                    String lk4 = receive.substring(e5, e5 + 3);
                    int e6 = Integer.parseInt(lk4) + 3;
                    int e7 = e5 + e6;
                    //System.out.println("128  ====" + receive.substring(e7, e7 + 64));
                    break;
                }

                case 62: {
                    int b = receive.length() - 64;
                    int e = receive.length();
                    //System.out.println("62 ====" + receive.substring(b, e));

                    int b2 = receive.length() - (64 + 40);
                    int e2 = b2 + 40;
                    //System.out.println("Merchant INfo ====" +receive.substring(b2, e2));

                    int b3 = receive.length() - (64 + 40 + 9);
                    int e3 = b3 + 4;
                    //System.out.println("Merchant MCC ====" +receive.substring(b3, e3));

                    int b4 = receive.length() - (64 + 40 + 9 + 14);
                    int e4 = b4 + 3;
                    //System.out.println("Merchant Country Code ====" +receive.substring(b4, e4));


                    int b5 = receive.length() - (64 + 40 + 9 + 14 + 8);
                    int e5 = b5 + 3;
                    //System.out.println("Merchant Currency Code ====" +receive.substring(b5, e5));


                    int b6 = receive.length() - (64 + 40 + 9 + 14 + 8 + 26);
                    int e6 = b6 + 15;
                    //System.out.println("Merchant Institute Code ====" +receive.substring(b6, e6));


                    int b7 = receive.length() - (64 + 40 + 9 + 14 + 8 + 26 + 27);
                    int e7 = b7 + 16;
                    //System.out.println("CTMS  Date====" +receive.substring(b7, e7));
                    break;
                }


            }
        }
        //System.out.println("FROM SERVER: ====" + bitm + "xxx" + field);
        //System.out.println("FROM SERVER: ====" + receive);
        ////System.out.println("FROM SERVER: ====" + klo);
        //System.out.println("FROM SERVER: ====" + receive.substring(0, 4));
        //System.out.println("FROM SERVER: ====" + receive.substring(4, 20));
        //System.out.println("FROM SERVER: ====" + receive.substring(20, 30));
        //System.out.println("FROM SERVER: ====" + receive.substring(30, 36));
        //System.out.println("FROM SERVER: ====" + receive.substring(36, 42));
        //System.out.println("FROM SERVER: ====" + receive.substring(42, 46));
        //System.out.println("FROM SERVER: ====" + receive.substring(46, 48));

        //System.out.println("FROM SERVER: ====" + receive.substring(48, 56));
        //System.out.println("FROM SERVER: ====" + receive.substring(56, 88));


        //+"====="+klo.substring(5, 10));
        //, width).startsWith("08100"));
        //    postBridge.setTransmissionDateTime(isores.substring(21, 10));
        //postBridge.setSystemTraceAudit(isores.substring(32, 6));
        // postBridge.setTimeLocalTransaction(isores.substring(39, 6));
        // postBridge.setDateLocalTransaction(isores.substring(41, 4));
        // postBridge.setResponseCode(isores.substring(46, 2));
        // postBridge.setCardAcceptorTerminalId(isores.substring(49,8));
        // postBridge.setMessagehashvalue(isores.substring(58,32));
        // //System.out.println("FROM SERVER: ===="+isomsg.substring(21, 2));
        //System.out.println("FROM SERVER: ====");
        //System.out.println("FROM SERVER: ====");
        //System.out.println("FROM SERVER: ====");
        //System.out.println("FROM SERVER: ====");
        //System.out.println("FROM SERVER: ====");


        //clientSocket.close();
        Socket echoSocket = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        String stdIn = "08008238000000000000040000000000000009261256591256590926000237301";

        try {
            echoSocket = new Socket("196.6.103.10", 55531);
            os = new DataOutputStream(echoSocket.getOutputStream());
            is = new DataInputStream(echoSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: taranis");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: taranis");
        }

        if (echoSocket != null && os != null && is != null) {
            try {
                String userInput;


                os.writeBytes(stdIn);
                //LOG.info("The pinblock value: " + is.readByte());
                // //System.out.println("echo: " + is.readLine());

                os.close();
                is.close();
                echoSocket.close();
            } catch (IOException e) {
                System.err.println("I/O failed on the connection to: taranis" + e);
            }
        }

    }
}