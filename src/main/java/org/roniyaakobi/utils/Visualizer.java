package org.roniyaakobi.utils;

public class Visualizer {
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

}
