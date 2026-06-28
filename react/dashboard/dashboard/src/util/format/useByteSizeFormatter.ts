import {useCallback, useMemo} from "react";
import {useDecimalFormatter} from "./useDecimalFormatter.js";

/**
 * Formatter for amount of Megabytes (MB)
 *
 * value is in megabytes
 */
export const useByteSizeFormatter = () => {
    const {formatDecimals} = useDecimalFormatter();

    const formatByteSize = useCallback((value: number | string): string => {
        const numericValue = Number(value);
        if (Number.isNaN(numericValue)) return String(value);

        if (numericValue >= 1000000) {
            return `${formatDecimals(numericValue / 1000000)} TB`;
        }
        if (numericValue >= 1000) {
            return `${formatDecimals(numericValue / 1000)} GB`;
        }
        return `${formatDecimals(numericValue)} MB`;
    }, [formatDecimals]);

    return useMemo(() => {
        return {formatByteSize};
    }, [formatByteSize]);
};
