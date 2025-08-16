import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faDownload} from "@fortawesome/free-solid-svg-icons";
import ActionButton from "../input/ActionButton.jsx";
import {useTranslation} from "react-i18next";
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";

const DownloadButton = ({className, disabled}) => {
    const {t} = useTranslation();

    const {
        name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases
    } = useThemeEditContext();

    const download = () => {
        const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify({
            name,
            colors: currentColors,
            nightColors: currentNightColors,
            useCases: currentUseCases,
            nightModeUseCases: currentNightModeUseCases
        }));
        const dlAnchorElem = document.createElement('a');
        dlAnchorElem.setAttribute("href", dataStr);
        dlAnchorElem.setAttribute("download", name + ".json");
        document.body.appendChild(dlAnchorElem);
        dlAnchorElem.click();
        dlAnchorElem.remove();
    }

    return (
        <ActionButton className={className} onClick={download} disabled={disabled}>
            <FontAwesomeIcon icon={faDownload}/> {t('html.modal.version.download')}
        </ActionButton>
    )
};

export default DownloadButton