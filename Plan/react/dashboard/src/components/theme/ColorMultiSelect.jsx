import React, {useEffect, useState} from 'react';
import MultiSelect from "../input/MultiSelect.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSquare} from "@fortawesome/free-solid-svg-icons";

const ColorMultiSelect = ({className, colors, selectedColors, setSelectedColors, sort}) => {
    const colorArray = Object.keys(colors);
    const [selectedIndexes, setSelectedIndexes] = useState(selectedColors ? selectedColors.map(color => colorArray.indexOf(color)) : []);

    const changeSelectedIndexes = selected => {
        setSelectedIndexes(sort ? selected.toSorted() : selected);
    }

    const options = colorArray.map(color => (
        <span key={color} style={{
            color: `var(--color-${color})`,
        }}><FontAwesomeIcon icon={faSquare}/> {color}</span>
    ));

    useEffect(() => {
        if (selectedIndexes.length) {
            if (selectedIndexes.length !== selectedColors.length) {
                setSelectedColors?.(selectedIndexes.map(index => colorArray[index]));
            }
        } else {
            setSelectedIndexes([0]);
        }
    }, [selectedIndexes, selectedColors]);

    return (
        <MultiSelect className={className} options={options} selectedIndexes={selectedIndexes}
                     setSelectedIndexes={changeSelectedIndexes}/>
    )
};

export default ColorMultiSelect