import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { iconTypeToFontAwesomeClass } from '../../util/icons';
import React from 'react';
import { ExtensionIcon as DataType } from '../../dataHooks/model/extension/ExtensionData';

type Props = {
    icon?: DataType;
};

const ExtensionIcon = ({ icon }: Props) => {
    if (!icon) return null;
    return (
        <Fa
            icon={[iconTypeToFontAwesomeClass(icon.family), icon.iconName]}
            className={icon.colorClass}
        />
    );
};

export default ExtensionIcon;
