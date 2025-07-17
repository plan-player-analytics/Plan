import React, {useState} from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import {faFileSignature, faPalette} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {Card, Col, Row} from "react-bootstrap";
import CardHeader from "../../components/cards/CardHeader";
import {ThemeStyleCss} from "../../components/theme/ThemeStyleCss";
import ExampleSection from "../../components/theme/ExampleSection.jsx";
import ColorSection from "../../components/theme/ColorSection.jsx";
import {ColorEditContextProvider} from "../../hooks/context/colorEditContextHook.jsx";
import ColorEditForm from "../../components/theme/ColorEditForm.jsx";
import UseCaseSection from "../../components/theme/UseCaseSection.jsx";
import TextInput from "../../components/input/TextInput.jsx";
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import EditorMenuToast from "../../components/theme/EditorMenuToast.jsx";

const ThemeEditorPage = () => {
    const {t} = useTranslation();
    const {
        name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases,
        deleteColor,
        deleteNightColor,
        saveColor,
        saveNightColor,
        updateUseCase,
        updateNightUseCase,
        removeNightOverride
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
        <>
            <ThemeStyleCss theme={{colors: currentColors, nightColors: currentNightColors}} useCases={currentUseCases}
                           nightModeUseCases={currentNightModeUseCases}/>
            <Sidebar page={title} items={[]}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={title} tab={name} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Card className="shadow mb-4 theme-editor">
                            <EditorMenuToast/>
                            <CardHeader icon={faPalette} color="primary" label={title}/>
                            <Card.Body>
                                <Row onMouseEnter={() => onHoverChange(undefined, 'enter', false)} className={'mb-4'}>
                                    <Col xs={12}>
                                        <h5 className="mb-3">{t('html.label.themeEditor.themeName')}</h5>
                                        <TextInput icon={faFileSignature}
                                                   isInvalid={newValue => !newValue.length || newValue.length > 100}
                                                   invalidFeedback={t('html.label.themeEditor.invalidName')}
                                                   placeholder={t('html.label.themeEditor.themeName')}
                                                   value={name}
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
                    </main>
                    <aside>
                        <ColorSelectorModal/>
                    </aside>
                </div>
            </div>
        </>
    );
};

export default ThemeEditorPage; 