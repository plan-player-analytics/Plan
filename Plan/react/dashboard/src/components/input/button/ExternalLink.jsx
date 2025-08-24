import React from 'react';

const ExternalLink = ({href, children}) => {
    return (
        <a className="btn col-theme" href={href}
           rel="noopener noreferrer" target="_blank">
            {children}
        </a>
    )
};

export default ExternalLink