import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {fetchAvailablePermissions, fetchGroupPermissions} from "../../service/manageService";

const GroupEditContext = createContext({});

const createPermissionTree = (permissions) => {
    if (!permissions.length) {
        return {children: []};
    }
    permissions.sort();

    const idTree = [];
    for (const permission of permissions) {
        const parentCandidates = permissions.filter(p => p !== permission && permission.includes(p));
        parentCandidates.sort();
        parentCandidates.reverse();
        const parent = parentCandidates.length ? parentCandidates[0] : null;
        const parentIndex = permissions.indexOf(parent);
        idTree.push({permission, parentIndex, children: []});
    }
    for (const permission of idTree) {
        const parentIndex = permission.parentIndex;
        if (parentIndex !== -1) idTree[parentIndex].children.push(permission);
    }
    const rootNodes = idTree.filter(node => node.parentIndex === -1);
    return {children: rootNodes};
}

export const GroupEditContextProvider = ({groupName, children}) => {
    const [allPermissions, setAllPermissions] = useState([]);
    useEffect(() => {
        fetchAvailablePermissions().then(response => {
            setAllPermissions(response?.data?.permissions);
        });
    }, []);

    const [permissions, setPermissions] = useState([]);
    useEffect(() => {
        fetchGroupPermissions(groupName).then(response => {
            setPermissions(response?.data?.permissions);
        });
    }, [groupName]);

    const isChecked = useCallback((permission) => {
        return permissions.includes(permission);
    }, [permissions]);

    const isIndeterminate = useCallback((permission) => {
        // Indeterminate if:
        // Some child nodes have different value (XOR)
        // Child is indeterminate

        const childPermissions = allPermissions.filter(p => p.includes(permission) && p !== permission);
        const checkedChildren = childPermissions.filter(p => isChecked(p));
        const indeterminate = childPermissions.length !== checkedChildren.length && checkedChildren.length !== 0;
        const indeterminateChildren = childPermissions.filter(p => !isChecked(p) && isIndeterminate(p));
        return indeterminate || indeterminateChildren.length;
    }, [permissions, allPermissions, isChecked]);

    const togglePermission = useCallback((permission) => {
        if (permissions.includes(permission)) {
            setPermissions(permissions.filter(p => !p.includes(permission)));
        } else {
            setPermissions([...permissions, ...allPermissions.filter(p => p.includes(permission))]);
        }
    }, [permissions, setPermissions, isIndeterminate]);

    const [permissionTree, setPermissionTree] = useState({children: []});
    useEffect(() => {
        setPermissionTree(createPermissionTree(allPermissions))
    }, [allPermissions]);

    const sharedState = useMemo(() => {
        return {
            permissionTree,
            permissions,
            isChecked,
            isIndeterminate,
            togglePermission,
            groupName
        }
    }, [
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