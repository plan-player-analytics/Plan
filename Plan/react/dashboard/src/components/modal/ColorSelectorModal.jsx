import React from "react";
import {useTheme} from "../../hooks/themeHook";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCloudMoon, faPalette} from "@fortawesome/free-solid-svg-icons";
import {Col, Modal, Row} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {useThemeStorage} from "../../hooks/context/themeContextHook.tsx";
import {useMetadata} from "../../hooks/metadataHook.jsx";
import ThemeOption from "../theme/ThemeOption.jsx";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import AddThemeButton from "../theme/AddThemeButton.jsx";
import ActionButton from "../input/button/ActionButton.jsx";
import ColorSelectorButton from "../theme/ColorSelectorButton.jsx";
import ModalCloseButton from "../input/button/ModalCloseButton.jsx";

const ColorSelectorModal = () => {
    const {t} = useTranslation();
    const theme = useTheme();
    const metadata = useMetadata();
    const {loaded, currentUseCases, currentNightModeUseCases} = useThemeStorage();
    const {authLoaded, authRequired, hasPermission} = useAuth();

    const canEdit = authLoaded && (!authRequired || hasPermission('access.theme.editor'))
    const colorOptions = loaded ? (theme.nightModeEnabled ? currentNightModeUseCases : currentUseCases)['themeColorOptions'] : [];

    return (
        <Modal id="colorChooserModal"
               aria-labelledby="colorChooserModalLabel"
               show={theme.colorChooserOpen}
               onHide={theme.toggleColorChooser}>
            <Modal.Header>
                <Modal.Title id="colorChooserModalLabel">
                    <Fa icon={faPalette}/> {t('html.label.themeSelect')}
                </Modal.Title>
                <ModalCloseButton onClick={theme.toggleColorChooser}/>
            </Modal.Header>
            <Modal.Body style={{padding: "0.5rem 0 0.5rem 0.5rem"}}>
                <Row>
                    <Col>
                        <h5>{t('html.label.themeEditor.themeColorOptions')}</h5>
                        {colorOptions.map(color =>
                            <ColorSelectorButton
                                key={color}
                                color={color}
                                setColor={theme.setColor}
                                active={color === theme.color}
                            />)}
                    </Col>
                </Row>
                <hr/>
                {metadata.loaded && <Row style={{paddingRight: "0.3rem"}}>
                    <h5>{t('html.label.themeSelect')}</h5>
                    {metadata.getAvailableThemes().map(themeName => <ThemeOption
                        key={themeName}
                        theme={themeName}
                        nightMode={theme.nightModeEnabled}
                        selected={themeName === theme.currentTheme}/>)}
                    {canEdit && <Col xs={4}><AddThemeButton/></Col>}
                </Row>}
            </Modal.Body>
            <Modal.Footer>
                <button className="btn" id="night-mode-toggle" type="button" onClick={theme.toggleNightMode}>
                    <Fa icon={faCloudMoon}/> {t('html.button.nightMode')}
                </button>
                <ActionButton onClick={theme.toggleColorChooser}>OK</ActionButton>
            </Modal.Footer>
        </Modal>
    )
}

export default ColorSelectorModal;