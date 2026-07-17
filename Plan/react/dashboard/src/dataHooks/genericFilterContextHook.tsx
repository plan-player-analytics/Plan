import {createContext, useCallback, useContext, useMemo, useState} from "react";
import {GenericFilter} from "./model/GenericFilter";

type GenericFilterContextProps = {
    reset: () => void;
    setAfter: (value: number | undefined) => void;
    setBefore: (value: number | undefined) => void;
    setTimeframe: (start: number | undefined, end: number | undefined) => void;
} & GenericFilter;

const GenericFilterContext = createContext<GenericFilterContextProps | undefined>(undefined);

type Props = {
    initialValue?: GenericFilter;
    children?: ((filter: GenericFilter) => React.ReactNode) | React.ReactNode;
}

export const GenericFilterContextProvider = ({initialValue, children}: Props) => {
    const [after, setAfter] = useState<number | undefined>(initialValue?.after);
    const [before, setBefore] = useState<number | undefined>(initialValue?.before);

    const reset = useCallback(() => {
        setAfter(undefined);
        setBefore(undefined);
    }, [setAfter, setBefore]);

    const changeBefore = useCallback((value: number | undefined) => {
        setBefore(value);
        if (after && value && after > value) {
            setAfter(value);
        }
    }, [setBefore, setAfter, after]);

    const changeAfter = useCallback((value: number | undefined) => {
        setAfter(value);
        if (before && value && before < value) {
            setBefore(value);
        }
    }, [setBefore, setAfter, before]);

    const changeTimeframe = useCallback((start: number | undefined, end: number | undefined) => {
        setAfter(start);
        setBefore(end);
    }, [setBefore, setAfter]);

    const context = useMemo(() => {
        return {
            ...initialValue,
            after,
            before,
            reset,
            setAfter: changeAfter,
            setBefore: changeBefore,
            setTimeframe: changeTimeframe
        }
    }, [initialValue, after, before, reset, changeAfter, changeBefore, changeTimeframe]);
    return <GenericFilterContext.Provider value={context}>
        {typeof children === 'function' ? children(context) : children}
    </GenericFilterContext.Provider>;
}

export const useGenericFilter = () => {
    const context = useContext(GenericFilterContext);
    if (!context) throw new Error("useGenericFilter must be used within GenericFilterContextProvider");
    return context;
}