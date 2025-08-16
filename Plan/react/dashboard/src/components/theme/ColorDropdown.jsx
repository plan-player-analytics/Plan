import React, {useEffect, useRef, useState} from 'react';
import {Dropdown} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {cssVariableToName, nameToContrastCssVariable, nameToCssVariable} from "../../util/colors.js";

const useMenuPlacement = (isOpen) => {
    const selectedItemRef = useRef(null);
    const dropdownMenuRef = useRef(null);
    useEffect(() => {
        if (isOpen && selectedItemRef.current && dropdownMenuRef.current) {
            setTimeout(() => {
                const menuElement = dropdownMenuRef.current;
                const selectedElement = selectedItemRef.current;
                const isDropup = menuElement.getAttribute('data-popper-placement') === 'top-start';
                const itemHeight = selectedElement.offsetHeight;
                const menuHeight = menuElement.clientHeight;
                const itemOffset = selectedElement.offsetTop;
                if (isDropup) {
                    menuElement.scrollTop = itemOffset - menuHeight + itemHeight;
                } else {
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
                           label
                       }) => {
    const {t} = useTranslation();
    const selectedName = cssVariableToName(value) || Object.keys(colors)[0];
    const isTextColor = selectedName.includes('text') || label.includes('Text');
    const cssColor = nameToCssVariable(selectedName);
    const contrastColor = nameToContrastCssVariable(selectedName);
    const [isOpen, setIsOpen] = useState(false);
    const missing = !colors[selectedName];
    const {selectedItemRef, dropdownMenuRef} = useMenuPlacement(isOpen);

    return (
        <Dropdown show={isOpen} onToggle={setIsOpen} style={{width: '100%'}}>
            <Dropdown.Toggle
                style={{
                    width: '100%',
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    background: isTextColor ? contrastColor : cssColor,
                    color: isTextColor ? cssColor : contrastColor
                }}
                variant=""
            >
                {missing ? t('html.label.themeEditor.missing') : selectedName}
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
                            as="button"
                            onClick={() => {
                                onChange?.(`var(--color-${name})`);
                                setIsOpen(false);
                            }}
                            style={{
                                background: isItemTextColor ? 'transparent' : `var(--color-${name})`,
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
    );
};

export default ColorDropdown