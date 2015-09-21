package br.com.hive.hibeacon.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Class that contains all kind of utility static methods
 *
 * Created by Felippe Bisca on 17/09/2015.
 */
public class Utils {

    /**
     * Returns a new array containing the <b>count</b> length
     *
     * @param array Array that should be sliced
     * @param start index position to start the slice
     * @param count count of elements that should be put in the new array
     *
     * @return new array
     */
    public static byte[] subArray(byte[] array, int start, int count) {
        byte[] newArray = new byte[count];
        int newArrayPos = 0;

        for (int i = start;
             i < array.length && i < start + count; i++) {
            newArray[newArrayPos] = array[i];
            newArrayPos++;
        }

        return newArray;
    }

    /**
     * Transforms a byte array into a UUID object
     *
     * @param bytes byte array that represents a UUID
     *
     * @return UUID
     */
    public static UUID getUUID(byte[] bytes) {
        if (bytes.length != 16) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }


}
