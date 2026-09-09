import {PlanError} from "../../../views/ErrorView";
import {ColorPropertyMap, ThemeConfig, UseCaseMap} from "./ThemeConfig";

export type ThemeContextValue = {
    loaded: boolean;
    error?: PlanError;
    name: string;
    color?: string;
    currentColors: ColorPropertyMap;
    currentNightColors: ColorPropertyMap;
    currentUseCases: UseCaseMap;
    currentNightModeUseCases: UseCaseMap;
    usedColors: ColorPropertyMap;
    usedUseCases: UseCaseMap;
    cloneThemeLocally: (themeToClone: string, name: string) => Promise<boolean>,
    saveUploadedThemeLocally: (name: string, themeJson: ThemeConfig, originalName?: string) => void,
    deleteThemeLocally: (name?: string) => void,
    reloadTheme: () => void,
}