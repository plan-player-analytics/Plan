import React from 'react';

type Props = {
    onClick: () => void;
}

const ModalCloseButton = ({onClick}: Props) => {
    return (
        <button aria-label="Close" className="btn-close" type="button" onClick={onClick}/>
    )
};

export default ModalCloseButton