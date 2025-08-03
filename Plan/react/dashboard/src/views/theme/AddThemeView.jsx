import React, {useState} from 'react';
import {useMetadata} from "../../hooks/metadataHook.jsx";
import {faFileSignature, faPalette} from "@fortawesome/free-solid-svg-icons";
import CardHeader from "../../components/cards/CardHeader.jsx";
import {Card, Col, Row} from "react-bootstrap";
import TextInput from "../../components/input/TextInput.jsx";
import ThemeOption from "../../components/theme/ThemeOption.jsx";
import {ChartLoader} from "../../components/navigation/Loader.jsx";
import {useTheme} from "../../hooks/themeHook.jsx";
import ActionButton from "../../components/input/ActionButton.jsx";
import {useTranslation} from "react-i18next";

const AddThemeView = () => {
    const {t} = useTranslation();
    const theme = useTheme();
    const metadata = useMetadata();
    const [name, setName] = useState('');
    const [basedOnTheme, setBasedOnTheme] = useState('default');

    if (!metadata.loaded) {
        return <ChartLoader/>
    }

    return (
        <Card className="shadow mb-4 add-theme">
            <CardHeader icon={faPalette} color="primary" label={'Add theme'}/>
            <Card.Body>
                <Row className={'mb-4'}>
                    <Col xs={12}>
                        <TextInput icon={faFileSignature}
                                   isInvalid={newValue => !newValue.length || newValue.length > 100 || name === 'default' || name === 'new'}
                                   invalidFeedback={t('html.label.themeEditor.invalidName')}
                                   placeholder={t('html.label.themeEditor.themeName')}
                                   value={name}
                                   setValue={newValue => setName(newValue)}
                        />
                    </Col>
                </Row>
                <Row className={'mb-4'}>
                    <Col xs={12}>
                        <h5 className="mb-3">{t('html.label.themeEditor.basedOnTheme')}</h5>
                        {metadata.availableThemes.map(themeName => <ThemeOption
                            key={themeName}
                            theme={themeName}
                            nightMode={theme.nightModeEnabled}
                            selected={themeName === basedOnTheme}
                            setSelected={setBasedOnTheme}/>)}
                    </Col>
                </Row>
                <Row>
                    <Col xs={12}>
                        <ActionButton>Start editing</ActionButton>
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    )
};

export default AddThemeView