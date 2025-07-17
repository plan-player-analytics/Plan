import {createContext, useContext, useMemo, useState} from "react";
import {useThemeStorage} from "./themeContextHook.jsx";
import {nameToCssVariable} from "../../util/colors.js";
import {recursiveFindAndReplaceValue} from "../../util/mutator.js";

const ThemeEditContext = createContext({});

export const ThemeEditContextProvider = ({children}) => {
    const [edits, setEdits] = useState([]);
    const [redos, setRedos] = useState([]);
    const {name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases} = useThemeStorage();

    const applyEdits = (type, object) => {
        console.group('Applying edits', edits.length)
        let result = object;
        const applicable = edits.filter(edit => edit.type.includes(type));
        for (let applicableEdit of applicable) {
            console.log('Applying', applicableEdit.name, 'to', applicableEdit.type)
            result = applicableEdit.operation(result, type);
        }
        console.groupEnd();
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
        addEdit(redos[redos.length - 1]);
        setRedos(redos.slice(0, -1));
    }

    const updateUseCaseColorName = (current, oldName, newName) => {
        const oldVariable = nameToCssVariable(oldName);
        const newVariable = nameToCssVariable(newName);
        return recursiveFindAndReplaceValue(current, oldVariable, newVariable);
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
        return newObj;
    }
    const saveColor = (name, color, previous) => {
        const renamed = name !== previous;
        if (renamed) {
            addEdit({
                name: 'rename-edit-color-' + previous + '-to-' + name,
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
                name: 'edit-color-' + name,
                type: 'color', operation: (current) => handleColorSave(current)(name, color, previous)
            })
        }
    }
    const saveNightColor = (name, color, previous) => {
        const renamed = name !== previous;
        if (renamed) {
            addEdit({
                name: 'rename-edit-color-' + previous + '-to-' + name,
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
                name: 'edit-color-' + name,
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
        name: 'delete-color-' + name,
        type: 'color',
        operation: current => handleDelete(current)(name)
    })
    const deleteNightColor = name => addEdit({
        name: 'delete-color-' + name,
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
    const updateUseCase = (newValue, path) => addEdit({
        name: 'update-use-case(' + path + '): ' + newValue,
        type: 'useCase',
        operation: current => handleColorChange(current, newValue, path)
    });
    const updateNightUseCase = (newValue, path) => addEdit({
        name: 'update-use-case(' + path + '): ' + newValue,
        type: 'nightModeUseCase',
        operation: current => handleColorChange(current, newValue, path)
    });
    const removeNightOverride = (path) => addEdit({
        name: 'delete-use-case(' + path + ')',
        type: 'nightModeUseCase',
        operation: current => handleRemoveOverride(current, path)
    });

    const sharedState = useMemo(() => {
        return {
            name,
            currentColors: applyEdits('color', currentColors),
            currentNightColors: applyEdits('nightColor', currentNightColors),
            currentUseCases: applyEdits('useCase', currentUseCases),
            currentNightModeUseCases: applyEdits('nightModeUseCase', currentNightModeUseCases),
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
            redo
        }
    }, [edits]);
    return (<ThemeEditContext.Provider value={sharedState}>
            {children}
        </ThemeEditContext.Provider>
    )
}

export const useThemeEditContext = () => {
    return useContext(ThemeEditContext);
}