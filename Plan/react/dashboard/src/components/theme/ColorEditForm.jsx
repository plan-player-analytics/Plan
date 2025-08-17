import React, {forwardRef, useEffect, useRef} from 'react';
import {Col, InputGroup, Row} from "react-bootstrap";
import {useColorEditContext} from "../../hooks/context/colorEditContextHook.jsx";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheck, faExclamationTriangle, faPalette, faPlus, faTrash} from "@fortawesome/free-solid-svg-icons";
import ActionButton from "../input/button/ActionButton.jsx";
import DangerButton from "../input/button/DangerButton.jsx";
import SecondaryActionButton from "../input/button/SecondaryActionButton.jsx";
import Chrome from "@uiw/react-color-chrome";
import {GithubPlacement} from '@uiw/react-color-github';
import Wheel from "@uiw/react-color-wheel";
import {getColorArrayConverter, getColorConverter, getContrastColor} from "../../util/Color.js";


const ColorInput = forwardRef(({color, contrastColor, invalid, onChange}, ref) => {
    const onChangeText = event => {
        onChange(event.target.value);
    }
    const converter = getColorConverter(color);

    const onSelectorChange = event => {
        if (converter) {
            const opacity = converter.toRgbaArray()[3];
            if (opacity < 1) {
                const rgba = getColorConverter(event.hex).toRgbaArray();
                rgba[3] = opacity;
                onChange(getColorArrayConverter(rgba, 'rgba').toRgbaString());
                return;
            }
        }
        onChange(event.hex);
    }


    return (
        <React.Fragment>
            <input ref={ref} type="text" className={"form-control " + (invalid ? 'is-invalid' : '')}
                   style={{background: color, color: contrastColor}}
                   id={'color-edit-id'}
                   value={color}
                   aria-invalid={invalid}
                   onChange={onChangeText}
            />
            <Row className={"color-selector p-0"}
                 style={{
                     padding: "0.5rem 0rem",
                     paddingTop: "0",
                     backgroundColor: "var(--color-cards-background)",
                     border: "1px solid var(--color-cards-border)",
                     position: 'absolute',
                     top: "2.5rem",
                     right: "0.8rem"
                 }}>
                <Col className={"p-0"}>
                    <Chrome
                        color={converter ? converter.toHex() : color}
                        style={{width: 140,}}
                        placement={GithubPlacement.Right}
                        showEyeDropper={false}
                        showColorPreview={false}
                        showAlpha={false}
                        showHue={true}
                        showWheel={false}
                        showInput={false}
                        onChange={onSelectorChange}
                    />
                </Col>
                <Col className={"p-0 m-2"}>
                    <Wheel
                        color={converter ? converter.toHex() : color}
                        style={{marginTop: "0.5rem"}}
                        onChange={onSelectorChange}
                    />
                </Col>
            </Row>
        </React.Fragment>
    );
});

const ColorEditForm = ({onFocus}) => {
    const {t} = useTranslation();
    const {
        alreadyExists,
        name,
        color,
        onNameChange,
        onColorChange,
        open,
        editNewColor,
        finishEdit,
        discardEdit,
        deleting,
        setDeleting
    } = useColorEditContext();

    const ref = useRef(null);

    useEffect(() => {
        if (open) {
            onFocus();
        }
        if (open && ref.current) {
            ref.current.focus();
        }
    }, [open]);

    if (deleting) {
        return (
            <Col className="mb-4">
                <ActionButton onClick={() => setDeleting(false)}>
                    <FontAwesomeIcon icon={faCheck}/>{t('html.label.themeEditor.finish')}
                </ActionButton>
            </Col>
        )
    }

    if (!open) {
        return (
            <Col className="mb-4">
                <ActionButton onClick={editNewColor}>
                    <FontAwesomeIcon icon={faPlus}/>{t('html.label.themeEditor.addColor')}
                </ActionButton>
                <DangerButton className={'ms-2'} onClick={() => setDeleting(true)}>
                    <FontAwesomeIcon icon={faTrash}/>{t('html.label.themeEditor.deleteColors')}
                </DangerButton>
            </Col>
        )
    }

    const contrastColor = !isColorInvalid() && getContrastColor(color) || "var(--color-forms-input-text)";

    function isNameInvalid() {
        return name.length > 100;
    }

    function isColorInvalid() {
        if (!color.length) {
            return true
        }

        const hexRegex = /^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/;
        const rgbRegex = /^rgba?\(\s*(\d{1,3}%?\s*,\s*){2}\d{1,3}%?(\s*,\s*(0|1|0?\.\d+))?\s*\)$/;
        const hslRegex = /^hsla?\(\s*(\d{1,3})(deg)?\s*,\s*(\d{1,3})%\s*,\s*(\d{1,3})%(?:\s*,\s*(0|1|0?\.\d+))?\s*\)$/i;
        const hsvRegex = /^hsva?\(\s*(\d{1,3})(deg)?\s*,\s*(\d{1,3})%\s*,\s*(\d{1,3})%(?:\s*,\s*(0|1|0?\.\d+))?\s*\)$/i;
        const linearGradientRegex = /^linear-gradient\(.*\)$/i;
        const radialGradientRegex = /^radial-gradient\(.*\)$/i;
        const colorMix = /^color-mix\(.*\)$/i;

        return !(hexRegex.test(color) || rgbRegex.test(color) || hslRegex.test(color) || hsvRegex.test(color) || linearGradientRegex.test(color) || radialGradientRegex.test(color)) || colorMix.test(color);
    }

    const isGradient = color.includes('gradient');

    return (
        <Row className={"mb-4 color-edit-form"}>
            <Col>
                <InputGroup>
                    <div className={"input-group-text"} style={{background: color, color: contrastColor}}>
                        <FontAwesomeIcon icon={faPalette}/>
                    </div>
                    <input type="text" className={"form-control " + (isNameInvalid() ? 'is-invalid' : '')}
                           id={'color-name-edit-id'}
                           value={name}
                           aria-invalid={isNameInvalid()}
                           placeholder={'new-color'}
                           onChange={event => onNameChange(event.target.value)}
                    />
                    <ColorInput ref={ref} color={color} contrastColor={contrastColor}
                                invalid={isColorInvalid()} onChange={onColorChange}/>
                </InputGroup>
                {alreadyExists && <span className="help-block" style={{color: "var(--color-warning)"}}><FontAwesomeIcon
                    icon={faExclamationTriangle}/> {t('html.label.themeEditor.alreadyExistsWarning')}</span>}
                {isGradient && <span className="help-block" style={{color: "var(--color-warning)"}}><FontAwesomeIcon
                    icon={faExclamationTriangle}/> {t('html.label.themeEditor.gradientWarning')}</span>}
            </Col>
            <Col>
                <ActionButton onClick={finishEdit} disabled={isNameInvalid() || isColorInvalid()}>
                    {t('html.label.managePage.changes.save')}
                </ActionButton>
                <SecondaryActionButton className={"ms-2"} onClick={discardEdit}>
                    {t('html.label.managePage.changes.discard')}
                </SecondaryActionButton>
            </Col>
        </Row>
    )
};

export default ColorEditForm