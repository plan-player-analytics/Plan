import React, {useEffect, useRef, useState} from 'react';
import Sidebar from "../../components/navigation/Sidebar";
import Header from "../../components/navigation/Header";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faPalette, faTimes} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import theme from "../../theme.json";
import SideNavTabs from "../../components/layout/SideNavTabs";
import ColorSelectorModal from "../../components/modal/ColorSelectorModal";
import {Card, Col, Dropdown, Form, Row} from "react-bootstrap";
import CardHeader from "../../components/cards/CardHeader";
import useCases from "../../useCases.json";
import nightModeUseCases from "../../nightModeUseCases.json";
import {ThemeStyleCss} from "../../components/theme/ThemeStyleCss";
import {ColorBox} from '../../components/theme/ColorBox';
import {mergeUseCases} from '../../util/mutator';

const ColorDropdown = ({colors, value, onChange, label, onRemoveOverride = null, marginLeft = 0}) => {
    // Extract name from CSS variable or use first color as default
    const selectedName = value?.replace('var(--col-', '').replace(')', '') || Object.keys(colors)[0];
    const isTextColor = selectedName.includes('text') || label.includes('Text');
    const cssColor = `var(--col-${selectedName})`;
    const contrastColor = `var(--contrast-col-${selectedName})`;
    const [isOpen, setIsOpen] = useState(false);
    const selectedItemRef = useRef(null);
    const dropdownMenuRef = useRef(null);

    useEffect(() => {
        if (isOpen && selectedItemRef.current && dropdownMenuRef.current) {
            setTimeout(() => {
                const menuElement = dropdownMenuRef.current;
                const selectedElement = selectedItemRef.current;

                // Check if the menu is positioned at the top using data-popper-placement
                const isDropup = menuElement.getAttribute('data-popper-placement') === 'top-start';

                if (isDropup) {
                    // For dropup, position the selected item at the bottom:
                    // Calculate how far from the bottom the item should be
                    const itemHeight = selectedElement.offsetHeight;
                    const menuHeight = menuElement.clientHeight;
                    const itemOffset = selectedElement.offsetTop;

                    // Set scroll position to show the item at the bottom
                    menuElement.scrollTop = itemOffset - menuHeight + itemHeight;
                } else {
                    // For dropdown, position at the top as before
                    menuElement.scrollTop = selectedElement.offsetTop;
                }
            }, 0);
        }
    }, [isOpen]);

    return (
        <tr>
            <td style={{paddingBottom: '8px', paddingRight: '16px', whiteSpace: 'nowrap'}}>
                <span style={{marginLeft}}>{label}</span>
            </td>
            <td style={{paddingBottom: '8px', width: '100%'}}>
                <Form.Group className="d-flex align-items-stretch">
                    <div style={{width: '300px', height: '100%'}}>
                        <Dropdown show={isOpen} onToggle={setIsOpen}>
                            <Dropdown.Toggle
                                style={{
                                    width: '100%',
                                    height: '100%',
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "center",
                                    backgroundColor: isTextColor ? contrastColor : cssColor,
                                    color: isTextColor ? cssColor : contrastColor
                                }}
                                variant=""
                            >
                                {selectedName}
                            </Dropdown.Toggle>

                            <Dropdown.Menu
                                style={{width: '100%', padding: '0', maxHeight: '300px', overflowY: 'auto'}}
                                ref={dropdownMenuRef}
                            >
                                {['theme', ...Object.keys(colors)].map(name => {
                                    const isItemTextColor = name.includes('text') || label.includes('Text');
                                    const isSelected = name === selectedName;
                                    return (
                                        <Dropdown.Item
                                            key={name}
                                            onClick={() => {
                                                onChange?.(`var(--col-${name})`);
                                                setIsOpen(false);
                                            }}
                                            style={{
                                                backgroundColor: isItemTextColor ? 'transparent' : `var(--col-${name})`,
                                                color: isItemTextColor ? `var(--col-${name})` : `var(--contrast-col-${name})`
                                            }}
                                            ref={isSelected ? selectedItemRef : null}
                                        >
                                            {name}
                                        </Dropdown.Item>
                                    );
                                })}
                            </Dropdown.Menu>
                        </Dropdown>
                    </div>
                    {onRemoveOverride && (
                        <button
                            className="d-flex align-items-center px-1 col-red"
                            onClick={onRemoveOverride}
                            title="Remove night mode override"
                        >
                            <Fa icon={faTimes}/>
                        </button>
                    )}
                </Form.Group>
            </td>
        </tr>
    );
};

const formatLabel = (key) => {
    // Convert camelCase to Title Case with spaces
    return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase())
        .trim();
};

const UseCase = ({path, value, onChange, colors, isNightMode, baseValue, onRemoveOverride}) => {
    const level = Math.max(0, path.length - 1);

    if (typeof value === 'string') {
        const hasOverride = isNightMode && value !== baseValue;
        return (
            <ColorDropdown
                colors={colors}
                value={value}
                onChange={(newValue) => onChange(newValue, path)}
                label={formatLabel(path[path.length - 1])}
                marginLeft={level * 20}
                onRemoveOverride={hasOverride ? () => onRemoveOverride?.(path) : null}
            />
        );
    }

    if (Array.isArray(value)) {
        return null;
    }

    return (
        <>
            {typeof value === 'object' && !Array.isArray(value) && path.length > 0 && (
                <tr>
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
                    colors={colors}
                    isNightMode={isNightMode}
                    baseValue={baseValue?.[key]}
                    onRemoveOverride={onRemoveOverride}
                />
            ))}
        </>
    );
};

const UseCaseSection = ({useCases, colors, baseUseCases = null, isNightMode = false, onUpdate}) => {
    // For night mode, we need to merge the base use cases with overrides
    const mergedUseCases = isNightMode && baseUseCases ? mergeUseCases(baseUseCases, useCases) : useCases;

    const handleColorChange = (newValue, path) => {
        const result = {...useCases};
        let current = result;

        for (let i = 0; i < path.length - 1; i++) {
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
            <h5 className="mb-3">{isNightMode ? 'Night mode overrides' : 'Use Cases'}</h5>
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
                />
                </tbody>
            </table>
        </div>
    );
};

const ColorSection = ({title, colors}) => (
    <div className="mb-4">
        <h5 className="mb-3">{title}</h5>
        <div className="row">
            {Object.entries(colors).map(([name, color]) => (
                <ColorBox key={name} name={name} color={color}/>
            ))}
        </div>
    </div>
);

const ThemeEditorPage = () => {
    const {t} = useTranslation();
    const backgroundColors = theme.colors;
    const [currentUseCases, setCurrentUseCases] = useState(useCases);
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState(nightModeUseCases);
    useEffect(() => {
        setCurrentUseCases(useCases);
    }, [useCases]);
    useEffect(() => {
        setCurrentNightModeUseCases(nightModeUseCases);
    }, [nightModeUseCases]);

    const colorSlices = [
        {
            header: <div className="d-flex align-items-center gap-2">
                <Fa icon={faPalette}/> <span>Colors</span>
            </div>,
            body: <>
                <ColorSection title="Colors" colors={backgroundColors}/>
                <ColorSection title="Night Mode" colors={theme.nightColors}/>
                <hr/>
                <Row>
                    <Col xl={6} lg={12} md={12} sm={12} xs={12}>
                        <UseCaseSection
                            useCases={currentUseCases}
                            colors={backgroundColors}
                            onUpdate={setCurrentUseCases}
                        />
                    </Col>
                    <Col xl={6} lg={12} md={12} sm={12} xs={12}>
                        <UseCaseSection
                            useCases={currentNightModeUseCases}
                            colors={{...theme.nightColors, ...theme.colors}}
                            baseUseCases={currentUseCases}
                            isNightMode={true}
                            onUpdate={setCurrentNightModeUseCases}
                        />
                    </Col>
                </Row>
            </>
        }
    ];

    return (
        <>
            <ThemeStyleCss theme={theme} useCases={currentUseCases} nightModeUseCases={currentNightModeUseCases}/>
            <Sidebar page={t('Theme Editor')} items={[]}/>
            <div className="d-flex flex-column" id="content-wrapper">
                <Header page={t('Theme Editor')} hideUpdater/>
                <div id="content" style={{display: 'flex'}}>
                    <main className="container-fluid mt-4">
                        <Card className="shadow mb-4">
                            <CardHeader icon={faPalette} color="primary" label={t('Theme Editor')}/>
                            <Card.Body>
                                <SideNavTabs slices={colorSlices} open={true} alignment="left"/>
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