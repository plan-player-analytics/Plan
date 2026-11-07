import React, { CSSProperties, PropsWithChildren } from 'react';

type Props = {
    id?: string;
    className?: string;
    disabled?: boolean;
    onClick: (() => void) | (() => Promise<void>);
    style?: CSSProperties;
    title?: string;
} & PropsWithChildren;
const SecondaryActionButton = ({
    className,
    onClick,
    disabled,
    children,
    id,
    title,
    style,
}: Props) => {
    return (
        <button
            className={`btn btn-secondary-action ${className}`}
            id={id}
            title={title}
            onClick={onClick}
            disabled={disabled}
            style={style}>
            {children}
        </button>
    );
};

export default SecondaryActionButton;
