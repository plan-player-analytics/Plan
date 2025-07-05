import React from 'react';
import {useTranslation} from "react-i18next";
import {mergeUseCases} from "../../util/mutator.js";
import UseCase from "./UseCase.jsx";

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

export default UseCaseSection