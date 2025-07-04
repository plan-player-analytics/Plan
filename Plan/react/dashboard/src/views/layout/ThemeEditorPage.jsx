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
import {mergeUseCases} from '../../util/mutator';
import ExampleSection from "../../components/theme/ExampleSection.jsx";
import ColorSection from "../../components/theme/ColorSection.jsx";
import {ColorEditContextProvider} from "../../hooks/context/colorEditContextHook.jsx";
import ColorEditForm from "../../components/theme/ColorEditForm.jsx";
import ColorDropdown from "../../components/theme/ColorDropdown.jsx";

const formatLabel = (key) => {
    // Convert camelCase to Title Case with spaces
    return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase())
        .trim();
};

const UseCase = ({path, value, onChange, onHoverChange, colors, isNightMode, baseValue, onRemoveOverride}) => {
    const level = Math.max(0, path.length - 1);
    const id = path.join('.');

    if (typeof value === 'string') {
        const hasOverride = isNightMode && value !== baseValue;
        return (
            <ColorDropdown
                id={id} key={id}
                colors={colors}
                value={value}
                onChange={(newValue) => onChange(newValue, path)}
                label={formatLabel(path[path.length - 1])}
                marginLeft={level * 20}
                onRemoveOverride={hasOverride ? () => onRemoveOverride?.(path) : null}
                onHoverChange={(a, b) => onHoverChange(a, b, isNightMode)}
            />
        );
    }

    if (Array.isArray(value)) {
        return null;
    }

    return (
        <>
            {typeof value === 'object' && !Array.isArray(value) && path.length > 0 && (
                <tr onMouseOver={() => onHoverChange(id, 'enter', isNightMode)}
                    onMouseOut={() => onHoverChange(id, 'exit', isNightMode)}>
                    <td colSpan={2}>
                        {level === 0 && <hr/>}
                        <h6 className={'mt-2 mb-3'} style={{marginLeft: level * 20, fontWeight: "bold"}}>
                            {formatLabel(path[path.length - 1])}
                        </h6>
                    </td>
                </tr>
            )}
            {Object.entries(value).map(([key, val]) => (
                <UseCase
                    key={key}
                    path={[...path, key]}
                    value={val}
                    onChange={onChange}
                    onHoverChange={onHoverChange}
                    colors={colors}
                    isNightMode={isNightMode}
                    baseValue={baseValue?.[key]}
                    onRemoveOverride={onRemoveOverride}
                />
            ))}
        </>
    );
};

const UseCaseSection = ({useCases, onHoverChange, colors, baseUseCases = null, isNightMode = false, onUpdate}) => {
    const {t} = useTranslation();
    // For night mode, we need to merge the base use cases with overrides
    const mergedUseCases = isNightMode && baseUseCases ? mergeUseCases(baseUseCases, useCases) : useCases;

    const handleColorChange = (newValue, path) => {
        const result = {...useCases};
        let current = result;
        for (let i = 0; i < path.length - 1; i++) {
            if (!current[path[i]] || typeof current[path[i]] !== 'object') {
                current[path[i]] = {};
            }
            current = current[path[i]];
        }
        current[path[path.length - 1]] = newValue;
        onUpdate?.(result);
    };

    const handleRemoveOverride = (path) => {
        // Create a new object without the override, but maintain structure
        const removeOverride = (obj, pathArr) => {
            if (pathArr.length === 0) return obj;

            const [current, ...rest] = pathArr;
            const result = {...obj};

            if (rest.length === 0) {
                // We've reached the target property, remove it
                delete result[current];
                // If the parent object becomes empty, return null to signal removal
                return Object.keys(result).length === 0 ? null : result;
            }

            // Continue traversing
            const nested = removeOverride(obj[current] || {}, rest);
            if (nested === null) {
                delete result[current];
                return Object.keys(result).length === 0 ? null : result;
            }
            result[current] = nested;
            return result;
        };

        // Get the new state without the override
        const newState = removeOverride(useCases, path) || {};

        // Update parent
        onUpdate?.(newState);
    };

    return (
        <div className={"ps-4 pt-4 pb-4 mb-4" + (isNightMode ? ' night-mode-colors' : '')}>
            <h5 className="mb-3">{isNightMode ? t('html.label.themeEditor.nightModeOverrides') : t('html.label.themeEditor.useCases')}</h5>
            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <tbody>
                <UseCase
                    path={[]}
                    value={mergedUseCases}
                    onChange={handleColorChange}
                    colors={colors}
                    isNightMode={isNightMode}
                    baseValue={baseUseCases}
                    onRemoveOverride={isNightMode ? handleRemoveOverride : undefined}
                    onHoverChange={onHoverChange}
                />
                </tbody>
            </table>
        </div>
    );
};

const ThemeEditorPage = () => {
    const {t} = useTranslation();
    const [currentColors, setCurrentColors] = useState(theme.colors);
    const [currentNightColors, setCurrentNightColors] = useState(theme.nightColors);
    const [currentUseCases, setCurrentUseCases] = useState(useCases);
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState(nightModeUseCases);
    const [hoveredItem, setHoveredItem] = useState(undefined);
    const [nightHover, setNightHover] = useState(false);
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

    // TODO change this to generate css a bit differently
    const referenceColors = {};
    Object.entries(currentUseCases).forEach(([key, value]) => {
        if (typeof value === 'string') {
            referenceColors[key] = value;
        }
    })
    const nightReferenceColors = {};
    Object.entries(currentNightModeUseCases).forEach(([key, value]) => {
        if (typeof value === 'string') {
            nightReferenceColors[key] = value;
        }
    })

    const handleColorSave = (current, setFunction) => (name, color, previous) => {
        const newObj = {};
        for (const [key, value] of Object.entries(current)) {
            if (key === previous) {
                // TODO needs to handle use cases that are using the color if renamed
                newObj[name] = color;
            } else {
                newObj[key] = value;
            }
        }
        if (newObj[name] === undefined) {
            newObj[name] = color;
        }
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
                        <Card className="shadow mb-4">
                            <CardHeader icon={faPalette} color="primary" label={title}/>
                            <Card.Body>
                                <Row>
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
                                            <Col xl={5} lg={12} md={12} sm={12} xs={12}>
                                                <UseCaseSection
                                                    useCases={currentUseCases}
                                                    colors={colors}
                                                    onUpdate={setCurrentUseCases}
                                                    onHoverChange={onHoverChange}
                                                />
                                            </Col>
                                            <Col xs={2} className={nightHover ? ' night-mode-colors' : ''}>
                                                <ExampleSection displayedItem={hoveredItem}/>
                                            </Col>
                                            <Col xl={5} lg={12} md={12} sm={12} xs={12}>
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