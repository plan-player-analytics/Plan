import React from 'react';
import { PermissionDropdown } from './PermissionDropdown';
import { PermissionNode } from '../../dataHooks/model/permissions/PermissionNode';

type Props = {
    nodes: PermissionNode[];
    isIndeterminate: (permission: string) => boolean;
    isChecked: (permission: string) => boolean;
    togglePermission: (permission: string) => void;
};

export const PermissionTree = ({
    nodes,
    isIndeterminate,
    isChecked,
    togglePermission,
}: Props) => {
    if (!nodes.length) {
        return <></>;
    }
    return (
        <>
            {nodes.map((node) => (
                <section key={node.permission + '-1234'}>
                    <PermissionDropdown
                        permission={node.permission}
                        root={node.parentIndex === -1}
                        indeterminate={isIndeterminate(node.permission)}
                        checked={isChecked(node.permission)}
                        togglePermission={togglePermission}
                        childNodes={node.children}>
                        <PermissionTree
                            nodes={node.children}
                            togglePermission={togglePermission}
                            isChecked={isChecked}
                            isIndeterminate={isIndeterminate}
                        />
                    </PermissionDropdown>
                </section>
            ))}
        </>
    );
};
