import React from 'react';
import {nameToContrastCssVariable, nameToCssVariable} from "../../util/colors.js";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCheck, faPalette} from "@fortawesome/free-solid-svg-icons";

const ColorSelectorButton = ({color, setColor, disabled, active}) => {
    const validCssColor = color => {
        return color === 'theme' ? 'reference-colors-theme' : color;
    }
    return (
        <button className={`btn color-chooser ${disabled ? "disabled" : ''} ${active ? 'active' : ''}`}
                style={{
                    color: nameToContrastCssVariable(validCssColor(color)),
                    background: nameToCssVariable(validCssColor(color))
                }}
                id={"choose-" + color}
                disabled={disabled}
                onClick={() => setColor(color)}
        >
            <Fa icon={active ? faCheck : faPalette}/>
        </button>
    )
}

export default ColorSelectorButton