import {createContext, useContext, useState} from "react";

const NavigationContext = createContext({});

export const NavigationContextProvider = ({children}) => {
    const [currentTab, setCurrentTab] = useState(undefined);

    const sharedState = {currentTab, setCurrentTab}
    return (<NavigationContext.Provider value={sharedState}>
            {children}
        </NavigationContext.Provider>
    )
}

export const useNavigation = () => {
    return useContext(NavigationContext);
}