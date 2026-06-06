package com.iisysgroup.androidlite.utils;

import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Agbede on 3/14/2018.
 */

public class StringUtil {
    public static String getPrintableLine(String a, String b){
        int length = 32;
        int a_length = a.length();
        int b_length = b.length();

        int total_length = a_length + b_length;
        int space_length = length - total_length;

        StringBuilder stringBuilder = new StringBuilder(32);
        stringBuilder.append(a);
        for (int i = 0; i < space_length; i++){
            stringBuilder.append(" ");
        }
        stringBuilder.append(b);

        Log.d("Final string", stringBuilder.toString());

        return stringBuilder.toString();
    }

    public static boolean isNullWithTrim(String str) {
        return str == null || str.trim().equals("")||str.trim().equals("null");
    }

    /**
     * 给字符串加*
     * @param cardNo 卡号
     * @param prefix 保留 前几位
     * @param suffix 保留 后几位
     * @return 加*后的 String
     */
    public static String getSecurityNum(String cardNo, int prefix, int suffix) {
        StringBuffer cardNoBuffer = new StringBuffer();
        int len = prefix + suffix;
        if ( cardNo.length() > len) {
            cardNoBuffer.append(cardNo.substring(0, prefix));
            for (int i = 0; i < cardNo.length() - len; i++) {
                cardNoBuffer.append("*");
            }
            cardNoBuffer.append(cardNo.substring(cardNo.length() - suffix, cardNo.length()));
        }
        return cardNoBuffer.toString();
    }

    public static String TwoWei(double s){
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(s);
    }

    /**
     * 金额存的时候*100  所有取的时候要/100   格式转换
     * @param amount
     * @return  0.00格式
     */
    public static String TwoWei(String amount){
        DecimalFormat df = new DecimalFormat("0.00");
        double d = 0;
        if(!StringUtil.isNullWithTrim(amount))
            d = Double.parseDouble(amount)/100;
        return df.format(d);
    }

    /**
     *
     * @param date   20160607152954
     * @param oldPattern  yyyyMMddHHmmss
     * @param newPattern yyyy-MM-dd HH:mm:ss
     * @return 2016-06-07 15:29:54
     */
    public static String StringPattern(String date, String oldPattern,
                                       String newPattern) {
        if (date == null || oldPattern == null || newPattern == null)
            return "";
        SimpleDateFormat sdf1 = new SimpleDateFormat(oldPattern); // 实例化模板对象
        SimpleDateFormat sdf2 = new SimpleDateFormat(newPattern); // 实例化模板对象
        Date d = null;
        try {
            d = sdf1.parse(date); // 将给定的字符串中的日期提取出来
        } catch (Exception e) { // 如果提供的字符串格式有错误，则进行异常处理
            e.printStackTrace(); // 打印异常信息
        }
        return sdf2.format(d);
    }

//    public static String hexString(byte[] b, int offset, int len) {
//        String ret = "";
//
//        try {
//            if(b == null) {
//                return "";
//            }
//
//            StringBuilder e = new StringBuilder(len * 2);
//            len += offset;
//
//            for(int i = offset; i < len; ++i) {
//                e.append(hexStrings[b[i] & 255]);
//            }
//
//            ret = e.toString();
//        } catch (Exception var6) {
//            var6.printStackTrace();
//        }
//
//        return ret;
//    }

//    public static String hexString(byte[] b) {
//        return b == null?"":hexString(b, 0, b.length);
//    }

    public static byte[] hex2ByteArray(String hexStr) {
        int len = hexStr.length();
        if((len & 1) == 1) {
            throw new IllegalArgumentException("hexStr is invalid: " + hexStr);
        } else {
            byte[] data = new byte[len / 2];

            for(int i = 0; i < len; i += 2) {
                data[i / 2] = (byte)(Short.valueOf(hexStr.substring(i, i + 2).toString(), 16).shortValue() & 255);
            }

            return data;
        }
    }

    public static byte[] hex2byte(byte[] b, int offset, int len) {
        byte[] d = new byte[len];

        for(int i = 0; i < len * 2; ++i) {
            int shift = i % 2 == 1?0:4;
            d[i >> 1] = (byte)(d[i >> 1] | Character.digit((char)b[offset + i], 16) << shift);
        }

        return d;
    }

    public static byte[] hex2byte(String s) {
        return s.length() % 2 == 0?hex2byte(s.getBytes(), 0, s.length() >> 1):hex2byte("0" + s);
    }

    public static boolean isAmout(String amount) {
        String reg = "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(amount);
        return m.matches();
    }

    /**
     * @param number The number in plain format
     * @param mask The  mask pattern.
     *    Use # to include the digit from the position.
     *    Use x to mask the digit at that position.
     *    Any other char will be inserted.
     *
     * @return The masked card number
     */
    public static String maskNumber(String number, String mask) {

        int index = 0;
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '#') {
                masked.append(number.charAt(index));
                index++;
            } else if (c == '*') {
                masked.append(c);
                index++;
            } else {
                masked.append(c);
            }
        }
        return masked.toString();
    }

    public static void DeleteDir(String FileOrDirName)
    {
        File FileOrDir = new File(FileOrDirName);
        if (FileOrDir.isDirectory())
        {
            Log.d("Delete", "Directory " + FileOrDir.toString());
            for (File child : FileOrDir.listFiles())
                DeleteDir(child.toString());
        }

        Log.d("Delete", "File " + FileOrDir.toString());
        FileOrDir.delete();
    }


    public static String toBase16String(int value){
        return Integer.toHexString(value);
    }
}
