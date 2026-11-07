import {
    createContext,
    PropsWithChildren,
    useContext,
    useMemo,
    useState,
} from 'react';
import { QueryResult } from './model/QueryResult';

type ContextType = {
    result?: QueryResult;
    setResult: (result: QueryResult) => void;
};

const QueryResultContext = createContext<ContextType | undefined>(undefined);

export const QueryResultContextProvider = ({ children }: PropsWithChildren) => {
    const [result, setResult] = useState<QueryResult | undefined>(undefined);

    const sharedState = useMemo(
        () => ({ result, setResult }),
        [result, setResult],
    );
    return (
        <QueryResultContext.Provider value={sharedState}>
            {children}
        </QueryResultContext.Provider>
    );
};

export const useQueryResultContext = () => {
    const context = useContext(QueryResultContext);
    if (!context)
        throw new Error(
            'useQueryResult must be used within QueryResultContextProvider',
        );
    return context;
};
