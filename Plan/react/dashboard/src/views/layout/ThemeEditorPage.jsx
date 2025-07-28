import React from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import {useTranslation} from "react-i18next";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {ThemeStyleCss} from "../../components/theme/ThemeStyleCss";
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import {SwitchTransition} from "react-transition-group";
import {Outlet} from "react-router-dom";

const ThemeEditorPage = () => {
    const {t} = useTranslation();
    const {name} = useThemeEditContext();

    const title = t("html.label.themeEditor.title");
    return (
        <>
            <ThemeStyleCss editMode/>
            <Sidebar page={title} items={[]}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={title} tab={name} hideUpdater/>
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
        </>
    );
};

export default ThemeEditorPage; 