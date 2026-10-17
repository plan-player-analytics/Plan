import React, {PropsWithChildren} from "react";

const Scrollable = ({children}: PropsWithChildren) => (
    <div className="scrollbar">
        {children}
    </div>
)

export default Scrollable;