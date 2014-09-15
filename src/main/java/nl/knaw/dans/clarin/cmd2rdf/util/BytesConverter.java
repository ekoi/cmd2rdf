package nl.knaw.dans.clarin.cmd2rdf.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum BytesConverter
{
    B("byte"),
    KB("kilobyte"),
    MB("megabyte"),
    GB("gigabyte"),
    TB("terabyte"),
    PB("petabyte"),
    EB("exabyte"),
    ZB("zettabyte"),
    YB("yottabyte");
    BytesConverter(String name)
    {
        this.name = name;
    }
    private String name;
    public String getName(){return name;}
    public BigInteger convertFrom(BigInteger originalAmount, BytesConverter originalType)
    {
        /**
         * So, B.convertFrom(BigInteger.ONE, YB)
         * should be used to convert 1 yottabyte to bytes...
         */

        BigInteger currentAmount = originalAmount;
        int convertTo = ordinal();
        int convertFrom = originalType.ordinal();
        while(convertTo != convertFrom)
        {
            if(convertTo < convertFrom)
            {
                currentAmount = currentAmount.shiftLeft(10);
                convertFrom--;
            }else{
                currentAmount = currentAmount.shiftRight(10);
                convertFrom++;
            }
        }
        return currentAmount;
    }

    private static List<BytesConverter> reversed = reverse(BytesConverter.values());
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final String friendlyFMT = "%s %s";

    public static String friendly(long bytes)
    {
        return friendly(BytesConverter.B, BigInteger.valueOf(bytes));
    }
    
    /**
     * Convert the specified amount into a human readable (though slightly less accurate)
     * result. IE:
     * '4096 B' to '4 KB'
     * '5080 B' to '5 KB' even though it is really '4 KB + 984 B'
     */
    public static String friendly(BytesConverter type, BigInteger value)
    {
        /**
         * Logic:
         * Loop from YB to B
         * If result = 0, continue
         * Else, round off
         *
         * NOTE: BigIntegers are not reusable, so not point in caching them outside the loop
         */
        for(BytesConverter newType : reversed)
        {
            BigInteger newAmount = newType.convertFrom(value, type);
            if(newAmount.equals(BigInteger.ZERO)) continue;
            // Found the right one. Now to round off
            BigInteger unitBytes = BytesConverter.B.convertFrom(BigInteger.ONE, newType);
            BigInteger usedBytes = newAmount.multiply(unitBytes);
            BigInteger remainingBytes = BytesConverter.B.convertFrom(value, type).subtract(usedBytes);
            if(remainingBytes.equals(BigInteger.ZERO))
                return String.format(friendlyFMT, newAmount.toString(), newType);
            if(remainingBytes.equals(value))
                return String.format(friendlyFMT, newAmount.toString(), newType);
            
            BigInteger halfUnit = unitBytes.divide(TWO);
            if( (remainingBytes.subtract(halfUnit)).signum() < 0)
                return String.format(friendlyFMT, newAmount.toString(), newType);

            return String.format(friendlyFMT, (newAmount.add(BigInteger.ONE)).toString(), newType);
        }
        
        // Give up
        return String.format(friendlyFMT, value.toString(), type);
    }
  public static <T> List<T> reverse(T[] array)
  {
    List<T> list = Arrays.asList(array);
    Collections.reverse(list);
    return list;
  }
}