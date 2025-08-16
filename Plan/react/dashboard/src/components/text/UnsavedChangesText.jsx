import React from 'react';
import {useTranslation} from "react-i18next";
import {useConfigurationStorageContext} from "../../hooks/context/configurationStorageContextHook.jsx";

const UnsavedChangesText = ({visible, className}) => {
    const {t} = useTranslation();
    const {dirty} = useConfigurationStorageContext();
    const show = visible !== undefined ? visible : dirty;
    if (show) {
        return (
            <p className={className} style={{
                display: "inline-block",
                marginLeft: "1rem",
                marginBottom: 0,
                opacity: 0.6
            }}>{t('html.label.managePage.changes.unsaved')}</p>
        )
    } else {
        return <></>
    }
}

export default UnsavedChangesText