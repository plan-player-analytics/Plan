import React, {useEffect} from "react";
import Sidebar from "../../components/navigation/Sidebar";
import {Outlet, useOutletContext, useParams} from "react-router";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {fetchPlayer} from "../../service/playerService";
import {faCampground, faCubes, faInfoCircle, faNetworkWired} from "@fortawesome/free-solid-svg-icons";
import Header from "../../components/navigation/Header";
import {useNavigation} from "../../hooks/navigationHook";
import {useTranslation} from "react-i18next";
import {faCalendarCheck} from "@fortawesome/free-regular-svg-icons";
import {useDataRequest} from "../../hooks/dataFetchHook";
import ErrorPage from "./ErrorPage";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import MainPageRedirect from "../../components/navigation/MainPageRedirect";
import {SwitchTransition} from "react-transition-group";
import {ChartLoader} from "../../components/navigation/Loader.jsx";

const HelpModal = React.lazy(() => import("../../components/modal/HelpModal"));

const PlayerPage = () => {
    const {t, i18n} = useTranslation();
    const {hasChildPermission} = useAuth();
    const seePlayer = hasChildPermission('access.player')

    const {sidebarItems, setSidebarItems} = useNavigation();

    const {identifier} = useParams();
    const {currentTab, finishUpdate} = useNavigation();

    const {data: player, loadingError} = useDataRequest(fetchPlayer, [identifier], seePlayer)

    useEffect(() => {
        if (!player) return;

        const items = [
            {
                name: 'html.label.playerOverview',
                icon: faInfoCircle,
                href: "overview",
                permission: 'page.player.overview'
            },
            {name: 'html.label.sessions', icon: faCalendarCheck, href: "sessions", permission: 'page.player.sessions'},
            {name: 'html.label.pvpPve', icon: faCampground, href: "pvppve", permission: 'page.player.versus'},
            {name: 'html.label.servers', icon: faNetworkWired, href: "servers", permission: 'page.player.servers'}
        ]

        items.push({
            name: 'html.label.plugins',
            permission: 'page.player.plugins',
            icon: faCubes,
            contents: player?.extensions?.filter(extension => extension?.extensionData?.length)
                .map(extension => {
                    return {
                        name: `${t('html.label.plugins')} (${extension.serverName})`,
                        icon: faCubes,
                        href: `plugins/${encodeURIComponent(extension.serverName)}`,
                        permission: 'page.player.plugins'
                    }
                })
        });

        setSidebarItems(items);
        window.document.title = `Plan | ${player?.info?.name}`;

        finishUpdate(player.timestamp, player.timestamp_f);
    }, [player, t, i18n, finishUpdate, setSidebarItems])

    const {authRequired, loggedIn} = useAuth();
    if (authRequired && !loggedIn) return <MainPageRedirect/>;
    if (loadingError) return <ErrorPage error={loadingError}/>;

    return (
        <>
            <Sidebar page={player?.info?.name} items={player ? sidebarItems : []}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={player?.info?.name} tab={currentTab}/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        {player && <SwitchTransition>
                            <Outlet context={{player: player}}/>
                        </SwitchTransition>}
                        {!player && <ChartLoader/>}
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                        <React.Suspense fallback={""}><HelpModal/></React.Suspense>
                    </aside>
                </div>
            </div>
        </>
    )
}

export const usePlayer = () => {
    return useOutletContext();
}

export default PlayerPage;