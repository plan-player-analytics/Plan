import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";

const GroupEditContext = createContext({});

const createPermissionTree = (permissions) => {
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
    return idTree[0];
}

export const GroupEditContextProvider = ({groupName, children}) => {
    const [allPermissions] = useState([
        'page',
        'page.server',
        'page.server.overview',
        'page.server.onlineOverview'
    ]);

    const [permissions, setPermissions] = useState(['page.server.overview']);

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
        console.log(permission, childPermissions, checkedChildren, indeterminate, indeterminateChildren)
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