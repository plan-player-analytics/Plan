import React from "react";
import {getContrastColor} from '../../util/colors';
import {Col} from 'react-bootstrap';

const BackgroundColorBox = ({name, color}) => {
    // Convert color value to CSS variable if it's a variable reference
    const cssColor = color.startsWith('var(') ? color : `var(--col-${name})`;
    const contrastColor = getContrastColor(color);

    return (
        <Col lg={2} md={3} sm={4} className="mb-2 px-2 background-color-box">
            <div
                className="color-box-wrapper"
                style={{'--box-color': cssColor, '--box-contrast-color': contrastColor}}
                title={`${name}: ${color}`}
            >
                <span>{name}</span>
                <div>{color}</div>
            </div>
        </Col>
    );
};

const TextColorBox = ({name, color}) => {
    const needsDarkBackground = name.startsWith('text-dark') || name.includes('night');
    const cssColor = color.startsWith('var(') ? color : `var(--col-${name})`;

    return (
        <Col lg={2} md={3} sm={4} className={`mb-2 px-2 text-color-box ${needsDarkBackground ? 'night-mode' : ''}`}>
            <div
                className="color-box-wrapper"
                style={{'--box-color': cssColor}}
                title={`${name}: ${color}`}
            >
                <span>{name}</span>
                <div>{color}</div>
            </div>
        </Col>
    );
};

export const ColorBox = ({name, color}) => {
    const isTextColor = name.includes('text');
    return isTextColor ?
        <TextColorBox name={name} color={color}/> :
        <BackgroundColorBox name={name} color={color}/>;
};
