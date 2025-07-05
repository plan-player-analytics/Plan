import React from 'react';
import ColorDropdown from "./ColorDropdown.jsx";

const formatLabel = (key) => {
    // Convert camelCase to Title Case with spaces
    return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase())
        .trim();
};


const UseCase = ({path, value, onChange, onHoverChange, colors, isNightMode, baseValue, onRemoveOverride}) => {
    const level = Math.max(0, path.length - 1);
    const id = path.join('.');

    if (typeof value === 'string') {
        const hasOverride = isNightMode && value !== baseValue;
        return (
            <ColorDropdown
                id={id} key={id}
                colors={colors}
                value={value}
                onChange={(newValue) => onChange(newValue, path)}
                label={formatLabel(path[path.length - 1])}
                marginLeft={level * 20}
                onRemoveOverride={hasOverride ? () => onRemoveOverride?.(path) : null}
                onHoverChange={(a, b) => onHoverChange(a, b, isNightMode)}
            />
        );
    }

    if (Array.isArray(value)) {
        return null;
    }

    return (
        <>
            {typeof value === 'object' && !Array.isArray(value) && path.length > 0 && (
                <tr>
                    <td colSpan={2}>
                        {(level === 0 && path[0] !== 'referenceColors') && <hr/>}
                        <h6 className={'mt-2 mb-3'} style={{marginLeft: level * 20, fontWeight: "bold"}}
                            onMouseOver={() => onHoverChange(id, 'enter', isNightMode)}
                            onMouseOut={() => onHoverChange(id, 'exit', isNightMode)}>
                            {formatLabel(path[path.length - 1])}
                        </h6>
                    </td>
                </tr>
            )}
            {Object.entries(value).map(([key, val]) => (
                <UseCase
                    key={key}
                    path={[...path, key]}
                    value={val}
                    onChange={onChange}
                    onHoverChange={onHoverChange}
                    colors={colors}
                    isNightMode={isNightMode}
                    baseValue={baseValue?.[key]}
                    onRemoveOverride={onRemoveOverride}
                />
            ))}
        </>
    );
};

export default UseCase