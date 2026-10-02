package org.roniyaakobi.utils;

public class BytesFormatter {
    public static void printBytes(byte[] bytes){
        for (byte b : bytes){
            System.out.print(b + " ");
        }
        System.out.println();
    }

    public static void printBytesAsString(byte[] bytes){
        for (byte b : bytes){
            System.out.print((char)b);
        }
        System.out.println();
    }

    public static String bytesToString(byte[] bytes){
        String str = "";

        for(byte b : bytes){
            str += b;
        }

        return str;
    }

}
