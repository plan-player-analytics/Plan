import React, {ChangeEvent, useCallback} from 'react';

type Option = {
    name: string;
    disabled?: boolean;
    displayName: string;
}

type Props = {
    selected: string;
    onChange: (selected: string) => void;
    options: Option[];
}

export const BasicDropdown = ({selected, onChange, options}: Props) => {
    const onSelect = useCallback(({target}: ChangeEvent<HTMLSelectElement>) => {
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