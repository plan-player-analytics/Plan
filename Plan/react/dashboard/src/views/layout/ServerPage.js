import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet, useOutletContext, useParams} from "react-router-dom";
import {useNavigation} from "../../hooks/navigationHook";
import {
    faCampground,
    faChartArea,
    faChartLine,
    faCogs,
    faCubes,
    faGlobe,
    faInfoCircle,
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
import {fetchExtensionData, fetchServerIdentity} from "../../service/serverService";
import ExtensionIcon from "../../components/extensions/ExtensionIcon";

const ServerPage = () => {
    const {t, i18n} = useTranslation();
    const {identifier} = useParams();
    const {isProxy, serverName} = useMetadata();

    const {
        data: serverIdentity,
        loadingError: identityLoadingError
    } = useDataRequest(fetchServerIdentity, [identifier]);
    const {
        data: extensionData,
        loadingError: extensionDataLoadingError
    } = useDataRequest(fetchExtensionData, [identifier]);

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
                    // {name: 'html.label.playerRetention', icon: faUsersViewfinder, href: "retention"},
                    {name: 'html.label.playerList', icon: faUserGroup, href: "players"},
                    {name: 'html.label.geolocations', icon: faGlobe, href: "geolocations"},
                ]
            },
            {name: 'html.label.performance', icon: faCogs, href: "performance"},
            {},
            {name: 'html.label.plugins'},
        ]

        if (extensionData) {
            items.push({name: 'html.label.pluginsOverview', icon: faCubes, href: "plugins-overview"})
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
    }, [t, i18n, extensionData])

    const {authRequired, loggedIn, user} = useAuth();
    if (authRequired && !loggedIn) return <MainPageRedirect/>

    const showBackButton = isProxy && (!authRequired || user.permissions.filter(perm => perm !== 'page.network').length);


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
    if (extensionDataLoadingError) {
        return <ErrorPage error={extensionDataLoadingError}/>
    }
    if (!extensionData) return <></>

    return (
        <>
            <NightModeCss/>
            <Sidebar items={sidebarItems} showBackButton={showBackButton}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={displayedServerName} tab={currentTab}/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <SwitchTransition>
                            <Outlet context={extensionData}/>
                        </SwitchTransition>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    )
}

export const useServer = () => {
    return useOutletContext();
}

export default ServerPage;