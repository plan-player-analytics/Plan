import {
    createContext,
    PropsWithChildren,
    useCallback,
    useContext,
    useMemo,
    useState,
} from 'react';

type ContextType = {
    toggled: string[];
    toggle: (key: string) => void;
};

const DropdownStatusContext = createContext<ContextType | undefined>(undefined);

export const DropdownStatusContextProvider = ({
    children,
}: PropsWithChildren) => {
    const [toggled, setToggled] = useState<string[]>([]);

    const toggle = useCallback(
        (key: string) => {
            if (toggled.includes(key)) {
                setToggled(toggled.filter((k) => k !== key));
            } else {
                setToggled([...toggled, key]);
            }
        },
        [toggled, setToggled],
    );

    const sharedState = useMemo(() => {
        return { toggled, toggle };
    }, [toggled, toggle]);
    return (
        <DropdownStatusContext.Provider value={sharedState}>
            {children}
        </DropdownStatusContext.Provider>
    );
};

export const useDropdownStatusContext = () => {
    const context = useContext(DropdownStatusContext);
    if (!context)
        throw new Error(
            'useDropdownStatusContext has to be used inside a DropdownStatusContextProvider',
        );
    return context;
};
