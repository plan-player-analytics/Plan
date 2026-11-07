import React, { ChangeEvent } from 'react';
import { classNames } from '../../util/classNames';

type Props = {
    options: string[];
    selectedIndex: number;
    setSelectedIndex: (index: number) => void;
    className?: string;
};

const Select = ({
    options,
    selectedIndex,
    setSelectedIndex,
    className,
}: Props) => {
    const handleChange = (event: ChangeEvent<HTMLSelectElement>) => {
        const renderedOptions = Object.values(event.target.selectedOptions)
            .map((htmlElement) => htmlElement.text)
            .map((option) => options.indexOf(option));
        setSelectedIndex(renderedOptions[0]);
    };

    return (
        <select
            className={classNames('form-control form-select', className)}
            onChange={handleChange}>
            {options.map((option, i) => (
                <option
                    key={JSON.stringify(option)}
                    value={String(selectedIndex === i)}
                    selected={selectedIndex === i}>
                    {option}
                </option>
            ))}
        </select>
    );
};

export default Select;
