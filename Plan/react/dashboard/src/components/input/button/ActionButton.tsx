import React, {CSSProperties, PropsWithChildren} from 'react';

type Props = {
    id?: string;
    className?: string;
    disabled?: boolean,
    onClick: (() => void) | (() => Promise<void>);
    style?: CSSProperties;
    title: string;
} & PropsWithChildren;

const ActionButton = ({id, className, disabled, onClick, children, style, title}: Props) => {
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