import React from "react";
import {useTheme} from "../../hooks/themeHook";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCheck, faCloudMoon, faPalette} from "@fortawesome/free-solid-svg-icons";
import {nameToContrastCssVariable, nameToCssVariable} from "../../util/colors";
import {Col, Modal, Row} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {useThemeStorage} from "../../hooks/context/themeContextHook.jsx";
import {useMetadata} from "../../hooks/metadataHook.jsx";
import ThemeOption from "../theme/ThemeOption.jsx";
import {useAuth} from "../../hooks/authenticationHook.jsx";
import AddThemeButton from "../theme/AddThemeButton.jsx";

const ColorSelectorButton = ({color, setColor, disabled, active}) => {
    const validCssColor = color => {
        return color === 'theme' ? 'reference-colors-theme' : color;
    }
    return (
        <button className={`btn color-chooser ${disabled ? "disabled" : ''} ${active ? 'active' : ''}`}
                style={{
                    color: nameToContrastCssVariable(validCssColor(color)),
                    backgroundColor: nameToCssVariable(validCssColor(color))
                }}
                id={"choose-" + color}
                disabled={disabled}
                onClick={() => setColor(color)}
        >
            <Fa icon={active ? faCheck : faPalette}/>
        </button>
    )
}


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
                <button aria-label="Close" className="btn-close" onClick={theme.toggleColorChooser}/>
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
                    {metadata.availableThemes.map(themeName => <ThemeOption
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
                <button className="btn bg-theme" type="button" onClick={theme.toggleColorChooser}>OK</button>
            </Modal.Footer>
        </Modal>
    )
}

export default ColorSelectorModal;