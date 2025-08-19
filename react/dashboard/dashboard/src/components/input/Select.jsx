import React from 'react';

const Select = ({options, selectedIndex, setSelectedIndex, className}) => {
    const handleChange = (event) => {
        const renderedOptions = Object.values(event.target.selectedOptions)
            .map(htmlElement => htmlElement.text)
            .map(option => options.indexOf(option));
        setSelectedIndex(renderedOptions[0]);
    }

    return (
        <select className={`form-control form-select ${className || ''}`}
                onChange={handleChange}>
            {options.map((option, i) => {
                return (
                    <option key={JSON.stringify(option)} value={selectedIndex === i}
                            selected={selectedIndex === i}>{option}</option>
                )
            })}
        </select>
    )
};

export default Select