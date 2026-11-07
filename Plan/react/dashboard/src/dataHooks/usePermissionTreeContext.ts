import { useCallback, useMemo } from 'react';
import { usePermissions } from './usePermissions';
import { PermissionNode } from './model/permissions/PermissionNode';

export function createPermissionTree(
    allPermissions: string[],
    toggledPermissions: string[],
): PermissionNode {
    if (!allPermissions || allPermissions.length === 0) {
        return {
            permission: '',
            parentIndex: -1,
            children: [],
            toggled: false,
        };
    }
    const sorted = [...allPermissions].sort();

    const isParent = (permission: string, parentCandidate: string): boolean => {
        if (parentCandidate === permission) return false;
        const substitute = permission.replace(parentCandidate, '').charAt(0);
        return substitute.length > 0 && substitute[0] === '.';
    };

    const idTree: PermissionNode[] = [];
    for (const permission of sorted) {
        const parentCandidates = sorted.filter((parentCandidate) =>
            isParent(permission, parentCandidate),
        );
        parentCandidates.sort();
        parentCandidates.reverse();
        const parent = parentCandidates.length > 0 ? parentCandidates[0] : null;
        const parentIndex = sorted.indexOf(parent ?? '');
        idTree.push({
            permission,
            parentIndex: parent ? parentIndex : -1,
            children: [],
            toggled: toggledPermissions.includes(permission),
            parent: null,
        });
    }
    for (const permission of idTree) {
        const parentIndex = permission.parentIndex;
        if (parentIndex !== -1) {
            const parentNode = idTree[parentIndex];
            permission.parent = parentNode;
            parentNode.children.push(permission);
        }
    }
    const rootNodes = idTree.filter((node) => node.parentIndex === -1);
    return {
        permission: '',
        parentIndex: -1,
        children: rootNodes,
        toggled: false,
    };
}

function dfs(root: PermissionNode, permission: string): PermissionNode | null {
    if (root.permission === permission) return root;

    for (const child of root.children) {
        const found = dfs(child, permission);
        if (found) return found;
    }
    return null;
}

function isNodeChecked(
    node: PermissionNode | null | undefined,
): PermissionNode | false {
    if (!node) return false;
    const parentNode = node.parent;
    return node.toggled ? node : isNodeChecked(parentNode);
}

function isNodeIndeterminate(node: PermissionNode): boolean {
    if (!node || node.toggled) return false;

    const childNodes = node.children;
    const toggledChildNodes = node.children.filter((child) => child.toggled);
    const indeterminate = toggledChildNodes.length !== 0;
    if (indeterminate) return true;

    for (const child of childNodes) {
        if (isNodeIndeterminate(child)) {
            return true;
        }
    }
    return false;
}

function getAllChildren(root: PermissionNode): PermissionNode[] {
    let children: PermissionNode[] = [...root.children];
    root.children.forEach((child) => children.push(...getAllChildren(child)));
    return children;
}

export interface UsePermissionTreeResult {
    permissionTree: PermissionNode;
    isChecked: (permission: string) => boolean;
    isIndeterminate: (permission: string) => boolean;
    togglePermission: (permission: string) => string[];
}

export function usePermissionTree(
    toggledPermissions: string[],
): UsePermissionTreeResult {
    const allPermissions = usePermissions();

    const permissionTree = useMemo(
        () => createPermissionTree(allPermissions, toggledPermissions),
        [allPermissions, toggledPermissions],
    );

    const isChecked = useCallback(
        (permission: string) => {
            const node = dfs(permissionTree, permission);
            return !!isNodeChecked(node);
        },
        [permissionTree],
    );

    const isIndeterminate = useCallback(
        (permission: string) => {
            const node = dfs(permissionTree, permission);
            if (!node) return false;
            return isNodeIndeterminate(node);
        },
        [permissionTree],
    );

    const togglePermission = useCallback(
        (permission: string) => {
            const node = dfs(permissionTree, permission);
            if (!node) return toggledPermissions;

            const checked = node.toggled;
            if (checked) {
                return toggledPermissions.filter((p) => p !== permission);
            } else {
                const checkedNode = isNodeChecked(node);
                if (checkedNode) {
                    const nodesToAdd: PermissionNode[] = [];
                    const queue: (PermissionNode | null | undefined)[] = [
                        node.parent,
                    ];
                    let foundAll = false;
                    while (!foundAll) {
                        const next = queue.pop();
                        if (!next) continue;
                        const otherChildren = next.children.filter(
                            (child) => child.permission !== permission,
                        );
                        nodesToAdd.push(...otherChildren);
                        if (next.permission === checkedNode.permission)
                            foundAll = true;

                        if (!foundAll) queue.push(next.parent);
                    }

                    const nodesToRemove: PermissionNode[] = [];
                    let next: PermissionNode | null | undefined = node;
                    while (next?.parent != null) {
                        nodesToRemove.push(next.parent);
                        next = next.parent;
                    }

                    const toAdd = nodesToAdd.map((child) => child.permission);
                    const toRemove = nodesToRemove.map(
                        (child) => child.permission,
                    );
                    const result = [...toggledPermissions, ...toAdd].filter(
                        (p) => !toRemove.includes(p),
                    );
                    return result;
                } else {
                    const toRemove = getAllChildren(node).map(
                        (child) => child.permission,
                    );
                    return [...toggledPermissions, permission].filter(
                        (p) => !toRemove.includes(p),
                    );
                }
            }
        },
        [permissionTree, toggledPermissions, getAllChildren, isNodeChecked],
    );

    return useMemo(
        () => ({
            permissionTree,
            isChecked,
            isIndeterminate,
            togglePermission,
        }),
        [permissionTree, isChecked, isIndeterminate, togglePermission],
    );
}
