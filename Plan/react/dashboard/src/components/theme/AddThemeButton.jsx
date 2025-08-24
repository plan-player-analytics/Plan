import React from 'react';
import OutlineButton from "../input/button/OutlineButton.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import {useNavigate} from "react-router";
import {useTheme} from "../../hooks/themeHook.jsx";
import {useTranslation} from "react-i18next";

const AddThemeButton = () => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const theme = useTheme();

    const onClick = () => {
        theme.toggleColorChooser();
        navigate("/theme-editor/new");
    }

    return (
        <OutlineButton style={{height: '82px', width: '100%', wordWrap: 'break-word'}} onClick={onClick}>
            <FontAwesomeIcon icon={faPlus}/> {t('html.label.themeEditor.addTheme')}
        </OutlineButton>
    )
};

export default AddThemeButton