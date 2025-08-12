import React, {useState} from 'react';
import {useMetadata} from "../../hooks/metadataHook.jsx";
import {faExclamationTriangle, faInfoCircle, faTrash} from "@fortawesome/free-solid-svg-icons";
import CardHeader from "../../components/cards/CardHeader.jsx";
import {Card, Col, Row} from "react-bootstrap";
import ThemeOption from "../../components/theme/ThemeOption.jsx";
import {ChartLoader} from "../../components/navigation/Loader.jsx";
import {ThemeContextProvider, useTheme} from "../../hooks/themeHook.jsx";
import {useTranslation} from "react-i18next";
import {
    getLocallyStoredThemes,
    ThemeStorageContextProvider,
    useThemeStorage
} from "../../hooks/context/themeContextHook.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import LoadIn from "../../components/animation/LoadIn.jsx";
import Checkbox from "../../components/input/Checkbox.jsx";
import DangerButton from "../../components/input/button/DangerButton.jsx";
import {ThemeEditContextProvider} from "../../hooks/context/themeEditContextHook.jsx";
import DownloadButton from "../../components/theme/DownloadButton.jsx";
import {useAuth} from "../../hooks/authenticationHook.jsx";
import {deleteTheme} from "../../service/metadataService.js";

const DeleteThemesView = () => {
    const {t} = useTranslation();
    const theme = useTheme();
    const metadata = useMetadata();
    const themeStorage = useThemeStorage();
    const [themeToDelete, setThemeToDelete] = useState('default');
    const [confirm, setConfirm] = useState(false);
    const {authRequired, hasPermission} = useAuth();

    if (!metadata.loaded) {
        return <ChartLoader/>
    }

    const onlyLocal = getLocallyStoredThemes().includes(themeToDelete);
    const canDelete = onlyLocal || authRequired && hasPermission('manage.themes');

    const onDelete = async () => {
        if (onlyLocal) {
            await themeStorage.deleteThemeLocally(themeToDelete);
        } else if (canDelete) {
            await deleteTheme(themeToDelete);
        }
        setConfirm(false);
        metadata.refreshThemeList();
        setThemeToDelete('default');
    }

    return (
        <LoadIn>
            {hasPermission('access.theme.editor') && <Card className="shadow mb-4 delete-theme" id="delete-theme">
                <CardHeader icon={faTrash} color="primary" label={t('html.label.themeEditor.deleteThemes')}/>
                <Card.Body>
                    <Row className={'mb-4'}>
                        <Col xs={12}>
                            <h5 className="mb-3">{t('html.label.themeEditor.themeToDelete')}</h5>
                            {!onlyLocal && <small><FontAwesomeIcon
                                icon={faInfoCircle}/> {t('html.label.themeEditor.canNotDeleteBuiltIn')}</small>}
                            <Row>
                                {metadata.getAvailableThemes().map(themeName => <ThemeOption
                                    key={themeName}
                                    theme={themeName}
                                    nightMode={theme.nightModeEnabled}
                                    selected={themeName === themeToDelete}
                                    setSelected={value => {
                                        setThemeToDelete(value);
                                        setConfirm(false);
                                    }}/>)}
                            </Row>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} className="mb-1">
                            <p className={"mb-1"}>{t('html.label.themeEditor.downloadThemeBeforeDeleting', {theme: themeToDelete})}</p>
                            <ThemeContextProvider themeOverride={themeToDelete} key={themeToDelete}>
                                <ThemeStorageContextProvider>
                                    <ThemeEditContextProvider>
                                        <DownloadButton/>
                                    </ThemeEditContextProvider>
                                </ThemeStorageContextProvider>
                            </ThemeContextProvider>
                        </Col>
                    </Row>
                    <hr/>
                    {canDelete && <Row>
                        <Col xs={12} className="mb-4">
                            <Checkbox checked={confirm} onChange={event => setConfirm(event.target.checked)}>
                                {t('html.label.themeEditor.confirmDelete', {theme: themeToDelete})}
                            </Checkbox>
                        </Col>
                    </Row>}
                    {canDelete && <Row>
                        <Col xs={12}>
                            <DangerButton onClick={onDelete} disabled={!confirm}>
                                <FontAwesomeIcon
                                    icon={faTrash}/> {t(onlyLocal ? 'html.label.themeEditor.deleteLocalTheme' : 'html.label.themeEditor.deleteTheme')}
                            </DangerButton>
                        </Col>
                    </Row>}
                    {!canDelete && <Row>
                        <Col xs={12}>
                            <p><FontAwesomeIcon
                                icon={faExclamationTriangle}/> {t('html.label.themeEditor.noPermissionToDelete')}</p>
                        </Col>
                    </Row>}
                </Card.Body>
            </Card>}
        </LoadIn>
    )
};

export default DeleteThemesView