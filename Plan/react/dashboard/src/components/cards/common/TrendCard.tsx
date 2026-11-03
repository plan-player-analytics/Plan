import { IconProp } from '@fortawesome/fontawesome-svg-core';
import { Trend } from '../../../dataHooks/model/datapoint/Trend';
import { Card } from 'react-bootstrap';
import Datapoint from '../../datapoint/Datapoint';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { faLongArrowAltRight } from '@fortawesome/free-solid-svg-icons';
import BigTrend from '../../trend/BigTrend';
import React, { ReactNode } from 'react';

type Props = {
    icon: IconProp;
    color?: string;
    name: ReactNode;
    previous: ReactNode;
    next: ReactNode;
    trend?: Trend | null;
};

const TrendCard = ({ icon, color, name, previous, next, trend }: Props) => {
    return (
        <Card>
            <Card.Body>
                <Datapoint
                    icon={icon}
                    name={name}
                    color={color}
                    value={
                        <>
                            {previous} <Fa icon={faLongArrowAltRight} /> {next}{' '}
                            <BigTrend trend={trend} />
                        </>
                    }
                />
            </Card.Body>
        </Card>
    );
};

export default TrendCard;
