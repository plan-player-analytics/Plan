import React from 'react';
import Sidebar from "../../navigation/Sidebar.jsx";
import {faCogs, faInfoCircle, faNetworkWired, faServer} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";

export const SidebarUseCase = () => {
    let items = [
        {
            name: 'html.label.networkOverview',
            icon: faInfoCircle,
            href: "javascript:void(0)",
        },
        {},
        {name: 'html.label.information'},
        {
            name: 'html.label.servers',
            icon: faServer,
            contents: [
                {
                    nameShort: 'html.label.overview', name: 'html.label.servers', icon: faNetworkWired,
                    href: "javascript:void(0)",
                },
                {
                    name: 'html.label.sessions', icon: faCalendarCheck, href: "javascript:void(0)",
                },
                {
                    name: 'html.label.performance', icon: faCogs, href: "javascript:void(0)",
                }
            ]
        }];
    return (
        <Sidebar page={'example'} items={items} openItemIndex={3}/>
    )
};