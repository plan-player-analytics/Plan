import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {fetchAvailablePermissions, fetchGroupPermissions} from "../../service/manageService";

const GroupEditContext = createContext({});

const createPermissionTree = (permissions) => {
    if (!permissions.length) {
        return {children: []};
    }
    permissions.sort();

    const isParent = (permission, parentCandidate) => {
        if (parentCandidate === permission) return false;
        const substitute = permission.replace(parentCandidate, '')[0];
        // Last character is . so it is a sub-permission of the parent, eg. page.player, page.player.thing -> .thing
        return substitute.length && substitute[0] === '.';
    }

    const idTree = [];
    for (const permission of permissions) {
        const parentCandidates = permissions.filter(parentCandidate => isParent(permission, parentCandidate));
        parentCandidates.sort();
        parentCandidates.reverse();
        const parent = parentCandidates.length ? parentCandidates[0] : null;
        const parentIndex = permissions.indexOf(parent);
        idTree.push({permission, parentIndex, children: []});
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

    const [permissionTree, setPermissionTree] = useState({children: []});
    useEffect(() => {
        setPermissionTree(createPermissionTree(allPermissions))
    }, [allPermissions]);

    const isChecked = useCallback((permission) => {
        const parent = dfs(permissionTree, permission)?.parent;
        return parent && isChecked(parent.permission) || permissions.includes(permission);
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

    const dfs = useCallback((root, permission) => {
        if (root.permission === permission) return root;

        for (const child of root.children) {
            const found = dfs(child, permission);
            if (found) return found;
        }
        return null;
    }, [allPermissions]);

    const removePermissions = (toRemoveArray) => {
        const filter = permissions.filter(p => !toRemoveArray.includes(p));
        console.log("Remove permissions", toRemoveArray, "Result", filter)
        setPermissions(filter);
    }

    const addPermissions = (toAddArray) => {
        console.log("Add permissions", toAddArray)
        setPermissions([...permissions, ...toAddArray]);
    }

    const togglePermission = useCallback((permission) => {
        const included = permissions.includes(permission);
        const node = dfs(permissionTree, permission);
        const parent = node.parent;
        // TODO This needs to check up the whole tree until it finds the checked parent and add every necessary permission
        const parentIncluded = parent && permissions.includes(parent.permission);
        console.log("Toggle", permission, included, node, parentIncluded, parent);

        if (included) {
            removePermissions([permission]);
        } else if (parentIncluded) {
            removePermissions([parent.permission, ...node.children.map(c => c.permission)]);
            addPermissions(parent.children.map(c => c.permission).filter(p => p !== permission));
        } else {
            removePermissions(node.children.map(c => c.permission));
            addPermissions([permission]);
        }
    }, [permissions, setPermissions, isIndeterminate]);

    console.log(permissions);
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