import React, {CSSProperties, PropsWithChildren} from 'react';

type Props = {
    id?: string;
    className?: string;
    disabled?: boolean;
    onClick: () => void;
    style?: CSSProperties;
} & PropsWithChildren

const OutlineButton = ({id, className, disabled, onClick, children, style}: Props) => {
    return (
        <button id={id}
                className={"btn btn-outline-secondary " + className}
                onClick={onClick}
                disabled={disabled}
                style={style}>
            {children}
        </button>
    )
};

export default OutlineButton