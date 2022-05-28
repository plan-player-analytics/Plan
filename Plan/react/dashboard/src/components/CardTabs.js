import React, {useEffect, useState} from "react";
import {useLocation} from "react-router-dom";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";

const TabButton = ({name, href, icon, color, active}) => {
    return (
        <li className="nav-item">
            <a className={"nav-link col-black" + (active ? ' active' : '')} aria-selected={active} role="tab"
               href={'#' + href}>
                <Fa icon={icon} className={'col-' + color}/> {name}
            </a>
        </li>
    )
}

const TabButtons = ({tabs, selectedTab}) => {
    return (
        <ul className="nav nav-tabs" role="tablist">
            {tabs.map((tab, i) => (
                <TabButton
                    key={i}
                    name={tab.name}
                    href={tab.href}
                    icon={tab.icon}
                    color={tab.color}
                    active={tab.href === selectedTab}
                />
            ))}
        </ul>
    )
}

const CardTabs = ({tabs}) => {
    const {hash} = useLocation();
    const firstTab = tabs ? tabs[0].href : undefined;
    const [selectedTab, setSelectedTab] = useState(firstTab);

    useEffect(() => {
        setSelectedTab(hash && tabs ? tabs.find(t => t.href === hash.substring(1)).href : firstTab)
    }, [hash, tabs, firstTab])

    const tabContent = tabs.find(t => t.href === selectedTab).element;
    return (
        <>
            <TabButtons tabs={tabs} selectedTab={selectedTab}/>
            {tabContent}
        </>
    )
}

export default CardTabs