import React, {useEffect, useRef} from 'react';
import {Col, InputGroup, Row} from "react-bootstrap";
import {useColorEditContext} from "../../hooks/context/colorEditContextHook.jsx";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {getContrastColor} from "../../util/colors.js";
import {faCheck, faExclamationTriangle, faPalette, faPlus, faTrash} from "@fortawesome/free-solid-svg-icons";


const ColorEditForm = () => {
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
        if (open && ref.current) {
            ref.current.focus();
        }
    }, [open]);

    if (deleting) {
        return (
            <Col className="mb-4">
                <button className={"btn bg-plan"} onClick={() => setDeleting(false)}>
                    <FontAwesomeIcon icon={faCheck}/>{t('html.label.themeEditor.finish')}
                </button>
            </Col>
        )
    }

    if (!open) {
        return (
            <Col className="mb-4">
                <button className={"btn bg-theme"} onClick={editNewColor}>
                    <FontAwesomeIcon icon={faPlus}/>{t('html.label.themeEditor.addColor')}
                </button>
                <button className={"btn bg-red ms-2"} onClick={() => setDeleting(true)}>
                    <FontAwesomeIcon icon={faTrash}/>{t('html.label.themeEditor.deleteColors')}
                </button>
            </Col>
        )
    }

    const contrastColor = getContrastColor(color) || "var(--color-forms-input-text)";

    function isNameInvalid() {
        return false;
    }

    function isColorInvalid() {
        if (!color.length) {
            return true
        }

        const hexRegex = /^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/;
        const rgbRegex = /^rgba?\(\s*(\d{1,3}%?\s*,\s*){2}\d{1,3}%?(\s*,\s*(0|1|0?\.\d+))?\s*\)$/;
        const hslRegex = /^hsla?\(\s*(\d{1,3})(deg)?\s*,\s*(\d{1,3})%\s*,\s*(\d{1,3})%(?:\s*,\s*(0|1|0?\.\d+))?\s*\)$/i;
        const hsvRegex = /^hsva?\(\s*(\d{1,3})(deg)?\s*,\s*(\d{1,3})%\s*,\s*(\d{1,3})%(?:\s*,\s*(0|1|0?\.\d+))?\s*\)$/i;

        return !(hexRegex.test(color) || rgbRegex.test(color) || hslRegex.test(color) || hsvRegex.test(color));
    }

    return (
        <Row className={"mb-4"}>
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
                    <input ref={ref} type="text" className={"form-control " + (isColorInvalid() ? 'is-invalid' : '')}
                           style={{background: color, color: contrastColor}}
                           id={'color-edit-id'}
                           value={color}
                           aria-invalid={isColorInvalid()}
                           onChange={event => onColorChange(event.target.value)}
                    />
                </InputGroup>
                {alreadyExists && <span class="help-block" style={{color: "var(--color-warning)"}}><FontAwesomeIcon
                    icon={faExclamationTriangle}/> {t('html.label.themeEditor.alreadyExistsWarning')}</span>}
            </Col>
            <Col>
                <button className={"btn bg-theme"} onClick={finishEdit} disabled={isNameInvalid() || isColorInvalid()}>
                    {t('html.label.managePage.changes.save')}
                </button>
                <button className={"btn bg-grey ms-2"} onClick={discardEdit}>
                    {t('html.label.managePage.changes.discard')}
                </button>
            </Col>
        </Row>
    )
};

export default ColorEditForm