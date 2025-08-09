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
import {faPlus, faSwatchbook, faTrash} from "@fortawesome/free-solid-svg-icons";

const ThemeEditorPage = () => {
    const {t} = useTranslation();
    const metadata = useMetadata();
    const title = t("html.label.themeEditor.title");
    const {identifier} = useParams();

    const items = metadata.loaded ? metadata.getAvailableThemes().map(theme => {
        return {name: theme, icon: faSwatchbook, href: theme}
    }) : [];
    items.push({name: t('html.label.themeEditor.addTheme'), icon: faPlus, href: 'new'});
    items.push({name: t('html.label.themeEditor.deleteThemes'), icon: faTrash, href: 'delete'});
    return (
        <>
            <Sidebar page={title} items={items}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={title} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
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