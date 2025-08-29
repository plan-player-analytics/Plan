import React, {createContext, useContext, useMemo, useState} from "react";
import {useThemeStorage} from "./themeContextHook.jsx";
import {cssVariableToName, nameToCssVariable} from "../../util/colors.js";
import {flattenObject, recursiveFindAndReplaceValue} from "../../util/mutator.js";
import {Trans, useTranslation} from "react-i18next";
import {saveTheme} from "../../service/metadataService.js";
import {useAuth} from "../authenticationHook.jsx";
import {useAlertPopupContext} from "./alertPopupContext.jsx";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCheck, faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {getLocallyStoredThemes} from "../themeHook.jsx";

const ThemeEditContext = createContext({});

export const ThemeEditContextProvider = ({children}) => {
    const {t} = useTranslation();
    const {authRequired, hasPermission} = useAuth();
    const [edits, setEdits] = useState([]);
    const [redos, setRedos] = useState([]);
    const {addAlert} = useAlertPopupContext();
    const {
        loaded,
        name: originalName,
        currentColors,
        currentNightColors,
        currentUseCases,
        currentNightModeUseCases,
        saveUploadedThemeLocally,
        deleteThemeLocally,
        reloadTheme
    } = useThemeStorage();

    const [name, setName] = useState(originalName);
    const onNameChange = value => {
        setName(value.toLowerCase().replace(/[^a-z0-9-]/g, "-"));
    }

    const applyEdits = (type, object) => {
        console.debug('Applying edits', edits.length)
        let result = object;
        const applicable = edits.filter(edit => edit.type.includes(type));
        for (let applicableEdit of applicable) {
            console.debug('Applying', applicableEdit.name, 'to', applicableEdit.type)
            result = applicableEdit.operation(result, type);
        }
        return result;
    }

    const addEdit = edit => {
        setEdits(prev => [...prev, edit]);
        setRedos([]);
    }

    const undo = () => {
        if (edits.length) {
            const undone = edits[edits.length - 1];
            setEdits(edits.slice(0, -1));
            setRedos(prev => [...prev, undone]);
        }
    }

    const redo = () => {
        const toRedo = redos[redos.length - 1];
        if (toRedo.length) {
            toRedo.forEach(edit => {
                addEdit(edit)
            })
        } else {
            addEdit(toRedo);
        }
        setRedos(redos.slice(0, -1));
    }

    const discardChanges = () => {
        if (!edits.length) {
            setRedos([]);
        } else {
            const undone = [...edits];
            setEdits([]);
            setRedos(prev => [...prev, undone]);
        }
        setName(originalName);
    }

    const updateUseCaseColorName = (current, oldName, newName) => {
        const oldVariable = nameToCssVariable(oldName);
        const newVariable = nameToCssVariable(newName);
        return recursiveFindAndReplaceValue(current, oldVariable, newVariable);
    }

    const handleColorSave = (current) => (name, color, previous) => {
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
        return newObj;
    }
    const saveColor = (name, color, previous) => {
        const renamed = previous && name !== previous;
        if (renamed) {
            addEdit({
                name: t('html.label.themeEditor.changes.renameColor', {previous, name, color}),
                type: 'color,useCase,nightModeUseCase', operation: (current, type) => {
                    if (type === 'color') {
                        return handleColorSave(current)(name, color, previous);
                    } else {
                        return updateUseCaseColorName(current, previous, name);
                    }
                }
            })
        } else {
            addEdit({
                name: t(previous ? 'html.label.themeEditor.changes.setColor' : 'html.label.themeEditor.changes.addColor', {
                    name,
                    color
                }),
                type: 'color', operation: (current) => handleColorSave(current)(name, color, previous)
            })
        }
    }
    const saveNightColor = (name, color, previous) => {
        const renamed = name !== previous;
        if (renamed) {
            addEdit({
                name: t('html.label.themeEditor.changes.renameColor', {previous, name, color}),
                type: 'nightColor,nightModeUseCase', operation: (current, type) => {
                    if (type === 'nightColor') {
                        return handleColorSave(current)(name, color, previous);
                    } else {
                        return updateUseCaseColorName(current, previous, name);
                    }
                }
            })
        } else {
            addEdit({
                name: t(previous ? 'html.label.themeEditor.changes.setColor' : 'html.label.themeEditor.changes.addColor', {
                    name,
                    color
                }),
                type: 'nightColor', operation: (current) => handleColorSave(current)(name, color, previous)
            })
        }
    }

    const handleDelete = (current) => (name) => {
        const copy = {...current};
        delete copy[name];
        return copy;
    }
    const deleteColor = name => addEdit({
        name: t('html.label.themeEditor.changes.deleteColor', {name}),
        type: 'color',
        operation: current => handleDelete(current)(name)
    })
    const deleteNightColor = name => addEdit({
        name: t('html.label.themeEditor.changes.deleteColor', {name}),
        type: 'nightColor',
        operation: current => handleDelete(current)(name)
    })

    const handleColorChange = (currentObject, newValue, path) => {
        if (path.length === 0) return newValue;
        const [key, ...rest] = path;
        return {
            ...currentObject,
            [key]: rest.length === 0
                ? newValue
                : handleColorChange(currentObject?.[key] || {}, newValue, rest)
        };
    };

    const handleRemoveOverride = (current, path) => {
        // Create a new object without the override, but maintain structure
        const removeOverride = (obj, pathArr) => {
            if (pathArr.length === 0) return obj;

            const [currentKey, ...rest] = pathArr;
            const result = {...obj};

            if (rest.length === 0) {
                // We've reached the target property, remove it
                delete result[currentKey];
                // If the parent object becomes empty, return null to signal removal
                return Object.keys(result).length === 0 ? null : result;
            }

            // Continue traversing
            const nested = removeOverride(obj[currentKey] || {}, rest);
            if (nested === null) {
                delete result[currentKey];
                return Object.keys(result).length === 0 ? null : result;
            }
            result[currentKey] = nested;
            return result;
        };

        // Get the new state without the override
        return removeOverride(current, path) || {};
    };

    const updateUseCase = (newValue, path) => Array.isArray(newValue)
        ? addEdit({
            name: t('html.label.themeEditor.changes.changeUseCaseArray', {path: path.join('.')}),
            type: 'useCase',
            operation: current => handleColorChange(current, newValue, path)
        })
        : addEdit({
            name: t('html.label.themeEditor.changes.changeUseCase', {
                path: path.join('.'),
                name: cssVariableToName(newValue)
            }),
            type: 'useCase',
            operation: current => handleColorChange(current, newValue, path)
        });

    const updateNightUseCase = (newValue, path) => Array.isArray(newValue)
        ? addEdit({
            name: t('html.label.themeEditor.changes.changeNightModeArray', {path: path.join('.')}),
            type: 'nightModeUseCase',
            operation: current => handleColorChange(current, newValue, path)
        })
        : addEdit({
            name: t('html.label.themeEditor.changes.changeNightMode', {
                path: path.join('.'),
                name: cssVariableToName(newValue)
            }),
            type: 'nightModeUseCase',
            operation: current => handleColorChange(current, newValue, path)
        });
    const removeNightOverride = (path) => addEdit({
        name: t('html.label.themeEditor.changes.removeNightMode', {path: path.join('.')}),
        type: 'nightModeUseCase',
        operation: current => handleRemoveOverride(current, path)
    });

    const sharedState = useMemo(() => {
        const editedColors = applyEdits('color', currentColors);
        const editedNightColors = applyEdits('nightColor', currentNightColors);
        const editedUseCases = applyEdits('useCase', currentUseCases);
        const editedNightModeUseCases = applyEdits('nightModeUseCase', currentNightModeUseCases);

        const issues = [];

        const allColorsExist = () => {
            const referenceColors = Object.keys(editedUseCases?.referenceColors || {})
            const colorMissing = name => {
                const exists = editedColors[name] || editedNightColors[name] || referenceColors.includes(name);
                if (!exists) console.warn(name, "doesn't exist on color maps")
                return !exists;
            }
            const missingUseCase = Object.entries(flattenObject(editedUseCases))
                .filter(e => colorMissing(cssVariableToName(e[1])));
            const missingNightModeUseCase = Object.entries(flattenObject(editedNightModeUseCases))
                .filter(e => colorMissing(cssVariableToName(e[1])));

            missingUseCase.forEach(e => issues.push(
                t('html.label.themeEditor.issues.missingUseCase', {name: e[0], colorName: cssVariableToName(e[1])})));
            missingNightModeUseCase.forEach(e => issues.push(
                t('html.label.themeEditor.issues.missingNightCase', {name: e[0], colorName: cssVariableToName(e[1])})));

            return !missingUseCase.length && !missingNightModeUseCase.length
        }

        const onlyLocal = getLocallyStoredThemes().includes(originalName);
        const somethingToSave = edits.length > 0 || name !== originalName;
        const savePossible = (somethingToSave || (onlyLocal && authRequired && hasPermission('manage.themes'))) && allColorsExist();

        const save = async () => {
            if (!savePossible) {
                return
            }

            const themeToSave = {
                name: name,
                colors: editedColors,
                nightColors: editedNightColors,
                useCases: editedUseCases,
                nightModeUseCases: editedNightModeUseCases
            };

            saveUploadedThemeLocally(name, themeToSave, originalName);
            // Save remotely
            if (authRequired && hasPermission('manage.themes')) {
                const {error} = await saveTheme(name, themeToSave, originalName);
                if (!error) {
                    deleteThemeLocally(name);
                    addAlert({
                        timeout: 5000,
                        color: "success",
                        content: <><Fa icon={faCheck}/>{" "}{t('html.label.managePage.alert.saveSuccess')}</>
                    });
                } else {
                    addAlert({
                        timeout: 15000,
                        color: "warning",
                        content: <>
                            <Fa icon={faExclamationTriangle}/>
                            {" "}
                            <Trans i18nKey={"html.label.managePage.alert.saveFail"} values={{error: error?.message}}/>
                        </>
                    });
                }
            }
            setEdits([]);
            setRedos([]);
            reloadTheme();
        }

        return {
            loaded,
            name,
            originalName,
            setName: onNameChange,
            currentColors: editedColors,
            currentNightColors: editedNightColors,
            currentUseCases: editedUseCases,
            currentNightModeUseCases: editedNightModeUseCases,
            editCount: edits.length,
            redoCount: redos.length,
            deleteColor,
            deleteNightColor,
            saveColor,
            saveNightColor,
            updateUseCase,
            updateNightUseCase,
            removeNightOverride,
            undo,
            redo,
            discardChanges,
            edits,
            redos,
            issues,
            savePossible,
            onlyLocal,
            discardPossible: somethingToSave,
            save
        }
    }, [edits, name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases]);
    return (<ThemeEditContext.Provider value={sharedState}>
            {children}
        </ThemeEditContext.Provider>
    )
}

export const useThemeEditContext = () => {
    return useContext(ThemeEditContext);
}