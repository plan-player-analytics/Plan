export type ColorPropertyList = string[];

export type ColorPropertyMap = { [key: string]: string };

export type UseCaseMap = {
    [key: string]: UseCaseMap | ColorPropertyList | string,
} & {
    themeColorOptions?: ColorPropertyList
};

export type ThemeConfig = {
    defaultTheme: string;
    colors: ColorPropertyMap,
    nightColors: ColorPropertyMap,
    useCases: UseCaseMap,
    nightModeUseCases: UseCaseMap
}