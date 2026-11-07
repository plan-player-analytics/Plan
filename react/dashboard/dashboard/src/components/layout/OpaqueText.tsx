import React, { PropsWithChildren } from 'react';

type Props = {
    inline?: boolean;
    floatEnd?: boolean;
} & PropsWithChildren;

const OpaqueText = ({ children, inline, floatEnd }: Props) => {
    if (inline) {
        return (
            <span className={'opaque-text' + (floatEnd ? ' float-end' : '')}>
                {children}
            </span>
        );
    }

    return (
        <p className={'opaque-text' + (floatEnd ? ' float-end' : '')}>
            {children}
        </p>
    );
};

export default OpaqueText;
