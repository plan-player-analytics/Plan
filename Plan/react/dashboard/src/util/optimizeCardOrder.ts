type Card = {
    widths: {
        cardWidth: number;
    };
};

type Row = Card[];

function isBetter(
    a: [number, number, number],
    b: [number, number, number] | null
): boolean {
    if (!b) return true;

    for (let i = 0; i < a.length; i++) {
        if (a[i] !== b[i]) {
            return a[i] < b[i];
        }
    }

    return false;
}

export function optimizeCardOrder<T extends Card>(cards: T[], rowWidth = 12): T[] {
    if (!cards?.length) {
        return [];
    }

    // Validate widths.
    for (const card of cards) {
        if (
            !Number.isInteger(card.widths.cardWidth) ||
            card.widths.cardWidth < 1 ||
            card.widths.cardWidth > rowWidth
        ) {
            throw new Error(`Invalid card width: ${card.widths.cardWidth}`);
        }
    }

    const n = cards.length;

    // Original position of each card.
    const originalIndex = new Map<T, number>();
    cards.forEach((card, index) => originalIndex.set(card, index));

    let bestRows: T[][] | null = null;
    let bestScore: [number, number, number] | null = null;

    /*
     * Score:
     *
     * 1. Number of rows
     * 2. Sum of squared gaps
     * 3. Number of inversions
     *
     * The first criterion guarantees minimum rows.
     * The second prefers evenly-packed rows.
     * The third preserves the original ordering.
     */
    function getScore(rows: T[][]): [number, number, number] {
        const gaps = rows.map(
            row =>
                rowWidth -
                row.reduce((sum, card) => sum + card.widths.cardWidth, 0)
        );

        // Squared gaps makes [0, 4] preferable to [2, 2].
        // If you don't care about this criterion, remove it.
        const gapScore = gaps.reduce((sum, gap) => sum + gap * gap, 0);

        const flattened = rows.flat();

        let inversions = 0;

        for (let i = 0; i < flattened.length; i++) {
            for (let j = i + 1; j < flattened.length; j++) {
                const a = originalIndex.get(flattened[i])!;
                const b = originalIndex.get(flattened[j])!;

                if (a > b) {
                    inversions++;
                }
            }
        }

        return [rows.length, gapScore, inversions];
    }

    /*
     * Search recursively.
     *
     * We process cards in their ORIGINAL order. This is useful because
     * it naturally biases the generated solutions toward preserving
     * the original order.
     */
    function search(index: number, rows: T[][]): void {
        if (index === n) {
            const score = getScore(rows);

            if (isBetter(score, bestScore)) {
                bestScore = score;
                bestRows = rows.map(row => [...row]);
            }

            return;
        }

        const card = cards[index];
        const width = card.widths.cardWidth;

        // Don't put the card into multiple rows having the same current
        // remaining space. Those choices are equivalent.
        const triedRemaining = new Set<number>();

        // Try existing rows first.
        for (const row of rows) {
            const used = row.reduce(
                (sum, card) => sum + card.widths.cardWidth,
                0
            );

            const remaining = rowWidth - used;

            if (remaining < width || triedRemaining.has(remaining)) {
                continue;
            }

            triedRemaining.add(remaining);

            row.push(card);
            search(index + 1, rows);
            row.pop();
        }

        // Try starting a new row.
        //
        // We don't need to do this if we already have a solution with
        // fewer rows than the number we'd create.
        if (!bestScore || rows.length + 1 <= bestScore[0]) {
            rows.push([card]);
            search(index + 1, rows);
            rows.pop();
        }
    }

    search(0, []);

    return bestRows!.flat();
}