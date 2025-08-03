import React from 'react';

const ActionButton = ({id, className, disabled, onClick, children, style}) => {
    return (
        <button id={id}
                className={"btn btn-action " + className}
                onClick={onClick}
                disabled={disabled}
                style={style}>
            {children}
        </button>
    )
};

export default ActionButton