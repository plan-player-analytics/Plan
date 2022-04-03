import React, {useEffect, useState} from "react";
import Sidebar from "../components/navigation/Sidebar";
import {Outlet, useOutletContext, useParams} from "react-router-dom";
import ColorSelectorModal from "../components/modal/ColorSelectorModal";
import {NightModeCss} from "../hooks/themeHook";
import {fetchPlayer} from "../service/playerService";
import ErrorView from "./ErrorView";
import {faCampground, faCubes, faInfoCircle, faNetworkWired} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../hooks/authenticationHook";
import Header from "../components/navigation/Header";
import {useNavigation} from "../hooks/navigationHook";
import {useTranslation} from "react-i18next";
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";
import {useDataRequest} from "../hooks/dataFetchHook";


const PlayerPage = () => {
    const {t, i18n} = useTranslation();

    const [sidebarItems, setSidebarItems] = useState([]);

    const {identifier} = useParams();
    const {currentTab, updateRequested, finishUpdate} = useNavigation();

    const {data: player, loadingError} = useDataRequest(fetchPlayer, [identifier, updateRequested])

    useEffect(() => {
        if (!player) return;

        const items = [
            {name: 'html.label.playerOverview', icon: faInfoCircle, href: "overview"},
            {name: 'html.label.sessions', icon: faCalendarCheck, href: "sessions"},
            {name: 'html.label.pvpPve', icon: faCampground, href: "pvppve"},
            {name: 'html.label.servers', icon: faNetworkWired, href: "servers"}
        ]

        player.extensions.map(extension => {
            return {
                name: `${t('html.label.plugins')} (${extension.serverName})`,
                icon: faCubes,
                href: `plugins/${encodeURIComponent(extension.serverName)}`
            }
        }).forEach(item => items.push(item));

        setSidebarItems(items);
        window.document.title = `Plan | ${player.info.name}`;

        finishUpdate(player.timestamp, player.timestamp_f);
    }, [player, t, i18n, finishUpdate])

    const {hasPermissionOtherThan} = useAuth();
    const showBackButton = hasPermissionOtherThan('page.player.self');

    if (loadingError) {
        return <>
            <NightModeCss/>
            <Sidebar items={[]} showBackButton={true}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={loadingError.title ? loadingError.title : 'Unexpected error occurred'}/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <ErrorView error={loadingError}/>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    }

    return player ? (
        <>
            <NightModeCss/>
            <Sidebar items={sidebarItems} showBackButton={showBackButton}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={player.info.name} tab={currentTab}/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Outlet context={{player: player}}/>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    ) : <>
        <NightModeCss/>
        <div className="page-loader">
            <div className="loader-container">
                <span className="loader"/>
                <p className="loader-text">Please wait..</p>
            </div>
        </div>
    </>
}

export const usePlayer = () => {
    return useOutletContext();
}

export default PlayerPage;