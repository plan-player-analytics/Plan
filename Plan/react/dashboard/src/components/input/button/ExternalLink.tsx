import React, { PropsWithChildren } from 'react';

type Props = {
    href: string;
} & PropsWithChildren;

const ExternalLink = ({ href, children }: Props) => {
    return (
        <a
            className='btn col-theme'
            href={href}
            rel='noopener noreferrer'
            target='_blank'>
            {children}
        </a>
    );
};

export default ExternalLink;
