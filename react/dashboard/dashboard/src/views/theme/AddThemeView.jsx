import React, {useState} from 'react';
import {useMetadata} from "../../hooks/metadataHook.jsx";
import {faFileSignature, faInfoCircle, faPlusCircle} from "@fortawesome/free-solid-svg-icons";
import CardHeader from "../../components/cards/CardHeader.jsx";
import {Card, Col, Row} from "react-bootstrap";
import TextInput from "../../components/input/TextInput.jsx";
import ThemeOption from "../../components/theme/ThemeOption.jsx";
import {ChartLoader} from "../../components/navigation/Loader.jsx";
import {useTheme} from "../../hooks/themeHook.jsx";
import ActionButton from "../../components/input/button/ActionButton.jsx";
import {useTranslation} from "react-i18next";
import {useThemeStorage} from "../../hooks/context/themeContextHook.jsx";
import {useNavigate} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import LoadIn from "../../components/animation/LoadIn.jsx";
import ErrorView from "../ErrorView.jsx";
import {useAuth} from "../../hooks/authenticationHook.jsx";

const AddThemeView = () => {
    const {hasPermission} = useAuth();
    const {t} = useTranslation();
    const navigate = useNavigate()
    const theme = useTheme();
    const metadata = useMetadata();
    const themeStorage = useThemeStorage();
    const [name, setName] = useState('');
    const [basedOnTheme, setBasedOnTheme] = useState('default');

    if (metadata.metadataError) {
        return <ErrorView error={metadata.metadataError}/>
    }
    if (!metadata.loaded) {
        return <ChartLoader/>
    }

    const createTheme = async () => {
        if (await themeStorage.cloneThemeLocally(basedOnTheme, name)) {
            metadata.refreshThemeList();
            navigate("/theme-editor/" + name);
        }
    }

    const onUploadFinished = (event) => {
        const theme = JSON.parse(event.target.result);
        themeStorage.saveUploadedThemeLocally(name, theme);
        navigate("/theme-editor/" + name);
    }
    const onUpload = (event) => {
        const reader = new FileReader();
        reader.onload = onUploadFinished;
        reader.readAsText(event.target.files[0]);
    }

    const isNameInvalid = newValue => {
        return !newValue.length
            || newValue.length > 100
            || metadata.getAvailableThemes()?.includes(newValue)
            || name === 'new' || name === 'delete'
    }
    const nameIsInvalid = isNameInvalid(name);
    return (
        <LoadIn>
            {hasPermission('access.theme.editor') && <Card className="shadow mb-4 add-theme" id={"add-theme"}>
                <CardHeader icon={faPlusCircle} color="primary" label={t('html.label.themeEditor.addTheme')}/>
                <Card.Body>
                    <Row className={'mb-4'}>
                        <Col xs={12}>
                            <h5 className="mb-3">{t('html.label.themeEditor.basedOnTheme')}</h5>
                            <Row>
                                {metadata.getAvailableThemes().map(themeName => <ThemeOption
                                    key={themeName}
                                    theme={themeName}
                                    nightMode={theme.nightModeEnabled}
                                    selected={themeName === basedOnTheme}
                                    setSelected={setBasedOnTheme}/>)}
                            </Row>
                        </Col>
                    </Row>
                    <Row className={'mb-4'}>
                        <Col xs={12}>
                            <TextInput icon={faFileSignature}
                                       isInvalid={isNameInvalid}
                                       invalidFeedback={t('html.label.themeEditor.invalidName')}
                                       placeholder={t('html.label.themeEditor.themeName')}
                                       value={name}
                                       setValue={newValue => setName(newValue.toLowerCase().replace(/[^a-z0-9-]/g, "-"))}
                            />
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12}>
                            {nameIsInvalid && <div className="disabled-feedback mb-1">
                                <FontAwesomeIcon icon={faInfoCircle}/> {t('html.label.themeEditor.nameWarning')}
                            </div>}
                            <ActionButton onClick={createTheme}
                                          disabled={nameIsInvalid}>{t('html.label.themeEditor.openEditor')}</ActionButton>
                            <p className={"mt-1 mb-1"}>{t('html.label.themeEditor.uploadTheme')}</p>
                            <input onChange={onUpload} type={"file"} disabled={nameIsInvalid}/>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>}
        </LoadIn>
    )
};

export default AddThemeView