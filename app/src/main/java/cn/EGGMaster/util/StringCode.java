package cn.EGGMaster.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static cn.EGGMaster.util.StaticVal.strDefaultKey;

public class StringCode {

    /**
     * 字符串默认键值
     */
    private static StringCode code = null;
    private Cipher encryptCipher = null;
    private Cipher decryptCipher = null;

    private StringCode(String strKey) throws Exception {
        Key key = getKey(strKey.getBytes());

        encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);

        decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
    }

    public static synchronized StringCode getInstance() {
        try {
            if (code == null) {
                code = new StringCode(getStr(strDefaultKey));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return code;
    }

    private Key getKey(byte[] arrBTmp) throws Exception {
        byte[] arrB = new byte[16];
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }
        for (int i = arrBTmp.length; i < arrB.length; i++) {
            arrB[i] = (byte) (1 + i);
        }
        return new SecretKeySpec(arrB, "AES");
    }

    private static String byteArr2HexStr(byte[] arrB) throws Exception {
        int iLen = arrB.length;
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (byte anArrB : arrB) {
            int intTmp = anArrB;
            while (intTmp < 0) {
                intTmp = intTmp + 256;
            }
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16));
        }
        return sb.toString();
    }

    private static byte[] hexStr2ByteArr(String strIn) throws Exception {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;
        byte[] arrOut = new byte[iLen / 2];
        for (int i = 0; i < iLen; i = i + 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }

    private byte[] encrypt(byte[] arrB) throws Exception {
        return encryptCipher.doFinal(arrB);
    }

    public String encrypt(String strIn) {
        try {
            return byteArr2HexStr(encrypt(strIn.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }

    private byte[] decrypt(byte[] arrB) throws Exception {
        return decryptCipher.doFinal(arrB);
    }

    public String decrypt(String strIn) {
        try {
            return new String(decrypt(hexStr2ByteArr(strIn)));
        } catch (Exception e) {
            return "";
        }
    }

//    private static String byteToStr(byte[] arrB) throws Exception {
//        StringBuffer sb = new StringBuffer(arrB.length * 2);
//        for (byte anArrB : arrB) {
//            int intTmp = anArrB;
//            while (intTmp < 0) {
//                intTmp = intTmp + 256;
//            }
//            if (intTmp < 16) {
//                sb.append("0");
//            }
//            sb.append(Integer.toString(intTmp + 2, 16));
//        }
//        return sb.toString();
//    }

    private static byte[] strToByte(String strIn) throws Exception {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;
        byte[] arrOut = new byte[arrB.length / 2];
        for (int i = 0; i < iLen; i = i + 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) (Integer.parseInt(strTmp, 16) - 2);
        }
        return arrOut;
    }

    public static String getStr(String hex) {
        try {
            return new String(strToByte(hex), "utf-8");
        } catch (Exception e) {
            return "";
        }
    }
}