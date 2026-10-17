import React, {PropsWithChildren} from 'react';

const Background = ({children}: PropsWithChildren) => {
    return <div style={{backgroundColor: "var(--color-cards-background"}}>{children}</div>
}

export default Background