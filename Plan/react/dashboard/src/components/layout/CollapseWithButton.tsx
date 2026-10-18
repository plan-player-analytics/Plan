import React, { PropsWithChildren, useState } from 'react';
import style from './CollapseWithButton.module.scss';

type Props = {
    title: string;
    coverToggle: boolean;
    open?: boolean;
    disabled?: boolean;
} & PropsWithChildren;

const CollapseWithButton = ({
    title,
    children,
    coverToggle,
    disabled,
    open,
}: Props) => {
    const [collapsed, setCollapsed] = useState(!open);

    const toggle = () => {
        setCollapsed(!collapsed);
    };

    return (
        <>
            {coverToggle && !collapsed && (
                <div
                    className={style.collapseToggleBackground}
                    onClick={toggle}
                    onTouchStart={toggle}
                    onKeyDown={toggle}
                />
            )}
            <button
                disabled={disabled}
                className={
                    'btn col-text dropdown-toggle ' +
                    (collapsed ? 'collapsed' : '')
                }
                onClick={toggle}>
                {title}
            </button>
            <div className={'collapse ' + (collapsed ? '' : 'show')}>
                {children}
            </div>
        </>
    );
};

export default CollapseWithButton;
