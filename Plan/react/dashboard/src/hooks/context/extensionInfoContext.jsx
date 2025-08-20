import {createContext, useContext} from "react";

const ExtensionInfoContext = createContext({});

export const ExtensionInfoContextProvider = ({extension, children}) => {
    return (<ExtensionInfoContext.Provider value={extension}>
            {children}
        </ExtensionInfoContext.Provider>
    )
}

export const useExtensionInfo = () => {
    return useContext(ExtensionInfoContext);
}