import { useTranslation } from 'react-i18next';
import { useDropdownStatusContext } from '../../hooks/context/dropdownStatusContextHook';
import Checkbox from '../input/Checkbox';
import OpaqueText from '../layout/OpaqueText';
import React, { PropsWithChildren } from 'react';
import { PermissionNode } from '../../dataHooks/model/permissions/PermissionNode';

type Props = {
    permission: string;
    checked: boolean;
    indeterminate: boolean;
    togglePermission: (permission: string) => void;
    childNodes: PermissionNode[];
    root?: boolean;
} & PropsWithChildren;

export const PermissionDropdown = ({
    permission,
    checked,
    indeterminate,
    togglePermission,
    children,
    childNodes,
    root,
}: Props) => {
    const { t } = useTranslation();
    const { toggle, toggled } = useDropdownStatusContext();

    const translationKey =
        'html.manage.permission.description.' +
        permission?.split('.').join('_');
    const translated = t(translationKey);

    if (childNodes.length) {
        if (permission === undefined) {
            return <>{children}</>;
        } else {
            return (
                <details
                    open={!toggled.includes(permission)}
                    style={
                        root
                            ? { marginLeft: '0.5rem' }
                            : { marginLeft: '2.1rem' }
                    }>
                    <summary
                        onClick={(event) => {
                            if (
                                event.target instanceof HTMLElement &&
                                (event.target.tagName === 'INPUT' ||
                                    event.target.closest('label'))
                            ) {
                                return;
                            }
                            event.preventDefault();
                            toggle(permission);
                        }}>
                        <Checkbox
                            indeterminate={indeterminate}
                            checked={checked}
                            onChange={() => togglePermission(permission)}>
                            {permission}{' '}
                            {permission && translated !== translationKey && (
                                <OpaqueText inline>
                                    &middot; {translated}
                                </OpaqueText>
                            )}
                        </Checkbox>
                        <hr style={{ margin: 0 }} />
                    </summary>

                    {children}
                </details>
            );
        }
    } else {
        return (
            <li
                style={
                    root ? { marginLeft: '1.4rem' } : { marginLeft: '3rem' }
                }>
                <Checkbox
                    checked={checked}
                    onChange={() => togglePermission(permission)}
                />{' '}
                {permission}{' '}
                {permission && translated !== translationKey && (
                    <OpaqueText inline>&middot; {translated}</OpaqueText>
                )}
            </li>
        );
    }
};
