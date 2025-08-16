import React from 'react';
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import {useTranslation} from "react-i18next";

const ThemeEditHistory = () => {
    const {t} = useTranslation()
    const {editCount, redoCount, edits, redos} = useThemeEditContext();

    if (editCount === 0 && redoCount === 0) {
        return <></>
    }

    return (
        <ul className={'edit-history'}>
            {redos.map((item, index) => {
                if (item.length) {
                    return <React.Fragment key={'redo' + index}>
                        <li className={'redo'}>{t('html.label.themeEditor.changes.discardedChanges')}</li>
                        {item.map((i, ix) => <li className={'redo nested'}
                                                 key={'redo' + index + '-' + ix}>{i.name}</li>)}
                    </React.Fragment>
                } else {
                    return <li className={'redo'} key={'redo' + index}>{item.name}</li>
                }
            })}
            {edits.toReversed().map((item, index) => <li className={'edit'} key={'history' + index}>{item.name}</li>)}
        </ul>
    )
};

export default ThemeEditHistory