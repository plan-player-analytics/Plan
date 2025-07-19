import React from 'react';
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";

const ThemeEditIssues = () => {
    const {t} = useTranslation();
    const {issues} = useThemeEditContext();
    return (
        <ul className={'issues'}>
            <li className={'issue'}><FontAwesomeIcon
                icon={faExclamationTriangle}/> {issues.length} {t('html.label.themeEditor.issues.problems')}</li>
            {issues.map((item, index) => <li className={'issue'} key={'issue' + index}>{item}</li>)}
        </ul>
    )
};

export default ThemeEditIssues