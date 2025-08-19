import React from 'react';

const ModalCloseButton = ({onClick}) => {
    return (
        <button aria-label="Close" className="btn-close" type="button" onClick={onClick}/>
    )
};

export default ModalCloseButton