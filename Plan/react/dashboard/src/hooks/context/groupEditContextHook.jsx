import React, {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {fetchAvailablePermissions, fetchGroupPermissions, saveGroupPermissions} from "../../service/manageService";
import {useConfigurationStorageContext} from "./configurationStorageContextHook";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCheck, faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useAlertPopupContext} from "./alertPopupContext.tsx";
import {Trans, useTranslation} from "react-i18next";
import {useAuth} from "../authenticationHook.tsx";

const GroupEditContext = createContext({});

const createPermissionTree = (allPermissions, toggledPermissions) => {
    if (!allPermissions.length) {
        return {children: []};
    }
    allPermissions.sort();

    const isParent = (permission, parentCandidate) => {
        if (parentCandidate === permission) return false;
        const substitute = permission.replace(parentCandidate, '')[0];
        // Last character is . so it is a sub-permission of the parent, eg. page.player, page.player.thing -> .thing
        return substitute.length && substitute[0] === '.';
    }

    const idTree = [];
    for (const permission of allPermissions) {
        const parentCandidates = allPermissions.filter(parentCandidate => isParent(permission, parentCandidate));
        parentCandidates.sort();
        parentCandidates.reverse();
        const parent = parentCandidates.length ? parentCandidates[0] : null;
        const parentIndex = allPermissions.indexOf(parent);
        idTree.push({permission, parentIndex, children: [], toggled: toggledPermissions.includes(permission)});
    }
    for (const permission of idTree) {
        const parentIndex = permission.parentIndex;
        if (parentIndex !== -1) {
            permission.parent = idTree[parentIndex];
            idTree[parentIndex].children.push(permission);
        }
    }
    const rootNodes = idTree.filter(node => node.parentIndex === -1);
    return {children: rootNodes};
}

export const GroupEditContextProvider = ({groupName, children}) => {
    const {t} = useTranslation();
    const [changed, setChanged] = useState(false);
    const {markDirty, saveRequested, discardRequested} = useConfigurationStorageContext();
    const [lastSave, setLastSave] = useState(Date.now());
    const [lastDiscard, setLastDiscard] = useState(Date.now());
    const {addAlert} = useAlertPopupContext();
    const {updateLoginDetails} = useAuth();

    const [allPermissions, setAllPermissions] = useState([]);
    useEffect(() => {
        // TODO Make this only happen once when opening groups page
        fetchAvailablePermissions().then(response => {
            setAllPermissions(response?.data?.permissions);
        });
    }, []);

    const [permissions, setPermissions] = useState([]);
    const loadPermissions = useCallback(() => {
        return fetchGroupPermissions(groupName).then(response => {
            setPermissions(response?.data?.permissions);
            setLastDiscard(Date.now());
            setChanged(false);
        });
    }, [groupName, setChanged, setPermissions, setLastDiscard]);
    useEffect(() => {
        loadPermissions()
    }, [loadPermissions]);

    const [permissionTree, setPermissionTree] = useState({children: []});
    useEffect(() => {
        setPermissionTree(createPermissionTree(allPermissions, permissions))
    }, [allPermissions, permissions]);

    const dfs = useCallback((root, permission) => {
        if (root.permission === permission) return root;

        for (const child of root.children) {
            const found = dfs(child, permission);
            if (found) return found;
        }
        return null;
    }, []);

    const isNodeChecked = useCallback((node) => {
        if (!node) return false;
        const parentNode = node?.parent;
        return node?.toggled ? node : isNodeChecked(parentNode)
    }, [])
    const isChecked = useCallback((permission) => {
        const node = dfs(permissionTree, permission);
        return isNodeChecked(node);
    }, [permissionTree, isNodeChecked, dfs]);

    const isNodeIndeterminate = useCallback((node) => {
        // Indeterminate if:
        // permission node itself is not toggled,
        // but some of its children are toggled or indeterminate.
        if (!node || node.toggled) return false;

        const childNodes = node.children;
        const toggledChildNodes = node.children.filter(child => child.toggled);
        const indeterminate = toggledChildNodes.length !== 0;
        if (indeterminate) return true;

        for (const child of childNodes) {
            if (isNodeIndeterminate(child)) {
                return true;
            }
        }
        return false;
    }, [])
    const isIndeterminate = useCallback((permission) => {
        const node = dfs(permissionTree, permission);
        return isNodeIndeterminate(node);
    }, [permissionTree, dfs, isNodeIndeterminate]);

    const removePermissions = useCallback((toRemoveArray) => {
        const result = permissions.filter(p => !toRemoveArray.includes(p));
        setPermissions(result);
    }, [setPermissions, permissions]);

    const modifyPermissions = useCallback((toAddArray, toRemoveArray) => {
        const result = [...permissions, ...toAddArray].filter(p => !toRemoveArray.includes(p));
        setPermissions(result);
    }, [setPermissions, permissions]);

    const getAllChildren = useCallback((root) => {
        let children = [...root.children];
        root.children.forEach(child => children.push(...getAllChildren(child)))
        return children;
    }, [])

    const togglePermission = useCallback((permission) => {
        markDirty(true);
        setChanged(true);
        const node = dfs(permissionTree, permission);

        const checked = node.toggled;
        if (checked) {
            removePermissions([permission]);
        } else {
            // Lookup parent that is checked
            const checkedNode = isNodeChecked(node);
            if (checkedNode) {
                // We need to find the nodes that are currently checked by the parent node
                // This way only the node that was clicked gets unchecked
                const nodesToAdd = [];
                const queue = [node.parent];
                let foundAll = false;
                while (!foundAll) {
                    const next = queue.pop();
                    if (!next) continue;
                    const otherChildren = next.children.filter(child => child.permission !== permission);
                    nodesToAdd.push(...otherChildren);
                    if (next.permission === checkedNode.permission) foundAll = true;

                    if (!foundAll) queue.push(next.parent);
                }

                // Then we need to remove the nodes that are no longer all checked
                // This way all the parents of the node that was clicked are unchecked
                const nodesToRemove = [];
                let next = node;
                while (next.parent != null) {
                    nodesToRemove.push(next.parent);
                    next = next.parent;
                }

                modifyPermissions(
                    nodesToAdd.map(child => child.permission),
                    nodesToRemove.map(child => child.permission)
                );
            } else {
                // Is not checked, add the permission and remove all child permission from list (they are checked)
                modifyPermissions(
                    [permission],
                    getAllChildren(node).map(child => child.permission)
                );
            }
        }
    }, [markDirty, setChanged, permissionTree, dfs, getAllChildren, isNodeChecked, modifyPermissions, removePermissions]);

    const saveChanges = useCallback(async () => {
        if (saveRequested > lastSave && changed) {
            const {error} = await saveGroupPermissions(groupName, permissions);
            if (error) {
                addAlert({
                    timeout: 15000,
                    color: "danger",
                    content: <>
                        <Fa icon={faExclamationTriangle}/>
                        {" "}
                        <Trans i18nKey={"html.label.managePage.alert.saveFail"} values={{error: error?.message}}/>
                    </>
                });
            } else {
                setChanged(false);
                addAlert({
                    timeout: 5000,
                    color: "success",
                    content: <><Fa icon={faCheck}/>{" "}{t('html.label.managePage.alert.saveSuccess')}</>
                });
            }
            setLastSave(Date.now());
            updateLoginDetails();
        }
    }, [lastSave, changed, setChanged, saveRequested, setLastSave, permissions, groupName, addAlert, t]);

    useEffect(() => {
        saveChanges();
    }, [saveChanges]);

    useEffect(() => {
        if (discardRequested > lastDiscard) {
            loadPermissions();
        }
    }, [lastDiscard, discardRequested, loadPermissions])

    const sharedState = useMemo(() => {
        return {
            changed,
            permissionTree,
            permissions,
            isChecked,
            isIndeterminate,
            togglePermission,
            groupName
        }
    }, [
        changed,
        permissionTree,
        permissions,
        isChecked,
        isIndeterminate,
        togglePermission,
        groupName
    ])
    return (<GroupEditContext.Provider value={sharedState}>
            {children}
        </GroupEditContext.Provider>
    )
}

export const useGroupEditContext = () => {
    return useContext(GroupEditContext);
}