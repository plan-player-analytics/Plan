import React from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import {useTranslation} from "react-i18next";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {ThemeStyleCss} from "../../components/theme/ThemeStyleCss";
import {ThemeEditContextProvider} from "../../hooks/context/themeEditContextHook.jsx";
import {SwitchTransition} from "react-transition-group";
import {Outlet, useParams} from "react-router-dom";
import {ThemeContextProvider} from "../../hooks/themeHook.jsx";
import {ThemeStorageContextProvider, useThemeStorage} from "../../hooks/context/themeContextHook.jsx";
import {ChartLoader} from "../../components/navigation/Loader.jsx";
import {useMetadata} from "../../hooks/metadataHook.jsx";
import {faPlus, faSwatchbook} from "@fortawesome/free-solid-svg-icons";

const ThemeEditorPage = () => {
    const {identifier} = useParams();

    return (
        <ThemeContextProvider themeOverride={identifier}>
            <ThemeStorageContextProvider>
                <WaitUntilThemeLoads/>
            </ThemeStorageContextProvider>
        </ThemeContextProvider>
    );
};

const WaitUntilThemeLoads = () => {
    const {t} = useTranslation();
    const metadata = useMetadata();
    const title = t("html.label.themeEditor.title");

    const theme = useThemeStorage();
    if (!theme.loaded) return <ChartLoader/>

    const items = metadata.loaded ? metadata.availableThemes.map(theme => {
        return {name: theme, icon: faSwatchbook, href: theme}
    }) : [];
    items.push({name: 'Add theme', icon: faPlus, href: 'new'});
    return (
        <ThemeEditContextProvider>
            <ThemeStyleCss editMode/>
            <Sidebar page={title} items={items}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={title} hideUpdater/>
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
        </ThemeEditContextProvider>
    )
}

export default ThemeEditorPage; 