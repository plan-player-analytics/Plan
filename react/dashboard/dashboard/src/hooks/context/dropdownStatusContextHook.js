import {createContext, useCallback, useContext, useMemo, useState} from "react";

const DropdownStatusContext = createContext({});

export const DropdownStatusContextProvider = ({children}) => {
    const [toggled, setToggled] = useState([]);

    const toggle = useCallback(key => {
        if (toggled.includes(key)) {
            setToggled(toggled.filter(k => k !== key));
        } else {
            setToggled([...toggled, key]);
        }
    }, [toggled, setToggled]);

    const sharedState = useMemo(() => {
        return {toggled, toggle};
    }, [toggled, toggle]);
    return (<DropdownStatusContext.Provider value={sharedState}>
            {children}
        </DropdownStatusContext.Provider>
    )
}

export const useDropdownStatusContext = () => {
    return useContext(DropdownStatusContext);
}