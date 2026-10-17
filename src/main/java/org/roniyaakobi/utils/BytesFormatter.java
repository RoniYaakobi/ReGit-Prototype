package org.roniyaakobi.utils;

public class BytesFormatter {

    private static String hexMap = "0123456789abcdef";
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
        for (byte b : bytes){
            str += (char)b;
        }

        return str;
    }

    public static byte[] hexToBytes(String hex){
        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length(); i += 2){
            int upper = hexMap.indexOf(hex.charAt(i));
            int lower = hexMap.indexOf(hex.charAt(i + 1));

            bytes[i / 2] = (byte)(upper * hexMap.length() + lower);
        }

        return bytes;
    }

    public static byte[][] splitAtOffset(byte[] buffer, int n){
        byte[] upToOffset = new byte[n];
        byte[] afterOffset = new byte[buffer.length - n - 1];

        for (int i = 0; i < buffer.length; i++){
            if (i < n) {
                upToOffset[i] = buffer[i];
            } else if (i > n) {
                afterOffset[i - n - 1] = buffer[i];
            }
        }

        return new byte[][] {upToOffset, afterOffset};
    }

    public static int getBytesToNulTerminator(byte[] buffer){
        int leadingBytes = 0;

        while(buffer[leadingBytes] != 0){
            leadingBytes++;
        }

        return leadingBytes;
    }

    public static String byteToAscii(byte b){
        return bytesToString(new byte[] {b});
    }
}
