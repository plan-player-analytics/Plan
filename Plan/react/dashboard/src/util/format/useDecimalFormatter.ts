import { usePreferences } from '../../hooks/preferences/preferencesHook';
import { useCallback, useMemo } from 'react';
import { isString } from 'highcharts';

export const useDecimalFormatter = () => {
    const { decimalFormat } = usePreferences();

    const formatDecimals = useCallback(
        (value: any) => {
            if (
                !decimalFormat ||
                Number.isNaN(Number(value)) ||
                isString(value)
            )
                return value;
            const split = decimalFormat.includes('.')
                ? decimalFormat.split('.')
                : decimalFormat.split(',');
            if (split.length <= 1) return value.toFixed(0);
            return value.toFixed(split[1].length);
        },
        [decimalFormat],
    );

    return useMemo(() => {
        return { formatDecimals };
    }, [formatDecimals]);
};
