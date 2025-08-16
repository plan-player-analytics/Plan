import React from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import {useTranslation} from "react-i18next";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {ThemeStyleCss} from "../../components/theme/ThemeStyleCss";
import {ThemeEditContextProvider} from "../../hooks/context/themeEditContextHook.jsx";
import {SwitchTransition} from "react-transition-group";
import {Outlet, useParams} from "react-router-dom";
import {ThemeContextProvider, useTheme} from "../../hooks/themeHook.jsx";
import {ThemeStorageContextProvider, useThemeStorage} from "../../hooks/context/themeContextHook.jsx";
import {ChartLoader} from "../../components/navigation/Loader.jsx";
import {useMetadata} from "../../hooks/metadataHook.jsx";
import {faInfoCircle, faPlus, faSwatchbook, faTrash} from "@fortawesome/free-solid-svg-icons";
import ErrorView from "../ErrorView.jsx";
import AlertPopupArea from "../../components/alert/AlertPopupArea.jsx";
import ErrorPage from "./ErrorPage.jsx";
import {Alert} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useAuth} from "../../hooks/authenticationHook.jsx";

const ThemeEditorPage = () => {
    const {t} = useTranslation();
    const metadata = useMetadata();
    const title = t("html.label.themeEditor.title");
    const {identifier} = useParams();
    const {nightModeEnabled} = useTheme();
    const {authRequired, loggedIn} = useAuth();

    if (authRequired && !loggedIn) return <MainPageRedirect/>;
    if (metadata.metadataError) {
        return <ErrorPage error={metadata.metadataError}/>
    }

    const items = metadata.loaded ? metadata.getAvailableThemes().map(theme => {
        return {name: theme, icon: faSwatchbook, href: theme}
    }) : [];
    items.push({name: t('html.label.themeEditor.addTheme'), icon: faPlus, href: 'new'});
    items.push({name: t('html.label.themeEditor.deleteThemes'), icon: faTrash, href: 'delete'});
    return (
        <>
            <Sidebar page={title} items={items}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <AlertPopupArea/>
                <Header page={title} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        {nightModeEnabled && <Alert variant={"warning"}>
                            <FontAwesomeIcon icon={faInfoCircle}/> {t('html.label.themeEditor.lightModeInfo')}
                        </Alert>}
                        <ThemeContextProvider themeOverride={identifier} key={identifier}>
                            <ThemeStorageContextProvider>
                                <WaitUntilThemeLoads/>
                            </ThemeStorageContextProvider>
                        </ThemeContextProvider>
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    );
};

const WaitUntilThemeLoads = () => {
    const theme = useThemeStorage();
    if (theme.error) return <ErrorView error={theme.error}/>
    if (!theme.loaded) return <ChartLoader/>

    return (
        <ThemeEditContextProvider>
            <ThemeStyleCss editMode applyToClass={'theme-editor-wrapper'}/>
            <div className={'theme-editor-wrapper'}>
                <SwitchTransition>
                    <Outlet/>
                </SwitchTransition>
            </div>
        </ThemeEditContextProvider>
    )
}

export default ThemeEditorPage; 