import {ColorBox} from "./ColorBox.jsx";
import React from "react";

const ColorSection = ({title, colors}) => (
    <div className="mb-1">
        <h5 className="mb-3">{title}</h5>
        <div className="row">
            {Object.entries(colors).map(([name, color]) => (
                <ColorBox key={name} name={name} color={color}/>
            ))}
        </div>
    </div>
);

export default ColorSection;