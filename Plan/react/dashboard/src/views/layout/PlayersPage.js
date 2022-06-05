import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet} from "react-router-dom";
import {useNavigation} from "../../hooks/navigationHook";
import {faSearch} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../../hooks/authenticationHook";
import {NightModeCss} from "../../hooks/themeHook";
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ErrorView from "../ErrorView";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {useMetadata} from "../../hooks/metadataHook";

const PlayersPage = () => {
    const {t, i18n} = useTranslation();
    const {isProxy, serverName} = useMetadata();

    const [error] = useState(undefined);
    const [sidebarItems, setSidebarItems] = useState([]);

    const {currentTab} = useNavigation();

    useEffect(() => {
        const items = [
            {name: 'html.label.links'},
            {name: 'html.label.query', icon: faSearch, href: "/query"},
        ]

        setSidebarItems(items);
        window.document.title = `Plan | Player list`;
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
            <Sidebar items={sidebarItems} showBackButton={true}/>
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