import React, {useEffect} from 'react';
import {Toast} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faRedoAlt, faUndoAlt} from "@fortawesome/free-solid-svg-icons";
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import ActionButton from "../input/ActionButton.jsx";
import {useTranslation} from "react-i18next";

const EditorMenuToast = () => {
    const {t} = useTranslation();
    const {editCount, redoCount, undo, redo} = useThemeEditContext();

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

    if (editCount === 0 && redoCount === 0) return <></>

    return (
        <Toast className={"editor-toast"} style={{zIndex: 100}}>
            <Toast.Body>
                <ActionButton className={'me-1'} onClick={undo} disabled={editCount === 0}><FontAwesomeIcon
                    icon={faUndoAlt}/> Undo</ActionButton>
                <ActionButton onClick={redo} disabled={redoCount === 0}><FontAwesomeIcon
                    icon={faRedoAlt}/> Redo</ActionButton>
            </Toast.Body>
        </Toast>
    )
};

export default EditorMenuToast