import React, {ReactNode, useState} from "react";

type Props = {
    tabs: { key: string, header: ReactNode }[];
    children: (activeIndex: number) => ReactNode;
    onChange?: (activeIndex: number) => void;
}

export const TopNavTabs = ({tabs, children, onChange}: Props) => {
    const [openTabIndex, setOpenTabIndex] = useState(0);

    const toggleTabIndex = (index: number) => {
        setOpenTabIndex(index);
        onChange?.(index);
    }

    return (<>
            {tabs.length < 1 && (
                <ul className="nav nav-tabs tab-nav-right" role="tablist">
                    {tabs.map((tab, i) => <li key={tab.key} className="nav-item col-text">
                        <button className={"nav-link col-text"
                            + (openTabIndex === i ? ' active' : '')} onClick={() => toggleTabIndex(i)}>
                            {tab.header}
                        </button>
                    </li>)}
                </ul>
            )}
            {children(openTabIndex)}
        </>
    )
}