import {
    createContext,
    PropsWithChildren,
    useCallback,
    useContext,
    useMemo,
    useState,
} from 'react';
import { CustomRange, TimeframePreset } from '../../util/datapointTablePresets';

export type StoredTimeframe = {
    preset: TimeframePreset;
    customRanges?: CustomRange[];
};

type TimeframeUpdate = (previous: StoredTimeframe) => StoredTimeframe;

type DatapointTimeframeContextValue = {
    timeframes: Record<string, StoredTimeframe>;
    updateTimeframe: (tableId: string, update: TimeframeUpdate) => void;
};

const DatapointTimeframeContext = createContext<
    DatapointTimeframeContextValue | undefined
>(undefined);

export const DEFAULT_TIMEFRAME: StoredTimeframe = { preset: 'default' };

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
                [tableId]: update(previous[tableId] ?? DEFAULT_TIMEFRAME),
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

export const useStoredTimeframe = (tableId: string) => {
    const context = useContext(DatapointTimeframeContext);
    const [localTimeframe, setLocalTimeframe] =
        useState<StoredTimeframe>(DEFAULT_TIMEFRAME);
    const contextUpdate = context?.updateTimeframe;

    const update = useCallback(
        (change: TimeframeUpdate) => {
            if (contextUpdate) {
                contextUpdate(tableId, change);
            } else {
                setLocalTimeframe(change);
            }
        },
        [contextUpdate, tableId],
    );

    const timeframe = context
        ? (context.timeframes[tableId] ?? DEFAULT_TIMEFRAME)
        : localTimeframe;

    return [timeframe, update] as const;
};
