import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet} from "react-router-dom";
import {useNavigation} from "../hooks/navigationHook";
import {
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
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";

const ServerPage = () => {
    const {t, i18n} = useTranslation();
    const {isProxy, serverName} = useMetadata();

    const [error] = useState(undefined);
    const [sidebarItems, setSidebarItems] = useState([]);

    const {currentTab} = useNavigation();

    useEffect(() => {
        const items = [
            {name: 'html.label.serverOverview', icon: faInfoCircle, href: "overview"},
            {},
            {name: 'html.label.information'},
            {
                name: 'html.label.onlineActivity',
                icon: faChartArea,
                contents: [
                    {
                        nameShort: 'html.label.overview',
                        name: 'html.label.playersOnlineOverview',
                        icon: faChartArea,
                        href: "online-activity"
                    },
                    {name: 'html.label.sessions', icon: faCalendarCheck, href: "sessions"},
                    {name: 'html.label.pvpPve', icon: faCampground, href: "pvppve"}
                ]
            },
            {
                name: 'html.label.playerbase',
                icon: faUsers,
                contents: [
                    {
                        nameShort: 'html.label.overview',
                        name: 'html.label.playerbaseOverview',
                        icon: faChartLine,
                        href: "playerbase"
                    },
                    {name: 'html.label.playerList', icon: faUsers, href: "players"},
                    {name: 'html.label.geolocations', icon: faGlobe, href: "geolocations"},
                ]
            },
            {name: 'html.label.performance', icon: faCogs, href: "performance"},
            {},
            {name: 'html.label.plugins'},
            {name: 'html.label.pluginsOverview', icon: faCubes, href: "plugins-overview"},
            {},
            {name: 'html.label.links'},
            {name: 'html.label.query', icon: faSearch, href: "/query"},
        ]

        // TODO Extensions

        setSidebarItems(items);
        window.document.title = `Plan | Server Analysis`;
    }, [t, i18n])

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