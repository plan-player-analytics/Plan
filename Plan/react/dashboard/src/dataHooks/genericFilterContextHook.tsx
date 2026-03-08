import {createContext, PropsWithChildren, useCallback, useContext, useMemo, useState} from "react";
import {GenericFilter} from "./model/GenericFilter";

type GenericFilterContextProps = {
    reset: () => void;
    setAfter: (value: number) => void;
    setBefore: (value: number) => void;
} & GenericFilter;

const GenericFilterContext = createContext<GenericFilterContextProps | undefined>(undefined);

type Props = {
    initialValue?: GenericFilter;
} & PropsWithChildren

export const GenericFilterContextProvider = ({initialValue, children}: Props) => {
    const [after, setAfter] = useState<number | undefined>(initialValue?.after);
    const [before, setBefore] = useState<number | undefined>(initialValue?.before);

    const reset = useCallback(() => {
        setAfter(undefined);
        setBefore(undefined);
    }, [setAfter, setBefore])

    const context = useMemo(() => {
        return {
            ...initialValue,
            after,
            before,
            reset,
            setAfter,
            setBefore
        }
    }, [initialValue, after, before, reset, setAfter, setBefore]);
    return <GenericFilterContext.Provider value={context}>
        {children}
    </GenericFilterContext.Provider>;
}

export const useGenericFilter = () => {
    const context = useContext(GenericFilterContext);
    if (!context) throw new Error("useGenericFilter must be used within ThemeStorageContextProvider");
    return context;
}