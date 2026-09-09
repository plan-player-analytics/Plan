export type ColorPropertyList = string[];

export type ColorPropertyMap = { [key: string]: string };

export type UseCase = UseCaseMap | ColorPropertyList | string;

export type UseCaseMap = {
    [key: string]: UseCase;
} & {
    themeColorOptions?: ColorPropertyList
};

export type ThemeConfig = {
    name?: string;
    defaultTheme: string;
    colors: ColorPropertyMap,
    nightColors: ColorPropertyMap,
    useCases: UseCaseMap,
    nightModeUseCases: UseCaseMap
}