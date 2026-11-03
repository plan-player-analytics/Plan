import { useTranslation } from 'react-i18next';
import { Card } from 'react-bootstrap';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { faBookOpen } from '@fortawesome/free-solid-svg-icons';
import React from 'react';
import OnlineActivityAsNumbersTable from '../../../table/OnlineActivityAsNumbersTable';
import {
    TimeframeControl,
    useDatapointTimeframe,
} from '../../../table/DatapointTimeframe';
import { ALL_TIMEFRAME_PRESETS } from '../../../../util/datapointTablePresets';

const OnlineActivityAsNumbersCard = () => {
    const { t } = useTranslation();
    const timeframe = useDatapointTimeframe(
        'online-activity-numbers',
        ALL_TIMEFRAME_PRESETS,
    );

    return (
        <Card id={'online-activity-numbers'}>
            <Card.Header>
                <h6 className='col-text'>
                    <Fa icon={faBookOpen} className='col-players-unique' />{' '}
                    {t('html.label.onlineActivityAsNumbers')}
                </h6>
            </Card.Header>
            <OnlineActivityAsNumbersTable
                columns={timeframe.columns}
                headerControl={
                    <TimeframeControl
                        id={'online-activity-numbers'}
                        timeframe={timeframe}
                    />
                }
            />
        </Card>
    );
};

export default OnlineActivityAsNumbersCard;
