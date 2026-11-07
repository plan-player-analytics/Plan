import React, { CSSProperties, PropsWithChildren } from 'react';

type Props = {
    id?: string;
    className?: string;
    disabled?: boolean;
    onClick: (() => void) | (() => Promise<void>);
    style?: CSSProperties;
    title?: string;
} & PropsWithChildren;

const DangerButton = ({
    id,
    className,
    disabled,
    onClick,
    children,
    title,
    style,
}: Props) => {
    return (
        <button
            className={'btn btn-danger ' + className}
            id={id}
            title={title}
            onClick={onClick}
            disabled={disabled}
            style={style}>
            {children}
        </button>
    );
};

export default DangerButton;
