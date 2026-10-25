package org.roniyaakobi.utils;

public class BytesFormatter {

    private static String hexMap = "0123456789abcdef";

    /**
     * Print the bytes raw
     * @param bytes The bytes to print
     */
    public static void printBytes(byte[] bytes){
        for (byte b : bytes){
            System.out.print(b + " ");
        }
        System.out.println();
    }

    /**
     * Print the bytes parsed as a string
     * @param bytes The bytes to print
     */
    public static void printBytesAsString(byte[] bytes){
        for (byte b : bytes){
            System.out.print((char)b);
        }
        System.out.println();
    }

    /**
     * Parse bytes to string
     * @param bytes the bytes to parse
     * @return A string parsed from the bytes
     */
    public static String bytesToString(byte[] bytes){
        String str = "";
        for (byte b : bytes){
            str += (char)b;
        }

        return str;
    }

    /**
     * Turn a hex string to byte array
     * @param hex The hex string
     * @return the new byte array
     */
    public static byte[] hexToBytes(String hex){
        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length(); i += 2){
            int upper = hexMap.indexOf(hex.charAt(i));
            int lower = hexMap.indexOf(hex.charAt(i + 1));

            bytes[i / 2] = (byte)(upper * hexMap.length() + lower);
        }

        return bytes;
    }

    /**
     * Split the byte array at an offset to two buffer
     * @param buffer the buffer to split
     * @param n the index to split at
     * @return the buffer split into two new buffers
     */
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

    /**
     * Counts the offset of the first null terminator
     * @param buffer the buffer to count from
     * @return the index of the first null terminator in the buffer
     */
    public static int getBytesToNulTerminator(byte[] buffer){
        int leadingBytes = 0;

        while(buffer[leadingBytes] != 0){
            leadingBytes++;
        }

        return leadingBytes;
    }

    /**
     * Converts a single byte to ascii
     * @param b the byte
     * @return A string of the ascii value of the byte
     */
    public static String byteToAscii(byte b){
        return bytesToString(new byte[] {b});
    }
}
