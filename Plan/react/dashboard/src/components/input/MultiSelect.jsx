import React, {useMemo} from 'react';
import Select from 'react-select';
import {useTranslation} from "react-i18next";

const MultiSelect = ({options, selectedIndexes, setSelectedIndexes, className, style}) => {
    const {t} = useTranslation();
    // Convert options to react-select format
    const selectOptions = options.map((option, index) => ({
        value: index,
        label: typeof option === 'string' ? t(option) : option
    }));

    // Convert selectedIndexes to react-select format
    const selectedOptions = selectedIndexes.map(index => selectOptions[index]);

    const handleChange = (selectedOptions) => {
        const newSelectedIndexes = selectedOptions ? selectedOptions.map(option => option.value) : [];
        setSelectedIndexes(newSelectedIndexes);
    };

    const bootstrapStyles = useMemo(() => ({
        container: (provided) => ({...provided, ...style}),
        control: (provided) => ({
            ...provided,
            background: 'var(--color-forms-input-background)',
            borderColor: 'var(--color-forms-input-border)',
            minHeight: '38px',
            boxShadow: 'none',
            '&:hover': {borderColor: 'var(--color-forms-input-border)'},
        }),
        menu: (provided) => ({
            ...provided,
            background: 'var(--color-forms-input-background)',
            borderRadius: '0.375rem',
            border: '1px solid var(--color-forms-input-border)',
            boxShadow: '0 0.5rem 1rem rgba(0,0,0,.15)',
            marginTop: 2,
            zIndex: 9999,
        }),
        option: (provided, state) => ({
            ...provided,
            background: state.isFocused
                ? 'color-mix(in srgb, var(--contrast-color-forms-input-background), transparent 90%)'
                : 'var(--color-forms-input-background)',
            color: 'var(--color-text)',
            padding: '0.375rem 1.5rem',
            cursor: 'pointer',
        }),
        multiValue: (provided) => ({
            ...provided,
            background: 'var(--color-forms-multi-select-item-background)',
            borderRadius: '0.2rem',
            padding: '0 2px',
        }),
        multiValueLabel: (provided) => ({
            ...provided,
            color: 'var(--color-text)',
            fontSize: '0.875em',
        }),
        multiValueRemove: (provided) => ({
            ...provided,
            color: 'var(--color-text)',
            ':hover': {
                background: 'var(--color-forms-buttons-dangerous-button)',
                color: 'var(--contrast-color-forms-buttons-dangerous-button)',
            },
        }),
        dropdownIndicator: (provided) => ({
            ...provided,
            color: 'var(--color-text)'
        }),
        clearIndicator: (provided) => ({
            ...provided,
            color: 'var(--color-text)',
            ':hover': {color: 'var(--color-forms-buttons-dangerous-button)'},
        }),
        indicatorSeparator: (provided) => ({
            ...provided,
            background: 'var(--color-forms-input-text)',
        }),
        input: (provided) => ({
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
            noOptionsMessage={t('html.label.select.noOptions')}
            placeholder={t('html.label.select.select')}
        />
    );
};

export default MultiSelect;