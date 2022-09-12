import React from 'react';

const MultiSelect = ({options, selectedIndexes, setSelectedIndexes}) => {
    const handleChange = (event) => {
        const renderedOptions = Object.values(event.target.selectedOptions)
            .map(htmlElement => htmlElement.text)
            .map(option => options.indexOf(option));
        setSelectedIndexes(renderedOptions);
    }

    return (
        <select className="form-control" multiple
                onChange={handleChange}>
            {options.map((option, i) => {
                return (
                    <option key={i} value={selectedIndexes.includes(i)}
                            selected={selectedIndexes.includes(i)}>{option}</option>
                )
            })}
        </select>
    )
};

export default MultiSelect