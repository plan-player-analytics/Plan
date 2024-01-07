import {createContext, useContext, useState} from "react";

const QueryResultContext = createContext({});

export const QueryResultContextProvider = ({children}) => {
    const [result, setResult] = useState({});

    const sharedState = {result, setResult}
    return (<QueryResultContext.Provider value={sharedState}>
            {children}
        </QueryResultContext.Provider>
    )
}

export const useQueryResultContext = () => {
    return useContext(QueryResultContext);
}