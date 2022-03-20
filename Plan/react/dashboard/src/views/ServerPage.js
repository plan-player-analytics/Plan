import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet} from "react-router-dom";
import {useNavigation} from "../hooks/navigationHook";
import {
    faCalendarAlt,
    faCampground,
    faChartArea,
    faChartLine,
    faCogs,
    faCubes,
    faGlobe,
    faInfoCircle,
    faSearch,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../hooks/authenticationHook";
import {NightModeCss} from "../hooks/themeHook";
import Sidebar from "../components/navigation/Sidebar";
import Header from "../components/navigation/Header";
import ErrorView from "./ErrorView";
import ColorSelectorModal from "../components/modal/ColorSelectorModal";
import {useMetadata} from "../hooks/metadataHook";

const ServerPage = () => {
    const {t} = useTranslation();
    const {isProxy, serverName} = useMetadata();

    const [error, setError] = useState(undefined);
    const [sidebarItems, setSidebarItems] = useState([]);

    const {currentTab} = useNavigation();

    useEffect(() => {
        const items = [
            {name: 'html.title.serverOverview', icon: faInfoCircle, href: "overview"},
            {},
            {name: 'html.sidebar.information'},
            {
                name: 'html.title.onlineActivity',
                icon: faChartArea,
                contents: [
                    {
                        nameShort: 'html.sidebar.overview',
                        name: 'html.title.playersOnlineOverview',
                        icon: faChartArea,
                        href: "online-activity"
                    },
                    {name: 'html.sidebar.sessions', icon: faCalendarAlt, href: "sessions"},
                    {name: 'html.sidebar.pvpPve', icon: faCampground, href: "pvppve"}
                ]
            },
            {
                name: 'html.sidebar.playerbase',
                icon: faUsers,
                contents: [
                    {
                        nameShort: 'html.sidebar.overview',
                        name: 'html.sidebar.playerbaseOverview',
                        icon: faChartLine,
                        href: "playerbase"
                    },
                    {name: 'html.sidebar.playerList', icon: faUsers, href: "players"},
                    {name: 'html.sidebar.geolocations', icon: faGlobe, href: "geolocations"},
                ]
            },
            {name: 'html.sidebar.performance', icon: faCogs, href: "performance"},
            {},
            {name: 'html.sidebar.plugins'},
            {name: 'html.side.pluginsOverview', icon: faCubes, href: "plugins-overview"},
            {},
            {name: 'html.sidebar.links'},
            {name: 'html.sidebar.query', icon: faSearch, href: "/query"},
        ]

        // player.extensions.map(extension => {
        //     return {
        //         name: `${t('html.side.plugins')} (${extension.serverName})`,
        //         icon: faCubes,
        //         href: `plugins/${encodeURIComponent(extension.serverName)}`
        //     }
        // }).forEach(item => items.push(item));

        setSidebarItems(items);
        window.document.title = `Plan | Server Analysis`;
    }, [t])

    const {authRequired, user} = useAuth();
    const showBackButton = isProxy && (!authRequired || user.permissions.filter(perm => perm !== 'page.network').length);

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

    const displayedServerName = !isProxy && serverName && serverName.startsWith('Server') ? "Plan" : serverName;
    return (
        <>
            <NightModeCss/>
            <Sidebar items={sidebarItems} showBackButton={showBackButton}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={displayedServerName} tab={currentTab}/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Outlet context={{}}/>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    )
}

export default ServerPage;