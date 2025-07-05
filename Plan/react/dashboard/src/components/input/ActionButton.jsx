import React from 'react';

const ActionButton = ({id, className, disabled, onClick, children}) => {
    return (
        <button id={id}
                className={"btn btn-action " + className}
                onClick={onClick}
                disabled={disabled}>
            {children}
        </button>
    )
};

export default ActionButton