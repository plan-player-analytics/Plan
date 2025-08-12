import React, {createContext, useCallback, useContext, useMemo, useState} from "react";

const MinHeightContext = createContext();

export const MinHeightProvider = ({children}) => {
    const [minHeightRules, setMinHeightRules] = useState({});

    const unregisterMinHeight = (selector, nightMode, count) => {
        let removed = false
        setMinHeightRules(rules => {
            const existingRule = rules[selector];
            const existingNightMode = existingRule?.nightMode;
            const existingCount = existingRule?.count || 0;
            removed = existingNightMode === nightMode && count < existingCount;
            if (removed) {
                // Remove the current rule when count is less than existing count
                const {[selector]: removed, ...remainingRules} = rules;
                return remainingRules;
            }
            return rules; // Return the rules unchanged if condition not met
        });
        return removed;
    }

    const registerMinHeight = useCallback((selector, minHeight, nightMode, count) => {
        setMinHeightRules(rules => {
            const existingRule = rules[selector];
            const existingHeight = existingRule?.minHeight;

            // Change the height if:
            // - It's taller than existing one
            // - or if the height of the currently registered one changed
            const shouldUpdate = !existingHeight || minHeight > existingHeight;

            if (!shouldUpdate) return rules;
            return {...rules, [selector]: {minHeight, nightMode, count}};
        });
    }, []);

    const value = useMemo(() => {
        return {minHeightRules, registerMinHeight, unregisterMinHeight}
    }, [minHeightRules]);
    return (
        <MinHeightContext.Provider value={value}>
            <style>
                {Object.entries(minHeightRules)
                    .map(([selector, opt]) => `.${selector}{min-height:${opt.minHeight}px;}`)
                    .join('\n')}
            </style>
            {children}
        </MinHeightContext.Provider>
    );
};

export const useMinHeightContext = () => useContext(MinHeightContext); 