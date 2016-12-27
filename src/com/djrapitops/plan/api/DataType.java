
package com.djrapitops.plan.api;

public enum DataType {
    STRING, // Any preformatted data & words
    TIME, // Long in milliseconds
    TIME_TIMESTAMP, // Long in milliseconds since Epoch Date 1970, will be subtracted from current time.
    DATE, // Long in milliseconds since Epoch Date 1970
    LOCATION, // X:# Y:# Z:#
    AMOUNT, // Number
    AMOUNT_WITH_MAX, // Example: 41 / 44
    AMOUNT_WITH_LETTERS, // Example $50
    BOOLEAN, // true/false
    PERCENT, // Example 50%
    OTHER, // Any data not listed here - will not be analyzed
    MAP, // An image presentation of array in string format, no format yet
    LINK, // Link to a webpage
    HEATMAP, // An image presentation of array in string format, no format yet
    DEPRECATED // Old data that has been rendered useless
}
