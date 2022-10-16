import React, {useState} from 'react';

const CollapseWithButton = ({title, children}) => {
    const [collapsed, setCollapsed] = useState(true);

    const toggle = () => {
        setCollapsed(!collapsed);
    }

    return (
        <>
            <button className={"btn dropdown-toggle " + (collapsed ? "collapsed" : "")}
                    onClick={toggle}>{title}</button>
            <div className={"collapse " + (collapsed ? '' : "show")}>
                {children}
            </div>
        </>
    )
};

export default CollapseWithButton