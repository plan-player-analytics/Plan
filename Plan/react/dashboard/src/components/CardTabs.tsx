import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { IconProp } from '@fortawesome/fontawesome-svg-core';

type Tab = {
    name: React.ReactNode;
    href: string;
    icon: IconProp;
    color?: string;
    disabled?: boolean;
    element: React.ReactNode;
};

type Props = {
    tabs: Tab[];
};

type TabButtonProps = Omit<Tab, 'element'> & {
    id: string;
    active: boolean;
};

const TabButton = ({
    id,
    name,
    href,
    icon,
    color,
    active,
    disabled,
}: TabButtonProps) => {
    const navigate = useNavigate();
    return (
        <li className={'nav-item' + (disabled ? ' disabled' : '')} id={id}>
            <button
                disabled={disabled}
                className={'nav-link col-text' + (active ? ' active' : '')}
                aria-selected={active}
                role='tab'
                onClick={() => navigate('#' + href, { replace: true })}>
                <Fa icon={icon} className={'col-' + color} /> {name}
            </button>
        </li>
    );
};

const TabButtons = ({
    tabs,
    selectedTab,
}: Props & { selectedTab?: string }) => {
    return (
        <ul className='nav nav-tabs' role='tablist'>
            {tabs.map((tab) => (
                <TabButton
                    key={tab.href}
                    id={tab.href + '-nav'}
                    name={tab.name}
                    href={tab.href}
                    icon={tab.icon}
                    color={tab.color}
                    active={tab.href === selectedTab}
                    disabled={tab.disabled}
                />
            ))}
        </ul>
    );
};

const CardTabs = ({ tabs }: Props) => {
    const { hash } = useLocation();
    const firstTab = tabs?.length ? tabs[0].href : undefined;
    const [selectedTab, setSelectedTab] = useState(firstTab);

    useEffect(() => {
        setSelectedTab(
            hash && tabs
                ? tabs.find((t) => t.href === hash.substring(1))?.href
                : firstTab,
        );
    }, [hash, tabs, firstTab]);

    const tabContent = tabs.find((t) => t.href === selectedTab)?.element;
    return (
        <>
            <TabButtons tabs={tabs} selectedTab={selectedTab} />
            {tabContent}
        </>
    );
};

export default CardTabs;
