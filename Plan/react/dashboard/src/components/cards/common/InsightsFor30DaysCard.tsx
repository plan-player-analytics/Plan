import { useTranslation } from 'react-i18next';
import { Card } from 'react-bootstrap';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import React, { PropsWithChildren, ReactNode } from 'react';
import { faLifeRing } from '@fortawesome/free-regular-svg-icons';

type Props = {
    id?: string;
    title?: ReactNode;
} & PropsWithChildren;

const InsightsFor30DaysCard = ({ id, children, title }: Props) => {
    const { t } = useTranslation();
    return (
        <Card id={id}>
            <Card.Header>
                <h6 className='col-text'>
                    <Fa icon={faLifeRing} className='col-insights' />{' '}
                    {title || t('html.label.insights30days')}
                </h6>
            </Card.Header>
            <Card.Body>{children}</Card.Body>
        </Card>
    );
};

export default InsightsFor30DaysCard;
