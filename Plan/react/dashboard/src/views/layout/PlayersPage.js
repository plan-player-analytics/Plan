import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet} from "react-router-dom";
import {useNavigation} from "../../hooks/navigationHook";
import {faSearch} from "@fortawesome/free-solid-svg-icons";
import {NightModeCss} from "../../hooks/themeHook";
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {useMetadata} from "../../hooks/metadataHook";
import ErrorPage from "./ErrorPage";
import {staticSite} from "../../service/backendConfiguration";

const PlayersPage = () => {
    const {t, i18n} = useTranslation();
    const {isProxy, serverName} = useMetadata();

    const [error] = useState(undefined);
    const {sidebarItems, setSidebarItems} = useNavigation();

    const {currentTab, setCurrentTab} = useNavigation();

    useEffect(() => {
        const items = staticSite ? [] : [
            {name: 'html.label.links'},
            {name: 'html.label.query', icon: faSearch, href: "/query"},
        ]

        setSidebarItems(items);
        window.document.title = `Plan | Player list`;
        setCurrentTab('html.label.players')
    }, [t, i18n, setCurrentTab, setSidebarItems])

    // const {authRequired, user} = useAuth();
    const showBackButton = true; // TODO

    if (error) return <ErrorPage error={error}/>;

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

export default PlayersPage;