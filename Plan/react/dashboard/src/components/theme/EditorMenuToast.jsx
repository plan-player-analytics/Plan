import React, {useEffect, useState} from 'react';
import {Toast} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faHistory, faRedoAlt, faUndoAlt} from "@fortawesome/free-solid-svg-icons";
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import ActionButton from "../input/button/ActionButton.jsx";
import {useTranslation} from "react-i18next";
import {unstable_usePrompt} from "react-router-dom";
import ThemeEditHistory from "./ThemeEditHistory.jsx";
import OutlineButton from "../input/button/OutlineButton.jsx";
import ThemeEditIssues from "./ThemeEditIssues.jsx";

const EditorMenuToast = () => {
    const {t} = useTranslation();
    const {editCount, redoCount, undo, redo, savePossible} = useThemeEditContext();
    const [historyOpen, setHistoryOpen] = useState(false);
    const toggleHistory = () => {
        setHistoryOpen(!historyOpen);
    }

    const [scrolled, setScrolled] = useState(false);
    useEffect(() => {
        const onScroll = () => {
            setScrolled(window.scrollY > 80); // adjust threshold as needed
        };
        window.addEventListener("scroll", onScroll);
        return () => window.removeEventListener("scroll", onScroll);
    }, []);

    useEffect(() => {
        const handleKeyDown = (e) => {
            // For Mac: metaKey (Cmd), for Windows/Linux: ctrlKey
            const isUndo = (e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z' && !e.shiftKey;
            const isRedo = (e.ctrlKey || e.metaKey) && (
                (e.key.toLowerCase() === 'y') ||
                (e.key.toLowerCase() === 'z' && e.shiftKey)
            );

            if (isUndo && editCount > 0) {
                e.preventDefault();
                undo();
            } else if (isRedo && redoCount > 0) {
                e.preventDefault();
                redo();
            }
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [editCount, redoCount, undo, redo]);

    useEffect(() => {
        if (editCount > 0) {
            const handleBeforeUnload = (event) => {
                event.preventDefault();
                event.returnValue = '';
            };
            window.addEventListener('beforeunload', handleBeforeUnload);
            return () => window.removeEventListener('beforeunload', handleBeforeUnload);
        }
    }, [editCount]);

    unstable_usePrompt({when: editCount > 0, message: t('html.label.themeEditor.unsavedChanges')});

    if (editCount === 0 && redoCount === 0) return <></>

    return (
        <Toast className={`editor-toast ${scrolled && 'scrolled'}`}>
            <Toast.Body>
                <ActionButton className={'me-1'} onClick={undo} disabled={editCount === 0}><FontAwesomeIcon
                    icon={faUndoAlt}/> {t('html.label.themeEditor.undo')}</ActionButton>
                <ActionButton className={'me-1'} onClick={redo} disabled={redoCount === 0}><FontAwesomeIcon
                    icon={faRedoAlt}/> {t('html.label.themeEditor.redo')}</ActionButton>
                <OutlineButton onClick={toggleHistory}><FontAwesomeIcon
                    icon={faHistory}/> {t(historyOpen ? 'html.label.themeEditor.hideHistory' : 'html.label.themeEditor.showHistory')}
                </OutlineButton>
                {!savePossible && editCount > 0 && <ThemeEditIssues/>}
                {historyOpen && <ThemeEditHistory/>}
            </Toast.Body>
        </Toast>
    )
};

export default EditorMenuToast