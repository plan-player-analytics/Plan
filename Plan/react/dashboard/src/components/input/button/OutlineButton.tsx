import React, {CSSProperties, PropsWithChildren} from 'react';
import {classNames} from "../../../util/classNames";

type Props = {
    id?: string;
    className?: string;
    disabled?: boolean;
    onClick: () => void;
    style?: CSSProperties;
    variant?: 'input'
} & PropsWithChildren

const OutlineButton = ({id, className, disabled, onClick, children, style, variant}: Props) => {
    let variantClass = undefined;
    if (variant === 'input') variantClass = ' btn-input';
    return (
        <button id={id}
                className={classNames("btn btn-outline-secondary", className, variantClass)}
                onClick={onClick}
                disabled={disabled}
                style={style}>
            {children}
        </button>
    )
};

export default OutlineButton