import React, {useEffect, useRef} from 'react';
import ColorDropdown from "./ColorDropdown.jsx";
import ColorMultiSelect from "./ColorMultiSelect.jsx";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faTimes} from "@fortawesome/free-solid-svg-icons";
import {useMinHeightContext} from "../../hooks/context/minHeightContextHook";

export const formatLabel = (key) => {
    return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase())
        .trim();
};

const SectionLabel = ({level, label, id, isNightMode, onHoverChange}) => (
    <tr>
        <td colSpan={2}>
            {(level === 0 && label !== 'Reference Colors') && <hr/>}
            <h6 className={'mt-2 mb-3'} style={{marginLeft: level * 20, fontWeight: "bold"}}
                onMouseOver={() => onHoverChange(id, 'enter', isNightMode)}
                onFocus={() => onHoverChange(id, 'enter', isNightMode)}
                onMouseOut={() => onHoverChange(id, 'exit', isNightMode)}
                onBlur={() => onHoverChange(id, 'exit', isNightMode)}>
                {label}
            </h6>
        </td>
    </tr>
);

const UseCaseLabel = ({level, label}) => (
    <td style={{paddingBottom: '8px', paddingRight: '16px', whiteSpace: 'nowrap'}}>
        <span style={{marginLeft: level * 20}}>{label}</span>
    </td>
);

const NightModeRemovalButton = ({onRemove, path}) => (
    <button
        className="d-flex align-items-center px-1 col-red"
        onClick={() => onRemove?.(path)}
        style={{marginLeft: 8, marginRight: 8}}
    >
        <Fa icon={faTimes}/>
    </button>
);

const UseCaseDropdown = ({id, colors, value, onChange, label, onRemoveOverride, path}) => (
    <td style={{paddingBottom: '8px', width: '100%'}}>
        <div style={{display: 'flex', alignItems: 'center', width: onRemoveOverride ? '90%' : 'calc(90% - 44px)'}}>
            <div style={{flexGrow: 1}}>
                <ColorDropdown
                    id={id} key={id}
                    colors={colors}
                    value={value}
                    onChange={onChange}
                    label={label}
                />
            </div>
            {onRemoveOverride && (
                <NightModeRemovalButton onRemove={onRemoveOverride} path={path}/>
            )}
        </div>
    </td>
);

const UseCaseArraySelector = ({
                                  value,
                                  isNightMode,
                                  baseValue,
                                  id,
                                  onHoverChange,
                                  level,
                                  path,
                                  colors,
                                  onChange,
                                  onRemoveOverride
                              }) => {
    const ref = useRef();
    const {registerMinHeight, unregisterMinHeight} = useMinHeightContext();
    const selector = `selector-${path.join('_')}`;
    const selectedNames = value || [];

    useEffect(() => {
        if (ref.current) {
            if (unregisterMinHeight(selector, isNightMode, selectedNames.length)) {
                setTimeout(() => {
                    registerMinHeight(selector, ref.current.offsetHeight, isNightMode, selectedNames.length);
                }, 0);
            } else {
                registerMinHeight(selector, ref.current.offsetHeight, isNightMode, selectedNames.length);
            }
        }
    }, [ref, registerMinHeight, selectedNames.length]);

    const showRemove = isNightMode && JSON.stringify(value) !== JSON.stringify(baseValue);
    return (
        <tr id={id}
            onMouseOver={() => onHoverChange(id, 'enter', isNightMode)}
            onFocus={() => onHoverChange(id, 'enter', isNightMode)}
            onMouseOut={() => onHoverChange(id, 'exit', isNightMode)}
            onBlur={() => onHoverChange(id, 'exit', isNightMode)}>
            <UseCaseLabel level={level} label={formatLabel(path[path.length - 1])}/>
            <td style={{paddingBottom: '8px', width: '100%'}}>
                <div style={{display: 'flex', alignItems: 'center'}}>
                    <div ref={ref} style={{
                        width: showRemove ? '100%' : 'calc(100% - 44px)',
                        display: 'flex',
                        alignItems: 'center'
                    }} className={selector}>
                        <ColorMultiSelect
                            style={{flexGrow: 1}}
                            colors={colors}
                            selectedColors={selectedNames}
                            setSelectedColors={newNames => {
                                onChange(newNames, path);
                            }}
                        />
                    </div>
                    {showRemove && (
                        <NightModeRemovalButton onRemove={onRemoveOverride} path={path}/>
                    )}
                </div>
            </td>
        </tr>
    );
}
const UseCase = ({path, value, onChange, onHoverChange, colors, isNightMode, baseValue, onRemoveOverride}) => {
    const level = Math.max(0, path.length - 1);
    const id = path.join('.');

    if (typeof value === 'string') {
        const showRemove = isNightMode && value !== baseValue;
        return (
            <tr id={id}
                onMouseOver={() => onHoverChange(id, 'enter', isNightMode)}
                onFocus={() => onHoverChange(id, 'enter', isNightMode)}
                onMouseOut={() => onHoverChange(id, 'exit', isNightMode)}
                onBlur={() => onHoverChange(id, 'exit', isNightMode)}>
                <UseCaseLabel level={level} label={formatLabel(path[path.length - 1])}/>
                <UseCaseDropdown
                    id={id}
                    colors={colors}
                    value={value}
                    onChange={(newValue) => onChange(newValue, path)}
                    label={formatLabel(path[path.length - 1])}
                    {...(showRemove ? {onRemoveOverride, path} : {})}
                />
            </tr>
        );
    }

    if (Array.isArray(value)) {
        // Use the array of names directly
        return <UseCaseArraySelector {...{
            value,
            isNightMode,
            baseValue,
            id,
            onHoverChange,
            level,
            path,
            colors,
            onChange,
            onRemoveOverride
        }}/>;
    }

    return (
        <>
            {typeof value === 'object' && !Array.isArray(value) && path.length > 0 && (
                <SectionLabel
                    level={level}
                    label={formatLabel(path[path.length - 1])}
                    id={id}
                    isNightMode={isNightMode}
                    onHoverChange={onHoverChange}
                />
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

export default UseCase;