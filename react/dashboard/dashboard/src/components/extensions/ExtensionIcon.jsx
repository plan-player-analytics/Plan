import {FontAwesomeIcon as Fa} from '@fortawesome/react-fontawesome'
import {iconTypeToFontAwesomeClass} from "../../util/icons.ts";
import React from "react";

const ExtensionIcon = ({icon}) => {
    return (
        <Fa icon={[
            iconTypeToFontAwesomeClass(icon.family),
            icon.iconName
        ]}
            className={icon.colorClass}
        />
    )
}

export default ExtensionIcon;
