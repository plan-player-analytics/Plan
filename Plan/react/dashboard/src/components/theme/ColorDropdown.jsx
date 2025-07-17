import React, {useEffect, useRef, useState} from 'react';
import {useTranslation} from "react-i18next";
import {Dropdown, Form} from "react-bootstrap";
import {FontAwesomeIcon as Fa, FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle, faTimes} from "@fortawesome/free-solid-svg-icons";
import {cssVariableToName, nameToContrastCssVariable, nameToCssVariable} from "../../util/colors.js";

const useMenuPlacement = (isOpen) => {
    const selectedItemRef = useRef(null);
    const dropdownMenuRef = useRef(null);
    useEffect(() => {
        if (isOpen && selectedItemRef.current && dropdownMenuRef.current) {
            setTimeout(() => {
                const menuElement = dropdownMenuRef.current;
                const selectedElement = selectedItemRef.current;

                // Check if the menu is positioned at the top using data-popper-placement
                const isDropup = menuElement.getAttribute('data-popper-placement') === 'top-start';

                if (isDropup) {
                    // For dropup, position the selected item at the bottom:
                    // Calculate how far from the bottom the item should be
                    const itemHeight = selectedElement.offsetHeight;
                    const menuHeight = menuElement.clientHeight;
                    const itemOffset = selectedElement.offsetTop;

                    // Set scroll position to show the item at the bottom
                    menuElement.scrollTop = itemOffset - menuHeight + itemHeight;
                } else {
                    // For dropdown, position at the top as before
                    menuElement.scrollTop = selectedElement.offsetTop;
                }
            }, 0);
        }
    }, [isOpen]);
    return {selectedItemRef, dropdownMenuRef};
}

const ColorDropdown = ({
                           id,
                           colors,
                           value,
                           onChange,
                           onHoverChange,
                           label,
                           onRemoveOverride = null,
                           marginLeft = 0
                       }) => {
    const {t} = useTranslation();
    // Extract name from CSS variable or use first color as default
    const selectedName = cssVariableToName(value) || Object.keys(colors)[0];
    const isTextColor = selectedName.includes('text') || label.includes('Text');
    const cssColor = nameToCssVariable(selectedName);
    const contrastColor = nameToContrastCssVariable(selectedName);
    const [isOpen, setIsOpen] = useState(false);

    const missing = !colors[selectedName];

    const {selectedItemRef, dropdownMenuRef} = useMenuPlacement(isOpen);

    return (
        <tr id={id} onMouseOver={() => onHoverChange(id, 'enter')} onMouseOut={() => onHoverChange(id, 'exit')}>
            <td style={{paddingBottom: '8px', paddingRight: '16px', whiteSpace: 'nowrap'}}>
                <span style={{marginLeft}}>{label}</span>
            </td>
            <td style={{paddingBottom: '8px', width: '100%'}}>
                <Form.Group className="d-flex align-items-stretch">
                    <div style={{width: '300px', height: '100%'}}>
                        <Dropdown show={isOpen} onToggle={setIsOpen}>
                            <Dropdown.Toggle
                                style={{
                                    width: '100%',
                                    height: '100%',
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "center",
                                    backgroundColor: isTextColor ? contrastColor : cssColor,
                                    color: isTextColor ? cssColor : contrastColor
                                }}
                                variant=""
                            >
                                {missing ? <><FontAwesomeIcon
                                        icon={faExclamationTriangle}/> {t('html.label.themeEditor.missing')}</>
                                    : selectedName}
                            </Dropdown.Toggle>

                            <Dropdown.Menu
                                style={{width: '100%', padding: '0', maxHeight: '300px', overflowY: 'auto'}}
                                ref={dropdownMenuRef}
                            >
                                {Object.keys(colors).map(name => {
                                    if (name === '-') return <hr key={id + '-' + name}/>;
                                    const isItemTextColor = name.includes('text') || label.includes('Text');
                                    const isSelected = name === selectedName;
                                    return (
                                        <Dropdown.Item
                                            key={id + '-' + name}
                                            onClick={() => {
                                                onChange?.(`var(--color-${name})`);
                                                setIsOpen(false);
                                            }}
                                            style={{
                                                backgroundColor: isItemTextColor ? 'transparent' : `var(--color-${name})`,
                                                color: isItemTextColor ? `var(--color-${name})` : `var(--contrast-color-${name})`
                                            }}
                                            ref={isSelected ? selectedItemRef : null}
                                        >
                                            {name}
                                        </Dropdown.Item>
                                    );
                                })}
                            </Dropdown.Menu>
                        </Dropdown>
                    </div>
                    {onRemoveOverride && (
                        <button
                            className="d-flex align-items-center px-1 col-red"
                            onClick={onRemoveOverride}
                            title={t('html.label.themeEditor.removeOverride')}
                        >
                            <Fa icon={faTimes}/>
                        </button>
                    )}
                </Form.Group>
            </td>
        </tr>
    );
};

export default ColorDropdown