import React from 'react';
import {useTranslation} from "react-i18next";
import {mergeUseCases} from "../../util/mutator.js";
import UseCase from "./UseCase.jsx";

const UseCaseSection = ({
                            useCases,
                            onHoverChange,
                            colors,
                            baseUseCases = null,
                            isNightMode = false,
                            updateUseCase,
                            removeOverride
                        }) => {
    const {t} = useTranslation();
    // For night mode, we need to merge the base use cases with overrides
    const mergedUseCases = isNightMode && baseUseCases ? mergeUseCases(baseUseCases, useCases) : useCases;

    return (
        <div className={"ps-4 pt-4 pb-4 mb-4" + (isNightMode ? ' night-mode-colors' : '')}>
            <h5 className="mb-3">{isNightMode ? t('html.label.themeEditor.nightModeOverrides') : t('html.label.themeEditor.useCases')}</h5>
            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <tbody>
                <UseCase
                    path={[]}
                    value={mergedUseCases}
                    onChange={updateUseCase}
                    colors={colors}
                    isNightMode={isNightMode}
                    baseValue={baseUseCases}
                    onRemoveOverride={isNightMode ? removeOverride : undefined}
                    onHoverChange={onHoverChange}
                />
                </tbody>
            </table>
        </div>
    );
};

export default UseCaseSection