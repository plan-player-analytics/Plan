import React, {useState} from 'react';

const Toggle = ({children, value, onValueChange, color, inline, className}) => {
    const [renderTime] = useState(new Date().getTime());
    const id = 'checkbox-' + renderTime;

    const handleChange = () => {
        onValueChange(!value);
    }

    return (
        <div className={`form-check form-switch ${className}`} style={inline ? {display: "inline-block"} : {}}>
            <input id={id} type={"checkbox"} className={"form-check-input bg-" + color} role="switch"
                   onChange={handleChange} checked={value}/>
            <label className="form-check-label" htmlFor={id}>{children}</label>
        </div>
    )
};

export default Toggle