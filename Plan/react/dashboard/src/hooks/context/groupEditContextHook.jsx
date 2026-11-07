import React, {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
} from 'react';
import {
    fetchGroupPermissions,
    saveGroupPermissions,
} from '../../service/manageService';
import { useConfigurationStorageContext } from './configurationStorageContextHook.tsx';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import {
    faCheck,
    faExclamationTriangle,
} from '@fortawesome/free-solid-svg-icons';
import { useAlertPopupContext } from './alertPopupContext.tsx';
import { Trans, useTranslation } from 'react-i18next';
import { useAuth } from '../authenticationHook.tsx';
import { usePermissionTree } from '../../dataHooks/usePermissionTreeContext';

const GroupEditContext = createContext({});

export const GroupEditContextProvider = ({ groupName, children }) => {
    const { t } = useTranslation();
    const [changed, setChanged] = useState(false);
    const { markDirty, saveRequested, discardRequested } =
        useConfigurationStorageContext();
    const [lastSave, setLastSave] = useState(Date.now());
    const [lastDiscard, setLastDiscard] = useState(Date.now());
    const { addAlert } = useAlertPopupContext();
    const { updateLoginDetails } = useAuth();

    const [permissions, setPermissions] = useState([]);
    const loadPermissions = useCallback(() => {
        return fetchGroupPermissions(groupName).then((response) => {
            setPermissions(response?.data?.permissions || []);
        });
    }, [groupName]);

    useEffect(() => {
        loadPermissions();
    }, [loadPermissions]);

    const {
        permissionTree,
        isChecked,
        isIndeterminate,
        togglePermission: togglePermissionFn,
    } = usePermissionTree(permissions);

    const togglePermission = useCallback(
        (permission) => {
            markDirty(true);
            setChanged(true);
            const newPermissions = togglePermissionFn(permission);
            setPermissions(newPermissions);
        },
        [markDirty, setChanged, togglePermissionFn],
    );

    const saveChanges = useCallback(async () => {
        if (saveRequested > lastSave && changed) {
            const { error } = await saveGroupPermissions(
                groupName,
                permissions,
            );
            if (error) {
                addAlert({
                    timeout: 15000,
                    color: 'danger',
                    content: (
                        <>
                            <Fa icon={faExclamationTriangle} />{' '}
                            <Trans
                                i18nKey={'html.label.managePage.alert.saveFail'}
                                values={{ error: error?.message }}
                            />
                        </>
                    ),
                });
            } else {
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
            }
            setLastSave(Date.now());
            updateLoginDetails();
        }
    }, [
        lastSave,
        changed,
        setChanged,
        saveRequested,
        setLastSave,
        permissions,
        groupName,
        addAlert,
        t,
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

    const sharedState = useMemo(() => {
        return {
            changed,
            permissionTree,
            permissions,
            isChecked,
            isIndeterminate,
            togglePermission,
            groupName,
        };
    }, [
        changed,
        permissionTree,
        permissions,
        isChecked,
        isIndeterminate,
        togglePermission,
        groupName,
    ]);
    return (
        <GroupEditContext.Provider value={sharedState}>
            {children}
        </GroupEditContext.Provider>
    );
};

export const useGroupEditContext = () => {
    return useContext(GroupEditContext);
};
