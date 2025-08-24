import React from 'react';
import MultiSelect from "../input/MultiSelect.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSquare} from "@fortawesome/free-solid-svg-icons";

const ColorMultiSelect = ({className, colors, selectedColors, setSelectedColors, sort, style}) => {
    const colorArray = Object.keys(colors);
    const selectedIndexes = selectedColors ? selectedColors.map(color => colorArray.indexOf(color)) : [];

    const changeSelectedIndexes = selected => {
        const newIndexes = sort ? selected.toSorted() : selected;
        setSelectedColors?.(newIndexes.length ? newIndexes.map(index => colorArray[index]) : [colorArray[0]]);
    };

    const options = colorArray.map(color => (
        <span key={color} style={{
            color: `var(--color-${color})`,
        }}><FontAwesomeIcon icon={faSquare}/> {color}</span>
    ));

    return (
        <MultiSelect className={className} options={options} selectedIndexes={selectedIndexes}
                     setSelectedIndexes={changeSelectedIndexes} style={style}/>
    )
};

export default ColorMultiSelect