import React from 'react';
import Accordion from './Accordion';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBug } from '@fortawesome/free-solid-svg-icons';
import { faClock } from '@fortawesome/free-regular-svg-icons';

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
    return (
        <>
            <td>{error.fileName}</td>
            <td>
                {error.contents.length ? error.contents[0] : '{Empty file}'}
            </td>
        </>
    );
};

const ErrorsAccordion = ({ errors }: Props) => {
    const headers = [
        <>
            <FontAwesomeIcon icon={faBug} /> Logfile
        </>,
        <>
            <FontAwesomeIcon icon={faClock} /> Occurrences
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
