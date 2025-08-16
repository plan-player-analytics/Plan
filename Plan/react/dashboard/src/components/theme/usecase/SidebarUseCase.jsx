import React from 'react';
import Sidebar from "../../navigation/Sidebar.jsx";
import {faCogs, faInfoCircle, faNetworkWired, faServer} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";
import AlertPopupArea from "../../alert/AlertPopupArea.jsx";
import Header from "../../navigation/Header.jsx";
import {useMetadata} from "../../../hooks/metadataHook.jsx";
import {useTranslation} from "react-i18next";

export const SidebarUseCase = () => {
    const {t} = useTranslation();
    const {displayedServerName} = useMetadata();

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
        <div id={"wrapper"}>
            <Sidebar page={'example'} items={items} openItemIndex={3} keepOpen/>
            <div className="d-flex flex-column" id="content-wrapper">
                <AlertPopupArea/>
                <Header page={displayedServerName} tab={t('html.label.themeEditor.example')} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <div style={{width: "100%"}}></div>
                    </main>
                </div>
            </div>
        </div>
    )
};