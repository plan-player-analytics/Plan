import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Outlet} from "react-router";
import {useNavigation} from "../../hooks/navigationHook";
import {faUndo} from "@fortawesome/free-solid-svg-icons";
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {useMetadata} from "../../hooks/metadataHook";
import ErrorPage from "./ErrorPage";
import {QueryResultContextProvider} from "../../hooks/queryResultContext";
import {useAuth} from "../../hooks/authenticationHook";
import MainPageRedirect from "../../components/navigation/MainPageRedirect";

const HelpModal = React.lazy(() => import("../../components/modal/HelpModal"));

const QueryPage = () => {
    const {t, i18n} = useTranslation();
    const {isProxy, networkName, serverName} = useMetadata();

    const [error] = useState(undefined);
    const {sidebarItems, setSidebarItems} = useNavigation();

    const {currentTab, setCurrentTab} = useNavigation();

    useEffect(() => {
        const items = [
            {name: 'html.label.links'},
            {name: 'html.query.label.makeAnother', icon: faUndo, href: "/query", external: true},
        ]

        setSidebarItems(items);
        window.document.title = `Plan | Query`;
        setCurrentTab('html.query.title.text');
    }, [t, i18n, setCurrentTab, setSidebarItems])

    const {authRequired, loggedIn} = useAuth();
    if (authRequired && !loggedIn) return <MainPageRedirect/>;
    if (error) return <ErrorPage error={error}/>;

    const displayedServerName = isProxy ? networkName : (serverName?.startsWith('Server') ? "Plan" : serverName);
    return (
        <>
            <QueryResultContextProvider>
                <Sidebar items={sidebarItems}/>
                <div className="d-flex flex-column" id="content-wrapper">
                    <Header page={displayedServerName} tab={currentTab} hideUpdater/>
                    <div id="content" style={{display: 'flex'}}>
                        <main className="container-fluid mt-4">
                            <Outlet context={{}}/>
                        </main>
                        <aside>
                            <ColorSelectorModal/>
                            <React.Suspense fallback={""}><HelpModal/></React.Suspense>
                        </aside>
                    </div>
                </div>
            </QueryResultContextProvider>
        </>
    )
}

export default QueryPage;