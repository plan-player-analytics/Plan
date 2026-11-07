import { useTranslation } from 'react-i18next';
import { Card } from 'react-bootstrap';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { faCampground } from '@fortawesome/free-solid-svg-icons';
import React, { ComponentProps } from 'react';
import ServerPvpPveAsNumbersTable from '../../../table/ServerPvpPveAsNumbersTable';

type Props = ComponentProps<typeof ServerPvpPveAsNumbersTable>;

const PvpPveAsNumbersCard = ({ killData }: Props) => {
    const { t } = useTranslation();
    return (
        <Card id={'pvp-pve-as-numbers'}>
            <Card.Header>
                <h6 className='col-text'>
                    <Fa icon={faCampground} className='col-player-kills' />{' '}
                    {t('html.label.pvpPveAsNumbers')}
                </h6>
            </Card.Header>
            <ServerPvpPveAsNumbersTable killData={killData} />
        </Card>
    );
};

export default PvpPveAsNumbersCard;
