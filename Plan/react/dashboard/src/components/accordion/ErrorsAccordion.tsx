import React from 'react';
import Accordion from './Accordion';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBug } from '@fortawesome/free-solid-svg-icons';
import { faClock } from '@fortawesome/free-regular-svg-icons';
import { useTranslation } from 'react-i18next';

type ErrorLog = {
    fileName: string;
    contents: string[];
};

type Props = {
    errors?: ErrorLog[];
};

type ErrorProps = {
    error: ErrorLog;
};

const ErrorBody = ({ error }: ErrorProps) => {
    return (
        <pre className='pre-scrollable' style={{ overflowX: 'scroll' }}>
            {error.contents.map((line) => (
                <>
                    {line}
                    <br />
                </>
            ))}
        </pre>
    );
};

const ErrorHeader = ({ error }: ErrorProps) => {
    const { t } = useTranslation();
    return (
        <>
            <td>{error.fileName}</td>
            <td>
                {error.contents.length
                    ? error.contents[0]
                    : t('html.label.emptyFile')}
            </td>
        </>
    );
};

const ErrorsAccordion = ({ errors }: Props) => {
    const { t } = useTranslation();
    const headers = [
        <>
            <FontAwesomeIcon icon={faBug} /> {t('html.label.logfile')}
        </>,
        <>
            <FontAwesomeIcon icon={faClock} /> {t('html.label.occurrences')}
        </>,
    ];
    const slices = errors
        ? errors.map((error) => {
              return {
                  body: <ErrorBody error={error} />,
                  header: <ErrorHeader error={error} />,
                  color: 'orange',
                  outline: true,
              };
          })
        : [];

    return (
        <Accordion
            headers={headers}
            slices={slices}
            open={false}
            style={{ tableLayout: 'fixed' }}
        />
    );
};

export default ErrorsAccordion;
