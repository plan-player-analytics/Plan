import {ElementOrder, ExtensionData} from "./ExtensionData";

export type ExtensionLayoutWidths = {
    cardWidth: number;
} & Partial<Record<ElementOrder, number>>;

type LayoutItem = {
    element: ElementOrder;
    min: number;
    max: number;
};

const expand = (row: LayoutItem[], cardWidth: number) => {
    const result = row.map(item => ({
        element: item.element,
        min: item.min / cardWidth * 12,
        max: item.max / cardWidth * 12
    }));

    let remaining =
        12 - result.reduce((sum, item) => sum + item.min, 0);

    while (remaining > 0) {
        let changed = false;

        for (const item of result) {
            if (item.min < item.max) {
                item.min++;
                remaining--;
                changed = true;

                if (remaining === 0) {
                    break;
                }
            }
        }

        if (!changed) {
            break;
        }
    }

    return result.map(item => ({
        ...item
    }));
}

const buildRows = (items: LayoutItem[]): LayoutItem[][] => {
    const rows: LayoutItem[][] = [];
    let currentRow: LayoutItem[] = [];
    let currentWidth = 0;

    for (const item of items) {
        if (currentWidth + item.min > 12) {
            rows.push(currentRow);
            currentRow = [];
            currentWidth = 0;
        }

        currentRow.push(item);
        currentWidth += item.min;
    }

    if (currentRow.length) {
        rows.push(currentRow);
    }

    return rows;
};

export const computeLayoutWidths = (extension: ExtensionData): ExtensionLayoutWidths => {
    const tabs = extension.tabs || [];

    const hasValues = tabs.some(tab => tab.values?.length);
    const hasGraph = tabs.some(tab => tab.graphNames?.length);
    const hasTable = tabs.some(tab => tab.tableData?.length);
    const hasGroups = false;
    const hasStatistics = false;
    const hasFlags = false;
    const includedItems: Set<ElementOrder> = new Set([
        hasValues ? 'VALUES' as const : undefined,
        hasGraph ? 'GRAPH' as const : undefined,
        hasTable ? 'TABLE' as const : undefined,
        hasGroups ? 'GROUPS' as const : undefined,
        hasStatistics ? 'STATISTICS' as const : undefined,
        hasFlags ? 'FLAGS' as const : undefined
    ].filter(v => v !== undefined));

    const hasWideTable = tabs.some(tab => tab.tableData?.some(table => table.wide));
    const hasFullWidthTable = extension.wide;

    const layout: LayoutItem[] = [
        {
            element: 'VALUES' as const,
            min: 4,
            max: 6,
        },
        {
            element: 'GRAPH' as const,
            min: 8,
            max: 12,
        },
        {
            element: 'TABLE' as const,
            min: hasFullWidthTable ? 12 : (hasWideTable ? 8 : 4),
            max: hasFullWidthTable || hasWideTable ? 12 : 6,
        },
        {
            element: 'GROUPS' as const,
            min: 4,
            max: 6,
        },
        {
            element: 'STATISTICS' as const,
            min: 4,
            max: 6,
        },
        {
            element: 'FLAGS' as const,
            min: 4,
            max: 6,
        }
    ].filter(item => includedItems.has(item.element));
    const combinedMinimumWidth = layout.reduce((partialSum, a) => partialSum + a.min, 0);

    const hasFullWidthElement = layout.some(item => item.min === 12);
    const horizontalSplit = !hasFullWidthElement
        && (hasValues && combinedMinimumWidth == 8 && layout.length == 2) // Values + table
        || (combinedMinimumWidth == 16 && layout.length == 2) // Two 2 wide elements
        || (combinedMinimumWidth == 16 && (layout.length == 3 || layout.length == 4) && !hasValues); // 3-4 elements without values

    if (horizontalSplit) {
        const halved = combinedMinimumWidth / 2; // 4 or 8
        return {
            cardWidth: halved,
            ...Object.fromEntries(expand(layout, halved).map(item => [item.element, item.min]))
        }
    }

    if (combinedMinimumWidth > 12) {
        return {
            cardWidth: 12,
            ...Object.fromEntries(expand(layout, 12).map(item => [item.element, item.min]))
        }
    } else {
        return {
            cardWidth: combinedMinimumWidth,
            ...Object.fromEntries(expand(layout, combinedMinimumWidth).map(item => [item.element, item.min]))
        }
    }
};