/**
 * Copyright (c) 2012 Eleven Inc. All Rights Reserved.
 */
package com.venus.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MD5Encoder {

    private static char[] hexDigits = "0123456789abcdef".toCharArray();

    public static String encode(String desStr) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(desStr.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte aResult : result) {
                int number = aResult & 0xff;//add salt
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    sb.append("0");
                    sb.append(str);
                } else {
                    sb.append(str);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getMd5(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        String result;
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(bytes);
            byte[] messageDigest = digester.digest();

            StringBuilder hexBuffer = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexBuffer.append(h);
            }

            result = hexBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            result = "";
        }

        return result;
    }

    public static String md5(File file) {
        String md5 = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bytes = new byte[4096];
            int read;
            MessageDigest digest = MessageDigest.getInstance("MD5");

            while ((read = fis.read(bytes)) != -1) {
                digest.update(bytes, 0, read);
            }

            byte[] messageDigest = digest.digest();

            StringBuilder sb = new StringBuilder(32);

            for (byte b : messageDigest) {
                sb.append(hexDigits[(b >> 4) & 0x0f]);
                sb.append(hexDigits[b & 0x0f]);
            }

            md5 = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return md5;
    }
}
