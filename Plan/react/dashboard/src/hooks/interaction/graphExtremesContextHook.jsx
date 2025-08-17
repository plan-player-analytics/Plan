import {createContext, useCallback, useContext, useMemo, useRef, useState} from "react";

const GraphExtremesContext = createContext({});

export const GraphExtremesContextProvider = ({children}) => {
    const currentDebounce = useRef(null);

    const [min, setMin] = useState(undefined);
    const [max, setMax] = useState(undefined);

    const onSetExtremes = useCallback((event) => {
        if (currentDebounce.current) clearTimeout(currentDebounce.current);
        currentDebounce.current = setTimeout(() => {
            if (event?.trigger) {
                setMin(event.min);
                setMax(event.max);
            }
        }, 500);
    }, [setMin, setMax]);

    const sharedState = useMemo(() => {
        if (min !== undefined && max !== undefined) {
            return {extremes: {min, max}, onSetExtremes}
        }
        return {onSetExtremes};
    }, [min, max]);

    return (<GraphExtremesContext.Provider value={sharedState}>
            {children}
        </GraphExtremesContext.Provider>
    )
}

export const useGraphExtremesContext = () => {
    return useContext(GraphExtremesContext);
}