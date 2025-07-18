import React, {useState} from 'react';
import {useTranslation} from "react-i18next";
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import {Card, Col, Row} from "react-bootstrap";
import EditorMenuToast from "../../components/theme/EditorMenuToast.jsx";
import CardHeader from "../../components/cards/CardHeader.jsx";
import TextInput from "../../components/input/TextInput.jsx";
import {ColorEditContextProvider} from "../../hooks/context/colorEditContextHook.jsx";
import ColorSection from "../../components/theme/ColorSection.jsx";
import ColorEditForm from "../../components/theme/ColorEditForm.jsx";
import UseCaseSection from "../../components/theme/UseCaseSection.jsx";
import ExampleSection from "../../components/theme/ExampleSection.jsx";
import {faFileSignature, faPalette} from "@fortawesome/free-solid-svg-icons";
import ActionButton from "../../components/input/ActionButton.jsx";
import UnsavedChangesText from "../../components/text/UnsavedChangesText.jsx";

const ThemeEditorView = () => {
    const {t} = useTranslation();
    const {
        name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases,
        setName,
        deleteColor,
        deleteNightColor,
        saveColor,
        saveNightColor,
        updateUseCase,
        updateNightUseCase,
        removeNightOverride,
        discardChanges, editCount, savePossible
    } = useThemeEditContext();
    const [hoveredItem, setHoveredItem] = useState(undefined);
    const [nightHover, setNightHover] = useState(false);
    const onHoverChange = (id, state, night) => {
        if (state === 'enter') {
            setHoveredItem(id);
            setNightHover(night);
        }
    }

    const referenceColors = currentUseCases.referenceColors;
    const nightReferenceColors = currentNightModeUseCases.referenceColors;

    const title = t("html.label.themeEditor.title");
    const colors = {...referenceColors, '-': "", ...currentColors};
    const nightColors = {...nightReferenceColors, '-': "", ...currentNightColors, ...colors};

    return (
        <Card className="shadow mb-4 theme-editor">
            <EditorMenuToast/>
            <CardHeader icon={faPalette} color="primary" label={title}>
                <button className={"btn bg-grey float-end"} onClick={discardChanges} disabled={editCount === 0}>
                    {t('html.label.managePage.changes.discard')}
                </button>
                <ActionButton className={"float-end me-2"}
                              disabled={!savePossible}>{t('html.label.managePage.changes.save')}</ActionButton>
                <UnsavedChangesText visible={editCount > 0} className={"float-end me-3"}/>
            </CardHeader>
            <Card.Body>
                <Row onMouseEnter={() => onHoverChange(undefined, 'enter', false)} className={'mb-4'}>
                    <Col xs={12}>
                        <h5 className="mb-3">{t('html.label.themeEditor.themeName')}</h5>
                        <TextInput icon={faFileSignature}
                                   isInvalid={newValue => !newValue.length || newValue.length > 100}
                                   invalidFeedback={t('html.label.themeEditor.invalidName')}
                                   placeholder={t('html.label.themeEditor.themeName')}
                                   value={name}
                                   disabled={name === 'Default'}
                                   disabledFeedback={t('html.label.themeEditor.defaultThemeNameFeedback')}
                                   setValue={newValue => setName(newValue)}
                        />
                    </Col>
                </Row>
                <Row onMouseEnter={() => onHoverChange(undefined, 'enter', false)}>
                    <Col xs={12}>
                        <ColorEditContextProvider
                            colors={nightColors}
                            saveFunction={saveColor}
                            deleteFunction={deleteColor}
                        >
                            <ColorSection title={t("html.label.themeEditor.colors")}
                                          colors={currentColors}/>
                            <ColorEditForm/>
                        </ColorEditContextProvider>
                        <ColorEditContextProvider
                            colors={nightColors}
                            saveFunction={saveNightColor}
                            deleteFunction={deleteNightColor}
                        >
                            <ColorSection title={t("html.label.themeEditor.nightColors")}
                                          colors={currentNightColors}/>
                            <ColorEditForm/>
                        </ColorEditContextProvider>
                    </Col>
                </Row>
                <hr/>
                <Row>
                    <Col xs={12}>
                        <Row>
                            <Col>
                                <UseCaseSection
                                    useCases={currentUseCases}
                                    colors={colors}
                                    updateUseCase={updateUseCase}
                                    onHoverChange={onHoverChange}
                                />
                            </Col>
                            <Col>
                                <UseCaseSection
                                    useCases={currentNightModeUseCases}
                                    colors={nightColors}
                                    baseUseCases={currentUseCases}
                                    isNightMode={true}
                                    updateUseCase={updateNightUseCase}
                                    removeOverride={removeNightOverride}
                                    onHoverChange={onHoverChange}
                                />
                            </Col>
                        </Row>
                    </Col>
                </Row>
            </Card.Body>
            <ExampleSection displayedItem={hoveredItem}
                            className={nightHover ? ' night-mode-colors' : ''}/>
        </Card>
    )
};

export default ThemeEditorView