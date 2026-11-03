import React from 'react';
import { Trans } from 'react-i18next'; /* eslint-disable jsx-a11y/anchor-has-content */

/* eslint-disable jsx-a11y/anchor-has-content */
const ApiTokenHelp = () => {
    return (
        <div className={'api-token-help'}>
            <p>
                <Trans i18nKey='html.label.help.manage.tokens.line-1' />
            </p>
            <p>
                <Trans
                    i18nKey='html.label.help.manage.tokens.line-2'
                    shouldUnescape={true}
                    values={{
                        header: 'X-Plan-Token',
                        token: 'your-token-value',
                    }}
                    components={{ 1: <code />, 2: <code /> }}
                />
            </p>
            <p>
                <Trans i18nKey='html.label.help.manage.tokens.line-3' />
            </p>
            <p>
                <Trans
                    i18nKey='html.label.help.manage.tokens.line-4'
                    shouldUnescape={true}
                    values={{
                        command:
                            'curl -H "X-Plan-Token: your-token-value" https://localhost:8804/v1/whoami',
                    }}
                    components={{ 1: <code /> }}
                />
            </p>
            <hr />
            <p>
                <Trans i18nKey='html.label.help.manage.tokens.line-5' />
            </p>
            <p>
                <Trans i18nKey='html.label.help.manage.tokens.line-6' />
            </p>
            <hr />
            <p>
                <Trans
                    i18nKey='html.label.help.manage.tokens.line-7'
                    shouldUnescape={true}
                    values={{
                        link: 'https://github.com/plan-player-analytics/Plan/wiki/Web-permissions',
                    }}
                    components={{
                        1: (
                            <a
                                rel='noopener noreferrer'
                                target='_blank'
                                href={
                                    'https://github.com/plan-player-analytics/Plan/wiki/Web-permissions'
                                }
                            />
                        ),
                    }}
                />
            </p>
        </div>
    );
};
/* eslint-enable jsx-a11y/anchor-has-content */

export default ApiTokenHelp;
