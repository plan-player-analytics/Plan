import React, {useCallback} from 'react';

export const BasicDropdown = ({selected, onChange, options}) => {
    const onSelect = useCallback(({target}) => {
        onChange(target.value);
    }, [onChange]);

    return (
        <select onChange={onSelect}
                className="form-select form-select-sm"
                defaultValue={selected}>
            {options.map((option, i) =>
                <option key={option.name} value={option.name} disabled={option.disabled}>{option.displayName}</option>)}
        </select>
    )
};