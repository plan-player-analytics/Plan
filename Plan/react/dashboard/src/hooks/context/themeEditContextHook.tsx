import React, {createContext, PropsWithChildren, useContext, useMemo, useState} from "react";
import {useThemeStorage} from "./themeContextHook";
import {cssVariableToName, nameToCssVariable} from "../../util/colors.js";
import {flattenObject, recursiveFindAndReplaceValue} from "../../util/mutator.js";
import {Trans, useTranslation} from "react-i18next";
import {saveTheme} from "../../service/metadataService.js";
import {useAuth} from "../authenticationHook.js";
import {useAlertPopupContext} from "./alertPopupContext";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCheck, faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {getLocallyStoredThemes} from "../themeHook.jsx";
import {ColorPropertyMap, ThemeConfig, UseCaseMap} from "../../components/theme/model/ThemeConfig";
import {PlanResponse} from "../../service/PlanResponse";
import {ThemeContextValue} from "../../components/theme/model/ThemeContextValue";

type EditTypeMap = {
    color: ColorPropertyMap;
    nightColor: ColorPropertyMap;
    useCase: UseCaseMap;
    nightModeUseCase: UseCaseMap;
};

function isEditForType<K extends keyof EditTypeMap>(
    edit: EditOperation,
    type: K
): edit is Extract<EditOperation, { type: readonly K[] }> {
    return edit.type.includes(type);
}

type EditOperation<K extends readonly (keyof EditTypeMap)[] = readonly (keyof EditTypeMap)[]> = {
    type: K;
    name: string;
    operation: <T extends K[number]>(
        current: EditTypeMap[T],
        type: T
    ) => EditTypeMap[T];
};
type RedoOperation = EditOperation | EditOperation[];

function createEdit<K extends readonly (keyof EditTypeMap)[]>(
    name: string,
    types: K,
    operation: <T extends K[number]>(
        current: EditTypeMap[T],
        type: T
    ) => EditTypeMap[T]
): EditOperation {
    return {
        type: types,
        name,
        operation: operation as any
    };
}

type ThemeEditContextValue =
    {
        originalName: string;
        setName: (name: string) => void;
        editCount: number,
        redoCount: number,
        deleteColor: (name: string) => void;
        deleteNightColor: (name: string) => void;
        saveColor: (name: string, color: string, previous: string) => void;
        saveNightColor: (name: string, color: string, previous: string) => void;
        updateUseCase: (newValue: string | string[], path: string[]) => void;
        updateNightUseCase: (newValue: string | string[], path: string[]) => void;
        removeNightOverride: (path: string[]) => void;
        undo: () => void,
        redo: () => void,
        discardChanges: () => void,
        edits: EditOperation[],
        redos: RedoOperation[],
        issues: string[],
        savePossible: boolean,
        onlyLocal: boolean,
        discardPossible: boolean,
        save: () => void
    }
    & Omit<ThemeContextValue, 'usedColors' | 'usedUseCases' | 'cloneThemeLocally'
    | 'saveUploadedThemeLocally' | 'deleteThemeLocally' | 'reloadTheme'>;

const ThemeEditContext = createContext<ThemeEditContextValue | undefined>(undefined);

export const ThemeEditContextProvider = ({children}: PropsWithChildren) => {
    const {t} = useTranslation();
    const {authRequired, hasPermission} = useAuth();
    const [edits, setEdits] = useState<EditOperation[]>([]);
    const [redos, setRedos] = useState<RedoOperation[]>([]);
    const {addAlert} = useAlertPopupContext();
    const {
        loaded,
        name: originalName,
        color,
        currentColors,
        currentNightColors,
        currentUseCases,
        currentNightModeUseCases,
        saveUploadedThemeLocally,
        deleteThemeLocally,
        reloadTheme
    } = useThemeStorage();

    const [name, setName] = useState(originalName);
    const onNameChange = (value: string) => {
        setName(value.toLowerCase().replace(/[^a-z0-9-]/g, "-"));
    }

    const applyEdits = <K extends keyof EditTypeMap>(type: K, object: EditTypeMap[K]) => {
        console.debug('Applying edits', edits.length)
        let result = object;
        const applicable = edits.filter(edit => isEditForType(edit, type));
        for (let applicableEdit of applicable) {
            console.debug('Applying', applicableEdit.name, 'to', applicableEdit.type)
            result = applicableEdit.operation(result, type);
        }
        return result;
    }

    const addEdit = (edit: EditOperation) => {
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
        if ('length' in toRedo) {
            toRedo.forEach(edit => {
                addEdit(edit)
            })
        } else {
            addEdit(toRedo);
        }
        setRedos(redos.slice(0, -1));
    }

    const discardChanges = () => {
        if (edits.length) {
            const undone = [...edits];
            setEdits([]);
            setRedos(prev => [...prev, undone]);
        } else {
            setRedos([]);
        }
        setName(originalName);
    }

    const updateUseCaseColorName = (current: UseCaseMap, oldName: string, newName: string) => {
        const oldVariable = nameToCssVariable(oldName);
        const newVariable = nameToCssVariable(newName);
        return recursiveFindAndReplaceValue(current, oldVariable, newVariable);
    }

    const handleColorSave = (current: ColorPropertyMap) => (name: string, color: string, previous: string) => {
        const newObj: any = {};
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
        return newObj as ColorPropertyMap;
    }
    const saveColor = (name: string, color: string, previous: string) => {
        const renamed = previous && name !== previous;
        if (renamed) {
            addEdit(createEdit(
                t('html.label.themeEditor.changes.renameColor', {previous, name, color}),
                ["color", "useCase", "nightModeUseCase"],
                (current, type) => {
                    if (type === 'color') {
                        return handleColorSave(current as ColorPropertyMap)(name, color, previous);
                    } else {
                        return updateUseCaseColorName(current as UseCaseMap, previous, name);
                    }
                }
            ));
        } else {
            addEdit(createEdit(
                t(previous ? 'html.label.themeEditor.changes.setColor' : 'html.label.themeEditor.changes.addColor', {
                    name,
                    color
                }),
                ['color'],
                (current) => handleColorSave(current)(name, color, previous)
            ));
        }
    }
    const saveNightColor = (name: string, color: string, previous: string) => {
        const renamed = name !== previous;
        if (renamed) {
            addEdit(createEdit(
                t('html.label.themeEditor.changes.renameColor', {previous, name, color}),
                ['nightColor', 'nightModeUseCase'],
                (current, type) => {
                    if (type === 'nightColor') {
                        return handleColorSave(current as ColorPropertyMap)(name, color, previous);
                    } else {
                        return updateUseCaseColorName(current as UseCaseMap, previous, name);
                    }
                }
            ));
        } else {
            addEdit(createEdit(
                t(previous ? 'html.label.themeEditor.changes.setColor' : 'html.label.themeEditor.changes.addColor', {
                    name,
                    color
                }),
                ['nightColor'],
                (current: ColorPropertyMap) => handleColorSave(current)(name, color, previous)
            ))
        }
    }

    const handleDelete = (current: ColorPropertyMap) => (name: string) => {
        const copy = {...current};
        delete copy[name];
        return copy;
    }
    const deleteColor = (name: string) => addEdit(createEdit(
        t('html.label.themeEditor.changes.deleteColor', {name}),
        ['color'],
        (current: ColorPropertyMap) => handleDelete(current)(name)
    ));
    const deleteNightColor = (name: string) => addEdit(createEdit(
        t('html.label.themeEditor.changes.deleteColor', {name}),
        ['nightColor'],
        (current: ColorPropertyMap) => handleDelete(current)(name)
    ));

    const handleColorChange: any = (currentObject: UseCaseMap, newValue: string | string[], path: string[]) => {
        if (path.length === 0) return newValue;
        const [key, ...rest] = path;
        return {
            ...currentObject,
            [key]: rest.length === 0
                ? newValue
                : handleColorChange(currentObject?.[key] as UseCaseMap || {}, newValue, rest)
        };
    };

    const handleRemoveOverride = (current: UseCaseMap, path: string[]) => {
        // Create a new object without the override, but maintain structure
        const removeOverride = (obj: UseCaseMap, pathArr: string[]) => {
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
            const nested = removeOverride(obj[currentKey] as UseCaseMap || {}, rest);
            if (nested === null) {
                delete result[currentKey];
                return Object.keys(result).length === 0 ? null : result;
            }
            result[currentKey] = nested;
            return result;
        };

        // Get the new state without the override
        return removeOverride(current, path) ?? {};
    };

    const updateUseCase = (newValue: string | string[], path: string[]) => addEdit(Array.isArray(newValue)
        ? createEdit(
            t('html.label.themeEditor.changes.changeUseCaseArray', {path: path.join('.')}),
            ['useCase'],
            (current: UseCaseMap) => handleColorChange(current, newValue, path)
        ) : createEdit(
            t('html.label.themeEditor.changes.changeUseCase', {
                path: path.join('.'),
                name: cssVariableToName(newValue)
            }),
            ['useCase'],
            (current: UseCaseMap) => handleColorChange(current, newValue, path)
        ));

    const updateNightUseCase = (newValue: string | string[], path: string[]) => addEdit(Array.isArray(newValue)
        ? createEdit(
            t('html.label.themeEditor.changes.changeNightModeArray', {path: path.join('.')}),
            ['nightModeUseCase'],
            (current: UseCaseMap) => handleColorChange(current, newValue, path)
        )
        : createEdit(
            t('html.label.themeEditor.changes.changeNightMode', {
                path: path.join('.'),
                name: cssVariableToName(newValue)
            }),
            ['nightModeUseCase'],
            (current: UseCaseMap) => handleColorChange(current, newValue, path)
        ));
    const removeNightOverride = (path: string[]) => addEdit(createEdit(
        t('html.label.themeEditor.changes.removeNightMode', {path: path.join('.')}),
        ['nightModeUseCase'],
        (current: UseCaseMap) => handleRemoveOverride(current, path)
    ));

    const sharedState = useMemo(() => {
        const editedColors = applyEdits('color', currentColors);
        const editedNightColors = applyEdits('nightColor', currentNightColors);
        const editedUseCases = applyEdits('useCase', currentUseCases);
        const editedNightModeUseCases = applyEdits('nightModeUseCase', currentNightModeUseCases);

        const issues: string[] = [];

        const allColorsExist = () => {
            const referenceColors = Object.keys(editedUseCases?.referenceColors || {})
            const colorMissing = (name: string) => {
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

            const themeToSave: ThemeConfig = {
                name: name,
                defaultTheme: editedUseCases.themeColorOptions?.[0] || 'plan',
                colors: editedColors,
                nightColors: editedNightColors,
                useCases: editedUseCases,
                nightModeUseCases: editedNightModeUseCases
            };

            saveUploadedThemeLocally(name, themeToSave, originalName);
            // Save remotely
            if (authRequired && hasPermission('manage.themes')) {
                const response: PlanResponse<{
                    success: boolean
                }> | undefined = await saveTheme(name, themeToSave, originalName);
                if (response?.error) {
                    addAlert({
                        timeout: 15000,
                        color: "warning",
                        content: <>
                            <Fa icon={faExclamationTriangle}/>
                            {" "}
                            <Trans i18nKey={"html.label.managePage.alert.saveFail"}
                                   values={{error: response.error.message}}/>
                        </>
                    });
                } else {
                    deleteThemeLocally(name);
                    addAlert({
                        timeout: 5000,
                        color: "success",
                        content: <><Fa icon={faCheck}/>{" "}{t('html.label.managePage.alert.saveSuccess')}</>
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
            color,
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
    const context = useContext(ThemeEditContext);
    if (!context) throw new Error('useThemeEditContext must be used within a ThemeEditContextProvider');
    return context;
}