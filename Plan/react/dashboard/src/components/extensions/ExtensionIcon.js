import {FontAwesomeIcon as Fa} from '@fortawesome/react-fontawesome'
import {iconTypeToFontAwesomeClass} from "../../util/icons";
import React from "react";

const ExtensionIcon = ({icon}) => (
    <Fa icon={[
        iconTypeToFontAwesomeClass(icon.family),
        icon.iconName
    ]}
        className={icon.colorClass}
    />
)

export const toExtensionIconHtmlString = ({icon}) => {
    return icon ? `<i class="${iconTypeToFontAwesomeClass(icon.family)} ${icon.iconName} ${icon.colorClass}"></i>` : '';
}

export default ExtensionIcon;
