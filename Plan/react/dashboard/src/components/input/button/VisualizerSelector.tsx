import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {IconProp} from "@fortawesome/fontawesome-svg-core";

type Props = {
    onClick: () => void;
    icon: IconProp;
}

const VisualizerSelector = ({onClick, icon}: Props) => {
    return (
        <button className="btn float-end visualizer-button" onClick={onClick}>
            <FontAwesomeIcon icon={icon}/>
        </button>
    )
}

export default VisualizerSelector