import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {useMetadata} from "../../hooks/metadataHook";
import {useNavigation} from "../../hooks/navigationHook";
import {staticSite} from "../../service/backendConfiguration";
import {faSearch, faUsersGear} from "@fortawesome/free-solid-svg-icons";
import {useAuth} from "../../hooks/authenticationHook";
import MainPageRedirect from "../../components/navigation/MainPageRedirect";
import ErrorPage from "./ErrorPage";
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import {Outlet} from "react-router-dom";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import AlertPopupArea from "../../components/alert/AlertPopupArea";

const HelpModal = React.lazy(() => import("../../components/modal/HelpModal"));

const ManagePage = () => {
    const {t, i18n} = useTranslation();
    const {displayedServerName} = useMetadata();

    const [error] = useState(undefined);
    const {sidebarItems, setSidebarItems, currentTab, setCurrentTab} = useNavigation();

    useEffect(() => {
        const items = staticSite ? [] : [
            {name: 'html.label.manage'},
            {name: 'html.label.groupPermissions', icon: faUsersGear, href: "groups"},
            // {name: 'html.label.groupUsers', icon: faUserGroup, href: "groupUsers"},
            // {name: 'html.label.users', icon: faUser, href: "users"},
            {name: 'html.label.links'},
            {name: 'html.label.query', icon: faSearch, href: "/query"},
        ]

        setSidebarItems(items);
        window.document.title = `Plan | Manage`;
        setCurrentTab('html.label.manage')
    }, [t, i18n, setCurrentTab, setSidebarItems])

    const {authRequired, loggedIn} = useAuth();
    if (authRequired && !loggedIn) return <MainPageRedirect/>;
    if (error) return <ErrorPage error={error}/>;

    return (
        <>
            <Sidebar items={sidebarItems}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <AlertPopupArea/>
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
        </>
    )
}

export default ManagePage;