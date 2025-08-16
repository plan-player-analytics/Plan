import React, {useEffect, useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";

const TabButton = ({id, name, href, icon, color, active, disabled}) => {
    const navigate = useNavigate();
    return (
        <li className={"nav-item" + (disabled ? ' disabled' : '')} id={id}>
            <button disabled={disabled} className={"nav-link col-text" + (active ? ' active' : '')}
                    aria-selected={active} role="tab"
                    onClick={() => navigate('#' + href, {replace: true})}>
                <Fa icon={icon} className={'col-' + color}/> {name}
            </button>
        </li>
    )
}

const TabButtons = ({tabs, selectedTab}) => {
    return (
        <ul className="nav nav-tabs" role="tablist">
            {tabs.map(tab => (
                <TabButton
                    key={tab.href}
                    id={tab.href + "-nav"}
                    name={tab.name}
                    href={tab.href}
                    icon={tab.icon}
                    color={tab.color}
                    active={tab.href === selectedTab}
                    disabled={tab.disabled}
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
        setSelectedTab(hash && tabs ? tabs.find(t => t.href === hash.substring(1))?.href : firstTab)
    }, [hash, tabs, firstTab])

    const tabContent = tabs.find(t => t.href === selectedTab)?.element;
    return (
        <>
            <TabButtons tabs={tabs} selectedTab={selectedTab}/>
            {tabContent}
        </>
    )
}

export default CardTabs