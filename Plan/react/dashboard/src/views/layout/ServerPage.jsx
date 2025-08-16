import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet, useParams} from "react-router-dom";
import {useNavigation} from "../../hooks/navigationHook";
import {
    faCampground,
    faChartArea,
    faChartLine,
    faCodeCompare,
    faCogs,
    faCubes,
    faFilterCircleXmark,
    faGlobe,
    faInfoCircle,
    faLocationArrow,
    faSearch,
    faUserGroup,
    faUsers,
    faUsersViewfinder
} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../../hooks/authenticationHook";
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {useMetadata} from "../../hooks/metadataHook";
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";
import ErrorPage from "./ErrorPage";
import {SwitchTransition} from "react-transition-group";
import MainPageRedirect from "../../components/navigation/MainPageRedirect";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchServerIdentity} from "../../service/serverService";
import {ServerExtensionContextProvider, useServerExtensionContext} from "../../hooks/serverExtensionDataContext";
import {iconTypeToFontAwesomeClass} from "../../util/icons";
import {staticSite} from "../../service/backendConfiguration";

const HelpModal = React.lazy(() => import("../../components/modal/HelpModal"));

const ServerSidebar = () => {
    const {t, i18n} = useTranslation();
    const {authRequired} = useAuth();
    const {sidebarItems, setSidebarItems} = useNavigation();
    const {extensionData} = useServerExtensionContext();

    useEffect(() => {
        let items = [
            {
                name: 'html.label.serverOverview',
                icon: faInfoCircle,
                href: "overview",
                permission: 'page.server.overview'
            },
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
                        href: "online-activity",
                        permission: 'page.server.online.activity'
                    },
                    {
                        name: 'html.label.sessions',
                        icon: faCalendarCheck,
                        href: "sessions",
                        permission: 'page.server.sessions'
                    },
                    {
                        name: 'html.label.pvpPve',
                        icon: faCampground,
                        href: "pvppve",
                        permission: 'page.server.player.versus'
                    },
                    {
                        name: 'html.label.allowlist',
                        icon: faFilterCircleXmark,
                        href: "allowlist",
                        permission: 'page.server.allowlist.bounce'
                    }
                ],
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
                        permission: 'page.server.playerbase'
                    },
                    {
                        name: 'html.label.joinAddresses',
                        icon: faLocationArrow,
                        href: "join-addresses",
                        permission: 'page.server.join.addresses'
                    },
                    {
                        name: 'html.label.playerRetention',
                        icon: faUsersViewfinder,
                        href: "retention",
                        permission: 'page.server.retention'
                    },
                    {
                        name: 'html.label.playerList',
                        icon: faUserGroup,
                        href: "players",
                        permission: 'page.server.players'
                    },
                    {
                        name: 'html.label.geolocations',
                        icon: faGlobe,
                        href: "geolocations",
                        permission: 'page.server.geolocations'
                    },
                ]
            },
            {name: 'html.label.performance', icon: faCogs, href: "performance", permission: 'page.server.performance'},
            {},
            {name: 'html.label.plugins', permission: 'page.server.plugins'},
            {
                name: 'html.label.pluginHistory',
                icon: faCodeCompare,
                href: "plugin-history",
                permission: 'page.server.plugin.history',
                authRequired: true
            },
            {
                name: 'html.label.pluginsOverview',
                icon: faCubes,
                href: "plugins-overview",
                permission: 'page.server.plugins'
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
        window.document.title = `Plan | Server Analysis`;
    }, [t, i18n, extensionData, setSidebarItems, authRequired])

    return (
        <Sidebar items={sidebarItems}/>
    )
}

const ServerPage = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();
    const {isProxy, serverName, networkMetadata} = useMetadata();

    const {
        data: serverIdentity,
        loadingError: identityLoadingError
    } = useDataRequest(fetchServerIdentity, [identifier]);
    const [error] = useState(undefined);

    const {currentTab} = useNavigation();

    const {authRequired, loggedIn} = useAuth();
    if (authRequired && !loggedIn) return <MainPageRedirect/>

    const getDisplayedServerName = () => {
        if (serverIdentity) {
            return serverIdentity.serverName;
        }

        if (isProxy) {
            const fromMetadata = networkMetadata?.servers?.find(server => server.serverUUID === identifier);
            return fromMetadata ? fromMetadata.serverName : identifier;
        } else {
            return serverName?.startsWith('Server') ? "Plan" : serverName
        }
    }
    const displayedServerName = getDisplayedServerName();

    if (error) return <ErrorPage error={error}/>;
    if (identityLoadingError) {
        if (identityLoadingError.status === 404) return <ErrorPage
            error={{
                title: t('html.error.404NotFound'),
                message: t(staticSite ? 'html.error.serverNotExported' : 'html.error.serverNotSeen')
            }}/>
        return <ErrorPage error={identityLoadingError}/>
    }

    return (
        <ServerExtensionContextProvider identifier={identifier}>
            <ServerSidebar/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={displayedServerName} tab={currentTab}/>
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
    )
}

export default ServerPage;