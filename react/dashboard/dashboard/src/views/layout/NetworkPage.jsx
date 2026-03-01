import React, {useEffect} from "react";
import {useTranslation} from "react-i18next";
import {Outlet} from "react-router";
import {useNavigation} from "../../hooks/navigationHook";
import {
    faChartLine,
    faCodeCompare,
    faCogs,
    faCubes,
    faGlobe,
    faInfoCircle,
    faLocationArrow,
    faNetworkWired,
    faSearch,
    faServer,
    faUserGroup,
    faUsers,
    faUsersViewfinder
} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {useMetadata} from "../../hooks/metadataHook";
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";
import {SwitchTransition} from "react-transition-group";
import MainPageRedirect from "../../components/navigation/MainPageRedirect";
import {ServerExtensionContextProvider, useServerExtensionContext} from "../../hooks/serverExtensionDataContext";
import {iconTypeToFontAwesomeClass} from "../../util/icons";
import {staticSite} from "../../service/backendConfiguration";

const HelpModal = React.lazy(() => import("../../components/modal/HelpModal"));

const NetworkSidebar = () => {
    const {t, i18n} = useTranslation();
    const {authRequired} = useAuth();
    const {sidebarItems, setSidebarItems} = useNavigation();
    const {networkMetadata} = useMetadata();
    const {extensionData} = useServerExtensionContext();

    useEffect(() => {
        const servers = networkMetadata?.servers || [];
        let items = [
            {
                name: 'html.label.networkOverview',
                icon: faInfoCircle,
                href: "overview",
                permission: 'page.network.overview'
            },
            {},
            {name: 'html.label.information'},
            {
                name: 'html.label.servers',
                icon: faServer,
                contents: [
                    {
                        nameShort: 'html.label.overview',
                        name: 'html.label.servers',
                        icon: faNetworkWired,
                        href: "serversOverview",
                        permission: 'page.network.server.list'
                    },
                    {
                        name: 'html.label.sessions', icon: faCalendarCheck, href: "sessions",
                        permission: 'page.network.sessions'
                    },
                    staticSite ? undefined : {
                        name: 'html.label.performance', icon: faCogs, href: "performance",
                        permission: 'page.network.performance'
                    },
                    {},
                    ...servers
                        .filter(server => !server.proxy)
                        .map(server => {
                            return {
                                name: server.serverName,
                                icon: faServer,
                                href: "/server/" + server.serverUUID,
                                color: 'light-green',
                                permission: 'access.server.' + server.serverUUID
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
                        href: "playerbase",
                        permission: 'page.network.playerbase'
                    },
                    {
                        name: 'html.label.joinAddresses',
                        icon: faLocationArrow,
                        href: "join-addresses",
                        permission: 'page.network.join.addresses'
                    },
                    {
                        name: 'html.label.playerRetention',
                        icon: faUsersViewfinder,
                        href: "retention",
                        permission: 'page.network.retention'
                    },
                    {
                        name: 'html.label.playerList',
                        icon: faUserGroup,
                        href: "players",
                        permission: 'page.network.players'
                    },
                    {
                        name: 'html.label.geolocations',
                        icon: faGlobe,
                        href: "geolocations",
                        permission: 'page.network.geolocations'
                    },
                ]
            },
            {},
            {name: 'html.label.plugins', permission: 'page.network.plugins'},
            {
                name: 'html.label.pluginHistory',
                icon: faCodeCompare,
                href: "plugin-history",
                permission: 'page.network.plugin.history',
                authRequired: true
            },
            {
                name: 'html.label.pluginsOverview',
                icon: faCubes,
                href: "plugins-overview",
                permission: 'page.network.plugins'
            }
        ]

        if (extensionData?.extensions) {
            extensionData.extensions.filter(extension => extension.wide)
                .map(extension => extension.extensionInformation)
                .map(info => {
                    return {
                        name: info.pluginName,
                        icon: [iconTypeToFontAwesomeClass(info.icon.family), info.icon.iconName],
                        href: `plugins/${encodeURIComponent(info.pluginName)}`,
                        permission: 'page.network.plugins'
                    }
                }).forEach(item => items.push(item))
        }

        if (!staticSite) {
            items.push(
                {},
                {name: 'html.label.links', permission: 'access.query'},
                {name: 'html.label.query', icon: faSearch, href: "/query", permission: 'access.query'}
            );
        }
        // Filter out items that need authentication
        items = items
            .filter(item => !item.authRequired || (authRequired && item.authRequired))

        setSidebarItems(items);
        window.document.title = `Plan | Network`;
    }, [t, i18n, extensionData, setSidebarItems, networkMetadata, authRequired])

    return (
        <Sidebar items={sidebarItems}/>
    )
}

const NetworkPage = () => {
    const {networkName, serverUUID} = useMetadata();

    const {currentTab} = useNavigation();

    const {authRequired, loggedIn} = useAuth();
    if (authRequired && !loggedIn) return <MainPageRedirect/>

    if (!serverUUID) return <></>

    return (
        <>
            <ServerExtensionContextProvider identifier={serverUUID} proxy={true}>
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
                            <React.Suspense fallback={""}><HelpModal/></React.Suspense>
                        </aside>
                    </div>
                </div>
            </ServerExtensionContextProvider>
        </>
    )
}

export default NetworkPage;