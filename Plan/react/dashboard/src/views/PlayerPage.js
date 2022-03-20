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


const PlayerPage = () => {
    const {t} = useTranslation();

    const [player, setPlayer] = useState(undefined);
    const [error, setError] = useState(undefined);
    const [sidebarItems, setSidebarItems] = useState([]);

    const {identifier} = useParams();
    const {currentTab} = useNavigation();


    const updatePlayer = async (id) => {
        try {
            setPlayer(await fetchPlayer(id));
        } catch (e) {
            setError(e);
        }
    }

    useEffect(() => {
        updatePlayer(identifier)
    }, [identifier]);

    useEffect(() => {
        if (!player) return;

        const items = [
            {name: 'html.title.playerOverview', icon: faInfoCircle, href: "overview"},
            {name: 'html.sidebar.sessions', icon: faCalendarCheck, href: "sessions"},
            {name: 'html.sidebar.pvpPve', icon: faCampground, href: "pvppve"},
            {name: 'html.sidebar.servers', icon: faNetworkWired, href: "servers"}
        ]

        player.extensions.map(extension => {
            return {
                name: `${t('html.side.plugins')} (${extension.serverName})`,
                icon: faCubes,
                href: `plugins/${encodeURIComponent(extension.serverName)}`
            }
        }).forEach(item => items.push(item));

        setSidebarItems(items);
        window.document.title = `Plan | ${player.info.name}`;
    }, [player, t])

    const {authRequired, user} = useAuth();
    const showBackButton = !authRequired || user.permissions.filter(perm => perm !== 'page.player.self').length;

    if (error) {
        return <>
            <NightModeCss/>
            <Sidebar items={[]} showBackButton={true}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={error.title ? error.title : 'Unexpected error occurred'}/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <ErrorView error={error}/>
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
                        <Outlet context={{player, updatePlayer}}/>
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