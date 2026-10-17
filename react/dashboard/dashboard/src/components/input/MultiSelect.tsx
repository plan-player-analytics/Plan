import React, {useMemo} from 'react';
import Select from 'react-select';
import {useTranslation} from "react-i18next";

type Props<T> = {
    options: T[];
    selectedIndexes: number[];
    setSelectedIndexes: (indexes: number[]) => void;
    className?: string;
    style?: CSSStyleProperties;
}

function MultiSelect<T>({options, selectedIndexes, setSelectedIndexes, className, style}: Props<T>) {
    const {t} = useTranslation();
    // Convert options to react-select format
    const selectOptions = options.map((option, index) => ({
        value: index,
        label: typeof option === 'string' ? t(option) : option
    }));

    // Convert selectedIndexes to react-select format
    const selectedOptions = selectedIndexes.map(index => selectOptions[index]);

    const handleChange = (selectedOptions: any) => {
        const newSelectedIndexes = selectedOptions ? selectedOptions.map((option: any) => option.value) : [];
        setSelectedIndexes(newSelectedIndexes);
    };

    const bootstrapStyles = useMemo(() => ({
        container: (provided: any) => ({...provided, ...style}),
        control: (provided: any) => ({
            ...provided,
            background: 'var(--color-forms-input-background)',
            borderColor: 'var(--color-forms-input-border)',
            minHeight: '38px',
            boxShadow: 'none',
            '&:hover': {borderColor: 'var(--color-forms-input-border)'},
        }),
        menu: (provided: any) => ({
            ...provided,
            background: 'var(--color-forms-input-background)',
            borderRadius: '0.375rem',
            border: '1px solid var(--color-forms-input-border)',
            boxShadow: '0 0.5rem 1rem rgba(0,0,0,.15)',
            marginTop: 2,
            zIndex: 9999,
        }),
        option: (provided: any, state: any) => ({
            ...provided,
            background: state.isFocused
                ? 'color-mix(in srgb, var(--contrast-color-forms-input-background), transparent 90%)'
                : 'var(--color-forms-input-background)',
            color: 'var(--color-text)',
            padding: '0.375rem 1.5rem',
            cursor: 'pointer',
        }),
        multiValue: (provided: any) => ({
            ...provided,
            background: 'var(--color-forms-multi-select-item-background)',
            borderRadius: '0.2rem',
            padding: '0 2px',
        }),
        multiValueLabel: (provided: any) => ({
            ...provided,
            color: 'var(--color-text)',
            fontSize: '0.875em',
        }),
        multiValueRemove: (provided: any) => ({
            ...provided,
            color: 'var(--color-text)',
            ':hover': {
                background: 'var(--color-forms-buttons-dangerous-button)',
                color: 'var(--contrast-color-forms-buttons-dangerous-button)',
            },
        }),
        dropdownIndicator: (provided: any) => ({
            ...provided,
            color: 'var(--color-text)'
        }),
        clearIndicator: (provided: any) => ({
            ...provided,
            color: 'var(--color-text)',
            ':hover': {color: 'var(--color-forms-buttons-dangerous-button)'},
        }),
        indicatorSeparator: (provided: any) => ({
            ...provided,
            background: 'var(--color-forms-input-text)',
        }),
        input: (provided: any) => ({
            ...provided,
            color: 'var(--color-forms-input-text)',
        })
    }), []);

    return (
        <Select
            isMulti
            closeMenuOnSelect={false}
            options={selectOptions}
            value={selectedOptions}
            onChange={handleChange}
            className={className}
            classNamePrefix="react-select"
            styles={bootstrapStyles}
            placeholder={t('html.label.select.select')}
            noOptionsMessage={() => t('html.label.select.noOptions')}
        />
    );
}

export default MultiSelect;