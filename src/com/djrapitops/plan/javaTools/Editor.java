
package com.djrapitops.plan.javaTools;

public class Editor {
    
    public String[] mergeArrays( String[]... arrays )
    {
        int arraySize = 0;

        for( String[] array : arrays )
        {
            arraySize += array.length;
        }

        String[] result = new String[arraySize];

        int j = 0;

        for( String[] array : arrays )
        {
            for( String string : array )
            {
                result[j++] = string;
            }
        }

        return result;
    }
}
