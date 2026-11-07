import React, {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
} from 'react';
import { useSaveApiToken } from '../../dataHooks/useSaveApiToken';
import { useConfigurationStorageContext } from './configurationStorageContextHook';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import {
    faCheck,
    faExclamationTriangle,
} from '@fortawesome/free-solid-svg-icons';
import { useAlertPopupContext } from './alertPopupContext';
import { Trans, useTranslation } from 'react-i18next';
import { useAuth } from '../authenticationHook';
import {
    usePermissionTree,
    UsePermissionTreeResult,
} from '../../dataHooks/usePermissionTreeContext';
import { ApiTokenWithPermissions } from '../../dataHooks/model/permissions/ApiToken';

type TokenEditContextState = {
    changed: boolean;
    permissions: string[];
    identifier: string;
    setIdentifier: (id: string) => void;
    apiToken: ApiTokenWithPermissions;
    togglePermission: (permission: string) => void;
} & Omit<UsePermissionTreeResult, 'togglePermission'>;

const TokenEditContext = createContext<TokenEditContextState>(
    {} as TokenEditContextState,
);

interface TokenEditContextProviderProps {
    apiToken: ApiTokenWithPermissions;
    children: React.ReactNode;
}

export const TokenEditContextProvider = ({
    apiToken,
    children,
}: TokenEditContextProviderProps) => {
    const { t } = useTranslation();
    const [changed, setChanged] = useState(false);
    const { markDirty, saveRequested, discardRequested } =
        useConfigurationStorageContext();
    const [lastSave, setLastSave] = useState(Date.now());
    const [lastDiscard, setLastDiscard] = useState(Date.now());
    const { addAlert } = useAlertPopupContext();
    const { updateLoginDetails } = useAuth();

    const saveMutation = useSaveApiToken();

    const [permissions, setPermissions] = useState<string[]>([]);
    const [identifier, setIdentifierState] = useState<string>('');
    const loadPermissions = useCallback(() => {
        setPermissions(apiToken.permissions || []);
        setIdentifierState(apiToken.token.identifier);
    }, [apiToken]);

    useEffect(() => {
        loadPermissions();
    }, [loadPermissions]);

    const setIdentifier = useCallback((id: string) => {
        markDirty();
        setChanged(true);
        setIdentifierState(id);
    }, [markDirty, setChanged]);

    const {
        permissionTree,
        isChecked,
        isIndeterminate,
        togglePermission: togglePermissionFn,
    } = usePermissionTree(permissions);

    const togglePermission = useCallback(
        (permission: string) => {
            markDirty();
            setChanged(true);
            const newPermissions = togglePermissionFn(permission);
            setPermissions(newPermissions);
        },
        [markDirty, setChanged, togglePermissionFn],
    );

    const saveChanges = useCallback(async () => {
        if (saveRequested > lastSave && changed) {
            try {
                await saveMutation.mutateAsync({
                    token: apiToken.token.token,
                    identifier,
                    permissions,
                });
                setChanged(false);
                addAlert({
                    timeout: 5000,
                    color: 'success',
                    content: (
                        <>
                            <Fa icon={faCheck} />{' '}
                            {t('html.label.managePage.alert.saveSuccess')}
                        </>
                    ),
                });
                setLastSave(Date.now());
                updateLoginDetails();
            } catch (e) {
                addAlert({
                    timeout: 15000,
                    color: 'danger',
                    content: (
                        <>
                            <Fa icon={faExclamationTriangle} />{' '}
                            <Trans
                                i18nKey={'html.label.managePage.alert.saveFail'}
                                values={{ error: e }}
                            />
                        </>
                    ),
                });
            }
        }
    }, [
        lastSave,
        changed,
        setChanged,
        saveRequested,
        setLastSave,
        permissions,
        apiToken,
        addAlert,
        t,
        saveMutation,
    ]);

    useEffect(() => {
        saveChanges();
    }, [saveChanges]);

    useEffect(() => {
        if (discardRequested > lastDiscard) {
            loadPermissions();
            setLastDiscard(Date.now());
            setChanged(false);
        }
    }, [lastDiscard, discardRequested, loadPermissions]);

    const sharedState = useMemo<TokenEditContextState>(() => {
        return {
            changed,
            permissionTree,
            permissions,
            identifier,
            setIdentifier,
            isChecked,
            isIndeterminate,
            togglePermission,
            apiToken,
        };
    }, [
        changed,
        permissionTree,
        permissions,
        identifier,
        setIdentifier,
        isChecked,
        isIndeterminate,
        togglePermission,
        apiToken,
    ]);
    return (
        <TokenEditContext.Provider value={sharedState}>
            {children}
        </TokenEditContext.Provider>
    );
};

export const useTokenEditContext = () => {
    return useContext(TokenEditContext);
};
