import LoadIn from '../../components/animation/LoadIn';
import { Card, Col, Row } from 'react-bootstrap';
import React, { Suspense, useCallback, useState } from 'react';
import CardHeader from '../../components/cards/CardHeader';
import {
    faCheck,
    faCopy,
    faExclamationTriangle,
    faKey,
    faPlus,
    faRotateLeft,
    faTag,
    faTrash,
} from '@fortawesome/free-solid-svg-icons';
import {
    FontAwesomeIcon as Fa,
    FontAwesomeIcon,
} from '@fortawesome/react-fontawesome';
import {
    TokenEditContextProvider,
    useTokenEditContext,
} from '../../hooks/context/tokenEditContextHook';
import { CardLoader, ChartLoader } from '../../components/navigation/Loader';
import { Trans, useTranslation } from 'react-i18next';
import {
    ConfigurationStorageContextProvider,
    useConfigurationStorageContext,
} from '../../hooks/context/configurationStorageContextHook';
import SideNavTabs from '../../components/layout/SideNavTabs';
import Scrollable from '../../components/Scrollable';
import { useAlertPopupContext } from '../../hooks/context/alertPopupContext';
import { DropdownStatusContextProvider } from '../../hooks/context/dropdownStatusContextHook';
import { useNavigation } from '../../hooks/navigationHook';
import { faQuestionCircle } from '@fortawesome/free-regular-svg-icons';
import { useAuth } from '../../hooks/authenticationHook';
import UnsavedChangesText from '../../components/text/UnsavedChangesText';
import ActionButton from '../../components/input/button/ActionButton';
import OutlineButton from '../../components/input/button/OutlineButton';
import SecondaryActionButton from '../../components/input/button/SecondaryActionButton';
import { PermissionTree } from '../../components/permissions/PermissionTree';
import { useApiTokens } from '../../dataHooks/useApiTokens';
import { useDeleteApiToken } from '../../dataHooks/useDeleteApiToken';
import { useCreateApiToken } from '../../dataHooks/useCreateApiToken';
import { ApiTokenWithPermissions } from '../../dataHooks/model/permissions/ApiToken';
import TextInput from '../../components/input/TextInput';

const TokensHeader = ({ label, icon }: { label: string; icon?: any }) => {
    return (
        <span className={'float-start'}>
            <FontAwesomeIcon icon={icon || faKey} /> {label}
        </span>
    );
};

const TokenBody = ({ apiToken }: { apiToken: ApiTokenWithPermissions }) => {
    const { t } = useTranslation();
    const {
        changed,
        permissionTree,
        togglePermission,
        isChecked,
        isIndeterminate,
        identifier,
        setIdentifier,
    } = useTokenEditContext();

    const copyToken = () => {
        navigator.clipboard.writeText(apiToken.token.token);
    };

    return (
        <Row>
            <Col>
                <div>
                    <h3 style={{ display: 'inline-block' }}>{identifier}</h3>
                    <UnsavedChangesText visible={changed} />
                    <DeleteTokenButton
                        apiToken={apiToken}
                        identifier={identifier}
                    />
                </div>

                <Row className='mt-3'>
                    <Col md={6}>
                        <TextInput
                            icon={faTag}
                            value={identifier}
                            setValue={(newValue) =>
                                setIdentifier(
                                    newValue.toLowerCase().replace(' ', '_'),
                                )
                            }
                        />
                    </Col>
                    <Col md={6}>
                        <TextInput
                            icon={faKey}
                            value={apiToken.token.token}
                            setValue={() => {}}
                            readOnly>
                            <OutlineButton
                                className='input-group-text'
                                style={{ whiteSpace: 'nowrap' }}
                                onClick={copyToken}>
                                <Fa icon={faCopy} />
                            </OutlineButton>
                        </TextInput>
                    </Col>
                </Row>

                <div className='mt-3'>
                    <h4>{t('html.label.managePage.tokenPermissions')}</h4>
                    <Scrollable>
                        {permissionTree?.children?.length && (
                            <PermissionTree
                                nodes={permissionTree.children}
                                togglePermission={togglePermission}
                                isChecked={isChecked}
                                isIndeterminate={isIndeterminate}
                            />
                        )}
                        {!permissionTree?.children?.length && <ChartLoader />}
                    </Scrollable>
                </div>
            </Col>
        </Row>
    );
};

const SaveButton = () => {
    const { t } = useTranslation();
    const { dirty, requestSave } = useConfigurationStorageContext();

    return (
        <ActionButton
            className={'float-end'}
            style={{ margin: '-0.5rem' }}
            disabled={!dirty}
            onClick={requestSave}>
            <Fa icon={faKey} /> {t('html.label.managePage.changes.save')}
        </ActionButton>
    );
};

const DeleteTokenButton = ({
    apiToken,
    identifier,
}: {
    apiToken: ApiTokenWithPermissions;
    identifier: string;
}) => {
    const [clicked, setClicked] = useState(false);
    const { addAlert } = useAlertPopupContext();
    const { t } = useTranslation();
    const deleteMutation = useDeleteApiToken();

    if (clicked) {
        return (
            <Card>
                <CardHeader
                    icon={faTrash}
                    label={
                        <Trans
                            i18nKey={'html.label.managePage.deleteToken.header'}
                            values={{ identifier }}
                        />
                    }
                />
                <Card.Body>
                    <p className={'mt-3'}>
                        <Trans
                            i18nKey='html.label.managePage.deleteToken.confirmDescription'
                            values={{ identifier }}
                        />
                    </p>

                    <ActionButton
                        className={'mt-2'}
                        onClick={() => {
                            deleteMutation.mutate(apiToken.token.token, {
                                onSuccess: () => {
                                    setClicked(false);
                                    addAlert({
                                        timeout: 7500,
                                        color: 'success',
                                        content: (
                                            <>
                                                <Fa icon={faCheck} />{' '}
                                                <Trans
                                                    i18nKey={
                                                        'html.label.managePage.alert.tokenDeleteSuccess'
                                                    }
                                                    values={{ identifier }}
                                                />
                                            </>
                                        ),
                                    });
                                },
                                onError: (error) => {
                                    addAlert({
                                        timeout: 15000,
                                        color: 'danger',
                                        content: (
                                            <>
                                                <Fa
                                                    icon={faExclamationTriangle}
                                                />{' '}
                                                <Trans
                                                    i18nKey={
                                                        'html.label.managePage.alert.tokenDeleteFail'
                                                    }
                                                    values={{
                                                        error: error?.message,
                                                    }}
                                                />
                                            </>
                                        ),
                                    });
                                },
                            });
                        }}>
                        <Fa icon={faTrash} />{' '}
                        <Trans
                            i18nKey='html.label.managePage.deleteToken.confirm'
                            values={{ identifier }}
                        />
                    </ActionButton>
                    <OutlineButton
                        className={'mt-2'}
                        style={{ marginLeft: '0.5rem' }}
                        onClick={() => {
                            setClicked(false);
                        }}>
                        <Fa icon={faRotateLeft} />{' '}
                        {t('command.confirmation.deny')}
                    </OutlineButton>
                </Card.Body>
            </Card>
        );
    }

    return (
        <OutlineButton
            className={'float-end'}
            onClick={() => {
                setClicked(true);
            }}>
            <Fa icon={faTrash} />{' '}
            <Trans
                i18nKey={'html.label.managePage.deleteToken.header'}
                values={{ identifier }}
            />
        </OutlineButton>
    );
};

const DiscardButton = () => {
    const { t } = useTranslation();
    const { dirty, requestDiscard } = useConfigurationStorageContext();

    return (
        <>
            {dirty && (
                <SecondaryActionButton
                    className={'float-end'}
                    style={{ margin: '-0.5rem', marginRight: '0.5rem' }}
                    onClick={requestDiscard}>
                    <Fa icon={faTrash} />{' '}
                    {t('html.label.managePage.changes.discard')}
                </SecondaryActionButton>
            )}
        </>
    );
};

const AddTokenBody = ({ tokens }: { tokens: ApiTokenWithPermissions[] }) => {
    const { t } = useTranslation();
    const [value, setValue] = useState('');
    const { addAlert } = useAlertPopupContext();
    const createMutation = useCreateApiToken();

    const isInvalid = (newValue: string): boolean =>
        !!(
            newValue &&
            (newValue.length > 100 ||
                tokens.find((token) => token.token.identifier === newValue))
        );
    const invalid = isInvalid(value);

    return (
        <>
            <h3>
                <FontAwesomeIcon icon={faPlus} />{' '}
                {t('html.label.managePage.addToken.header')}
            </h3>
            <TextInput
                icon={faKey}
                isInvalid={isInvalid}
                invalidFeedback={t(
                    'html.label.managePage.addToken.invalidName',
                )}
                placeholder={t('html.label.managePage.addToken.name')}
                value={value}
                setValue={(newValue) =>
                    setValue(newValue.toLowerCase().replace(' ', '_'))
                }
            />
            <ActionButton
                className={'mt-2'}
                disabled={invalid || !value || value.length === 0}
                onClick={() => {
                    createMutation.mutate(
                        { identifier: value },
                        {
                            onSuccess: () => {
                                addAlert({
                                    timeout: 7500,
                                    color: 'success',
                                    content: (
                                        <>
                                            <Fa icon={faCheck} />{' '}
                                            <Trans
                                                i18nKey={
                                                    'html.label.managePage.alert.tokenAddSuccess'
                                                }
                                                values={{
                                                    identifier: value,
                                                }}
                                            />
                                        </>
                                    ),
                                });
                            },
                            onError: (error) => {
                                addAlert({
                                    timeout: 15000,
                                    color: 'danger',
                                    content: (
                                        <>
                                            <Fa icon={faExclamationTriangle} />{' '}
                                            <Trans
                                                i18nKey={
                                                    'html.label.managePage.alert.tokenAddFail'
                                                }
                                                values={{
                                                    error: error?.message,
                                                }}
                                            />
                                        </>
                                    ),
                                });
                            },
                        },
                    );
                }}>
                <Fa icon={faKey} /> {t('html.label.managePage.addToken.header')}
            </ActionButton>
        </>
    );
};

const TokensCard = ({ tokens }: { tokens: ApiTokenWithPermissions[] }) => {
    const { t } = useTranslation();
    const { setHelpModalTopic } = useNavigation();
    const openHelp = useCallback(
        () => setHelpModalTopic('api-tokens'),
        [setHelpModalTopic],
    );

    const slices = tokens.map((apiToken) => {
        return {
            body: (
                <TokenEditContextProvider apiToken={apiToken}>
                    <TokenBody apiToken={apiToken} />
                </TokenEditContextProvider>
            ),
            header: <TokensHeader label={apiToken.token.identifier} />,
            color: 'light-green',
            outline: false,
        };
    });

    slices.push({
        body: <AddTokenBody tokens={tokens} />,
        header: (
            <TokensHeader
                label={t('html.label.managePage.addToken.header')}
                icon={faPlus}
            />
        ),
        color: 'light-green',
        outline: false,
    });

    return (
        <Card id='api-tokens-card'>
            <CardHeader
                icon={faKey}
                color='theme'
                label={t('html.label.managePage.tokenHeader')}>
                <button
                    className={'btn bg-transparent col-help-icon'}
                    style={{ margin: '-0.5rem' }}
                    onClick={openHelp}>
                    <Fa icon={faQuestionCircle} />
                </button>
                <UnsavedChangesText />
                <SaveButton />
                <DiscardButton />
            </CardHeader>
            <Card.Body>
                <SideNavTabs
                    slices={slices}
                    open={true}
                    alignment={undefined}></SideNavTabs>
            </Card.Body>
        </Card>
    );
};

const ApiTokenView = () => {
    const { hasPermission } = useAuth();
    const seeManageTokens = hasPermission('manage.tokens');

    if (!seeManageTokens) return null;

    return (
        <LoadIn>
            <ConfigurationStorageContextProvider>
                <DropdownStatusContextProvider>
                    <Row>
                        <Col md={12}>
                            <Suspense fallback={<CardLoader />}>
                                <TokensLoader />
                            </Suspense>
                        </Col>
                    </Row>
                </DropdownStatusContextProvider>
            </ConfigurationStorageContextProvider>
        </LoadIn>
    );
};

const TokensLoader = () => {
    const tokens = useApiTokens();

    if (!tokens) return <CardLoader />;

    return <TokensCard tokens={tokens} />;
};

export default ApiTokenView;
