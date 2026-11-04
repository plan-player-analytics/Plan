import {
    createContext,
    PropsWithChildren,
    useCallback,
    useContext,
    useMemo,
    useState,
} from 'react';
import { CustomRange, TimeframePreset } from '../../util/datapointTablePresets';

type StoredTimeframe = {
    preset: TimeframePreset;
    customRanges?: CustomRange[];
};

type TimeframeUpdate = (previous?: StoredTimeframe) => StoredTimeframe;

type DatapointTimeframeContextValue = {
    timeframes: Record<string, StoredTimeframe>;
    updateTimeframe: (tableId: string, update: TimeframeUpdate) => void;
};

const DatapointTimeframeContext = createContext<
    DatapointTimeframeContextValue | undefined
>(undefined);

export const DatapointTimeframeContextProvider = ({
    children,
}: PropsWithChildren) => {
    const [timeframes, setTimeframes] = useState<
        Record<string, StoredTimeframe>
    >({});

    const updateTimeframe = useCallback(
        (tableId: string, update: TimeframeUpdate) => {
            setTimeframes((previous) => ({
                ...previous,
                [tableId]: update(previous[tableId]),
            }));
        },
        [],
    );

    const value = useMemo(
        () => ({ timeframes, updateTimeframe }),
        [timeframes, updateTimeframe],
    );

    return (
        <DatapointTimeframeContext.Provider value={value}>
            {children}
        </DatapointTimeframeContext.Provider>
    );
};

export const useDatapointTimeframeContext = () => {
    const context = useContext(DatapointTimeframeContext);
    if (!context)
        throw new Error(
            'useDatapointTimeframeContext must be used within DatapointTimeframeContextProvider',
        );
    return context;
};
