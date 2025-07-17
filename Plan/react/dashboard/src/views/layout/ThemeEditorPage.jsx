import React, {useEffect, useState} from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import {faPalette} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import theme from "../../theme.json";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {Card, Col, Row} from "react-bootstrap";
import CardHeader from "../../components/cards/CardHeader";
import useCases from "../../useCases.json";
import nightModeUseCases from "../../nightModeUseCases.json";
import {ThemeStyleCss} from "../../components/theme/ThemeStyleCss";
import ExampleSection from "../../components/theme/ExampleSection.jsx";
import ColorSection from "../../components/theme/ColorSection.jsx";
import {ColorEditContextProvider} from "../../hooks/context/colorEditContextHook.jsx";
import ColorEditForm from "../../components/theme/ColorEditForm.jsx";
import UseCaseSection from "../../components/theme/UseCaseSection.jsx";
import {nameToCssVariable} from "../../util/colors.js";
import {recursiveFindAndReplaceValue} from "../../util/mutator.js";

const ThemeEditorPage = () => {
    const {t} = useTranslation();
    const [currentColors, setCurrentColors] = useState(theme.colors);
    const [currentNightColors, setCurrentNightColors] = useState(theme.nightColors);
    const [currentUseCases, setCurrentUseCases] = useState(useCases);
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState(nightModeUseCases);
    const [hoveredItem, setHoveredItem] = useState(undefined);
    const [nightHover, setNightHover] = useState(false);
    useEffect(() => {
        setCurrentColors(theme.colors)
    }, [theme.colors]);
    useEffect(() => {
        setCurrentUseCases(useCases);
    }, [useCases]);
    useEffect(() => {
        setCurrentNightModeUseCases(nightModeUseCases);
    }, [nightModeUseCases]);
    const onHoverChange = (id, state, night) => {
        if (state === 'enter') {
            setHoveredItem(id);
            setNightHover(night);
        }
    }

    const referenceColors = currentUseCases.referenceColors;
    const nightReferenceColors = currentNightModeUseCases.referenceColors;

    const updateUseCaseColorName = (oldName, newName) => {
        const oldVariable = nameToCssVariable(oldName);
        const newVariable = nameToCssVariable(newName);
        setCurrentUseCases(recursiveFindAndReplaceValue(currentUseCases, oldVariable, newVariable));
        setCurrentNightModeUseCases(recursiveFindAndReplaceValue(currentNightModeUseCases, oldVariable, newVariable));
    }

    const handleColorSave = (current, setFunction) => (name, color, previous) => {
        const newObj = {};
        for (const [key, value] of Object.entries(current)) {
            if (key === previous) {
                newObj[name] = color;
            } else {
                newObj[key] = value;
            }
        }
        if (newObj[name] === undefined) {
            newObj[name] = color;
        }
        const renamed = name !== previous;
        if (renamed) updateUseCaseColorName(previous, name);
        setFunction(newObj);
    }

    const handleDelete = (current, setFunction) => (name) => {
        const copy = {...current};
        // TODO needs to handle use cases that are using the color
        delete copy[name];
        setFunction(copy);
    }

    const title = t("html.label.themeEditor.title");
    const colors = {...referenceColors, '-': "", ...currentColors};
    const nightColors = {...nightReferenceColors, '-': "", ...currentNightColors, ...colors};
    return (
        <>
            <ThemeStyleCss theme={{colors: currentColors, nightColors: currentNightColors}} useCases={currentUseCases}
                           nightModeUseCases={currentNightModeUseCases}/>
            <Sidebar page={title} items={[]}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={title} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Card className="shadow mb-4 theme-editor">
                            <CardHeader icon={faPalette} color="primary" label={title}/>
                            <Card.Body>
                                <Row onMouseEnter={() => onHoverChange(undefined, 'enter', false)}>
                                    <Col xs={12}>
                                        <ColorEditContextProvider
                                            colors={nightColors}
                                            saveFunction={handleColorSave(currentColors, setCurrentColors)}
                                            deleteFunction={handleDelete(currentColors, setCurrentColors)}
                                        >
                                            <ColorSection title={t("html.label.themeEditor.colors")}
                                                          colors={currentColors}/>
                                            <ColorEditForm/>
                                        </ColorEditContextProvider>
                                        <ColorEditContextProvider
                                            colors={nightColors}
                                            saveFunction={handleColorSave(currentNightColors, setCurrentNightColors)}
                                            deleteFunction={handleDelete(currentNightColors, setCurrentNightColors)}
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
                                                    onUpdate={setCurrentUseCases}
                                                    onHoverChange={onHoverChange}
                                                />
                                            </Col>
                                            <Col>
                                                <UseCaseSection
                                                    useCases={currentNightModeUseCases}
                                                    colors={nightColors}
                                                    baseUseCases={currentUseCases}
                                                    isNightMode={true}
                                                    onUpdate={setCurrentNightModeUseCases}
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