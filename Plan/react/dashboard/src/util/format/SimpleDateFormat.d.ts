declare class SimpleDateFormat {
    constructor(pattern: string);

    applyPattern(pattern: string): void;

    format(date: number, locale: string): string;
}

export {SimpleDateFormat};