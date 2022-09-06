import React, {useEffect} from "react";
import {useTranslation} from "react-i18next";
import {Outlet} from "react-router-dom";
import {useNavigation} from "../../hooks/navigationHook";
import {
    faChartLine,
    faCogs,
    faCubes,
    faGlobe,
    faInfoCircle,
    faNetworkWired,
    faSearch,
    faServer,
    faUserGroup,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../../hooks/authenticationHook";
import {NightModeCss} from "../../hooks/themeHook";
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {useMetadata} from "../../hooks/metadataHook";
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";
import {SwitchTransition} from "react-transition-group";
import MainPageRedirect from "../../components/navigation/MainPageRedirect";
import {ServerExtensionContextProvider, useServerExtensionContext} from "../../hooks/serverExtensionDataContext";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchNetworkMetadata} from "../../service/metadataService";
import {iconTypeToFontAwesomeClass} from "../../util/icons";

const NetworkSidebar = () => {
    const {t, i18n} = useTranslation();
    const {sidebarItems, setSidebarItems} = useNavigation();
    const {extensionData} = useServerExtensionContext();

    const {data: networkMetadata} = useDataRequest(fetchNetworkMetadata, [])

    useEffect(() => {
        const servers = networkMetadata?.servers || [];
        const items = [
            {name: 'html.label.networkOverview', icon: faInfoCircle, href: "overview"},
            {},
            {
                name: 'html.label.servers',
                icon: faServer,
                contents: [
                    {
                        nameShort: 'html.label.overview',
                        name: 'html.label.servers',
                        icon: faNetworkWired,
                        href: "serversOverview"
                    },
                    {name: 'html.label.sessions', icon: faCalendarCheck, href: "sessions"},
                    {name: 'html.label.performance', icon: faCogs, href: "performance"},
                    {},
                    ...servers.map(server => {
                        return {
                            name: server.serverName,
                            icon: faServer,
                            href: "/server/" + server.serverUUID,
                            color: 'light-green'
                        }
                    })
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
                    // {name: 'html.label.playerRetention', icon: faUsersViewfinder, href: "retention"},
                    {name: 'html.label.playerList', icon: faUserGroup, href: "players"},
                    {name: 'html.label.geolocations', icon: faGlobe, href: "geolocations"},
                ]
            },
            {},
            {name: 'html.label.plugins'},
            {name: 'html.label.pluginsOverview', icon: faCubes, href: "plugins-overview"}
        ]

        if (extensionData) {
            extensionData.extensions.filter(extension => extension.wide)
                .map(extension => extension.extensionInformation)
                .map(info => {
                    return {
                        name: info.pluginName,
                        icon: [iconTypeToFontAwesomeClass(info.icon.family), info.icon.iconName],
                        href: `plugins/${encodeURIComponent(info.pluginName)}`
                    }
                }).forEach(item => items.push(item))
        }

        items.push(
            {},
            {name: 'html.label.links'},
            {name: 'html.label.query', icon: faSearch, href: "/query"}
        );

        setSidebarItems(items);
        window.document.title = `Plan | Network`;
    }, [t, i18n, extensionData, setSidebarItems, networkMetadata])

    return (
        <Sidebar items={sidebarItems} showBackButton={false}/>
    )
}

const ServerPage = () => {
    const {networkName, serverUUID} = useMetadata();

    const {currentTab} = useNavigation();

    const {authRequired, loggedIn} = useAuth();
    if (authRequired && !loggedIn) return <MainPageRedirect/>

    if (!serverUUID) return <></>

    return (
        <>
            <NightModeCss/>
            <ServerExtensionContextProvider identifier={serverUUID}>
                <NetworkSidebar/>
                <div className="d-flex flex-column" id="content-wrapper">
                    <Header page={networkName} tab={currentTab}/>
                    <div id="content" style={{display: 'flex'}}>
                        <main className="container-fluid mt-4">
                            <SwitchTransition>
                                <Outlet/>
                            </SwitchTransition>
                        </main>
                        <aside>
                            <ColorSelectorModal/>
                        </aside>
                    </div>
                </div>
            </ServerExtensionContextProvider>
        </>
    )
}

export default ServerPage;