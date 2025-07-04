import {createContext, useContext, useState} from "react";

const HoverContext = createContext({});

export const HoverTrigger = ({children}) => {
    const [hovered, setHovered] = useState(false);

    const onHoverEnter = () => {
        setHovered(true);
    }

    const onHoverLeave = () => {
        setHovered(false);
    }

    return (<HoverContext.Provider value={{hovered}}>
            <div onfocusin={onHoverEnter} onfocusout={onHoverLeave} onMouseEnter={onHoverEnter}
                 onMouseLeave={onHoverLeave}>
                {children}
            </div>
        </HoverContext.Provider>
    )
}

export const useHoverContext = () => {
    return useContext(HoverContext);
}