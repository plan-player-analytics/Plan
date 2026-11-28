import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
    faCircleCheck,
    faCircleQuestion,
    faCircleXmark,
} from '@fortawesome/free-regular-svg-icons';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { classNames } from '../../util/classNames';

type Props = {
    value: boolean | null;
};

export const ExtensionBoolean = ({ value }: Props) => {
    const { t } = useTranslation();
    const icon =
        value === true
            ? faCircleCheck
            : value === false
              ? faCircleXmark
              : faCircleQuestion;
    const color =
        value === true
            ? 'col-plugin-green'
            : value === false
              ? 'col-plugin-red'
              : 'col-plugin-grey';
    const label =
        value === null
            ? 'plugin.generic.unknown'
            : value
              ? 'plugin.generic.yes'
              : 'plugin.generic.no';
    return (
        <>
            <FontAwesomeIcon
                icon={icon}
                className={classNames('me-1', color)}
                aria-hidden='true'
            />
            {t(label)}
        </>
    );
};
