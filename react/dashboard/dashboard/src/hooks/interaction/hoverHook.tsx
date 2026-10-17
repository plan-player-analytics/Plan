import {createContext, PropsWithChildren, useContext, useState} from "react";

const HoverContext = createContext<boolean | undefined>(undefined);

export const HoverTrigger = ({children}: PropsWithChildren) => {
    const [hovered, setHovered] = useState(false);

    const onHoverEnter = () => {
        setHovered(true);
    }

    const onHoverLeave = () => {
        setHovered(false);
    }

    return (<HoverContext.Provider value={hovered}>
            <div onFocus={onHoverEnter} onBlur={onHoverLeave} onMouseEnter={onHoverEnter}
                 onMouseLeave={onHoverLeave} tabIndex={-1}>
                {children}
            </div>
        </HoverContext.Provider>
    )
}

export const useHoverContext = () => {
    const context = useContext(HoverContext);
    if (context === undefined) throw new Error("HoverContext must be used inside HoverTrigger")
    return context;
}