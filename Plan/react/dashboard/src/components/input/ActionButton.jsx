import React from 'react';

const ActionButton = ({id, className, disabled, onClick, children, style, title}) => {
    return (
        <button id={id}
                title={title}
                className={"btn btn-action " + className}
                onClick={onClick}
                disabled={disabled}
                style={style}>
            {children}
        </button>
    )
};

export default ActionButton