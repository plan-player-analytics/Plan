import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const VisualizerSelector = ({onClick, icon}) => {
    return (
        <button className="btn float-end visualizer-button" onClick={onClick}>
            <FontAwesomeIcon icon={icon}/>
        </button>
    )
}

export default VisualizerSelector