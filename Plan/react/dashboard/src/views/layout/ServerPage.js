import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet, useParams} from "react-router-dom";
import {useNavigation} from "../../hooks/navigationHook";
import {
    faCampground,
    faChartArea,
    faChartLine,
    faCogs,
    faCubes,
    faGlobe,
    faInfoCircle,
    faLocationArrow,
    faSearch,
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
import ErrorPage from "./ErrorPage";
import {SwitchTransition} from "react-transition-group";
import MainPageRedirect from "../../components/navigation/MainPageRedirect";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchServerIdentity} from "../../service/serverService";
import ExtensionIcon from "../../components/extensions/ExtensionIcon";
import {ServerExtensionContextProvider, useServerExtensionContext} from "../../hooks/serverExtensionDataContext";

const ServerSidebar = () => {
    const {t, i18n} = useTranslation();
    const {sidebarItems, setSidebarItems} = useNavigation();
    const {extensionData} = useServerExtensionContext();
    const {authRequired, loggedIn, user} = useAuth();

    const {isProxy} = useMetadata();
    const showBackButton = isProxy
        && (!authRequired || (loggedIn && user.permissions.filter(perm => perm !== 'page.network').length));

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
                    {name: 'html.label.joinAddresses', icon: faLocationArrow, href: "join-addresses"},
                    // {name: 'html.label.playerRetention', icon: faUsersViewfinder, href: "retention"},
                    {name: 'html.label.playerList', icon: faUserGroup, href: "players"},
                    {name: 'html.label.geolocations', icon: faGlobe, href: "geolocations"},
                ]
            },
            {name: 'html.label.performance', icon: faCogs, href: "performance"},
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
                        icon: <ExtensionIcon icon={info.icon}/>,
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
        window.document.title = `Plan | Server Analysis`;
    }, [t, i18n, extensionData, setSidebarItems])

    return (
        <Sidebar items={sidebarItems} showBackButton={showBackButton}/>
    )
}

const ServerPage = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();
    const {isProxy, serverName} = useMetadata();

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
            return identifier;
        } else {
            return serverName && serverName.startsWith('Server') ? "Plan" : serverName
        }
    }
    const displayedServerName = getDisplayedServerName();

    if (error) return <ErrorPage error={error}/>;
    if (identityLoadingError) {
        if (identityLoadingError.status === 404) return <ErrorPage
            error={{title: t('html.error.404NotFound'), message: t('html.error.serverNotSeen')}}/>
        return <ErrorPage error={identityLoadingError}/>
    }

    return (
        <>
            <NightModeCss/>
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
                        </aside>
                    </div>
                </div>
            </ServerExtensionContextProvider>
        </>
    )
}

export default ServerPage;