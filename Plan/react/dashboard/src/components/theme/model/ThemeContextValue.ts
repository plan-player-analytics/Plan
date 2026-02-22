import {PlanError} from "../../../views/ErrorView";
import {ColorPropertyMap, ThemeConfig, UseCaseMap} from "./ThemeConfig";

export type ThemeContextValue = {
    loaded: boolean;
    error: PlanError;
    color?: string;
    currentColors: ColorPropertyMap;
    currentNightColors: ColorPropertyMap;
    currentUseCases: UseCaseMap;
    currentNightModeUseCases: UseCaseMap;
    usedColors: ColorPropertyMap;
    usedUseCases: UseCaseMap;
    cloneThemeLocally: (themeToClone: string, name: string) => boolean,
    saveUploadedThemeLocally: (name: string, themeJson: ThemeConfig, originalName?: string) => boolean,
    deleteThemeLocally: (name?: string) => void,
    reloadTheme: () => void,
}