import { ExtensionTab } from '../../dataHooks/model/extension/ExtensionData';
import { Col, Table } from 'react-bootstrap';
import { CardSection } from '../cards/CardSection';
import { useTranslation } from 'react-i18next';
import React from 'react';
import ExtensionIcon from './ExtensionIcon';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
    faCircleCheck,
    faCircleQuestion,
    faCircleXmark,
} from '@fortawesome/free-regular-svg-icons';

const flagValueOrder = (value: boolean | null) => {
    if (value === true) return 0;
    if (value === false) return 1;
    return 2;
};

type Props = {
    uuid: string;
    tab: ExtensionTab;
    width: number;
};

export const ExtensionFlags = ({ uuid, tab, width }: Props) => {
    const { t } = useTranslation();

    return (
        <>
            {tab.flagsData.map((flags) => (
                <Col
                    key={flags.providerName}
                    md={width}
                    className={'extension-section-wrapper-' + uuid}>
                    <CardSection noPadding>
                        <Table striped size='sm' className='mb-0'>
                            <thead className={flags.tableColorClass}>
                                <tr>
                                    <th
                                        colSpan={2}
                                        style={
                                            flags.tableColor === 'NONE'
                                                ? undefined
                                                : {
                                                      backgroundColor: `var(--color-plugin-${flags.tableColor.toLowerCase().replace('_', '-')})`,
                                                  }
                                        }>
                                        <ExtensionIcon
                                            icon={{
                                                ...flags.icon,
                                                colorClass: '',
                                            }}
                                        />{' '}
                                        {flags.providerText}
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.entries(flags.flags.valueByKey)
                                    .sort(
                                        (
                                            [firstName, firstValue],
                                            [secondName, secondValue],
                                        ) =>
                                            flagValueOrder(firstValue) -
                                                flagValueOrder(secondValue) ||
                                            firstName.localeCompare(
                                                secondName,
                                                undefined,
                                                {
                                                    sensitivity: 'base',
                                                },
                                            ),
                                    )
                                    .map(([name, value]) => (
                                        <tr key={name}>
                                            <td>{name}</td>
                                            <td>
                                                <FontAwesomeIcon
                                                    icon={
                                                        value === true
                                                            ? faCircleCheck
                                                            : value === false
                                                              ? faCircleXmark
                                                              : faCircleQuestion
                                                    }
                                                    className={
                                                        value === true
                                                            ? 'me-1 col-plugin-green'
                                                            : value === false
                                                              ? 'me-1 col-plugin-red'
                                                              : 'me-1 col-plugin-grey'
                                                    }
                                                    aria-hidden='true'
                                                />
                                                {value === null
                                                    ? t(
                                                          'plugin.generic.unknown',
                                                      )
                                                    : t(
                                                          value
                                                              ? 'plugin.generic.yes'
                                                              : 'plugin.generic.no',
                                                      )}
                                            </td>
                                        </tr>
                                    ))}
                            </tbody>
                        </Table>
                    </CardSection>
                </Col>
            ))}
        </>
    );
};
