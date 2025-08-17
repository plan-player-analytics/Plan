import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRight, faQuestion, faSearch, faTrash} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import ActionButton from "../../input/button/ActionButton.jsx";
import OutlineButton from "../../input/button/OutlineButton.jsx";
import {Card, Col, Row} from "react-bootstrap";
import DateInputField from "../../input/DateInputField.jsx";
import TimeInputField from "../../input/TimeInputField.jsx";
import MultiSelect from "../../input/MultiSelect.jsx";
import Checkbox from "../../input/Checkbox.jsx";
import {BasicDropdown} from "../../input/BasicDropdown.jsx";
import SecondaryActionButton from "../../input/button/SecondaryActionButton.jsx";
import DangerButton from "../../input/button/DangerButton.jsx";
import Toggle from "../../input/Toggle.jsx";

const FormsUseCase = () => {
    const {t} = useTranslation();

    const label = t('html.label.themeEditor.example');
    const options = [
        {name: 'label-1', displayName: label},
        {name: 'label-2', displayName: label},
        {name: 'label-3', displayName: label}
    ];
    return (
        <Card>
            <Card.Body>
                <ActionButton disabled={false} className={"m-2"}>
                    <FontAwesomeIcon icon={faSearch}/> {label}
                </ActionButton>
                <ActionButton disabled={true} className={"m-2"}>
                    <FontAwesomeIcon icon={faSearch}/> {label}
                </ActionButton>
                <SecondaryActionButton disabled={false} className={"m-2"}>
                    <FontAwesomeIcon icon={faQuestion}/> {label}
                </SecondaryActionButton>
                <SecondaryActionButton disabled={true} className={"m-2"}>
                    <FontAwesomeIcon icon={faQuestion}/> {label}
                </SecondaryActionButton>
                <DangerButton disabled={false} className={"m-2"}>
                    <FontAwesomeIcon icon={faTrash}/> {label}
                </DangerButton>
                <DangerButton disabled={true} className={"m-2"}>
                    <FontAwesomeIcon icon={faTrash}/> {label}
                </DangerButton>
                <OutlineButton disabled={false} className={"m-2"}>
                    <FontAwesomeIcon icon={faArrowRight}/> {label}
                </OutlineButton>
                <OutlineButton disabled={true} className={"m-2"}>
                    <FontAwesomeIcon icon={faArrowRight}/> {label}
                </OutlineButton>
                <hr/>
                <Row>
                    <Col md={6}><DateInputField id={"viewToDateField"} placeholder={"01/07/2025"}/></Col>
                    <Col md={6}><TimeInputField id={"viewToTimeField"} placeholder={"12:00"}/></Col>
                </Row>
                <hr/>
                <Row>
                    <Col md={12}>
                        <MultiSelect options={[label, label, label]} selectedIndexes={[0]}/>
                    </Col>
                </Row>
                <hr/>
                <Checkbox indeterminate={false} checked={false} className={"ms-2"}>{label}</Checkbox>
                <Checkbox indeterminate={false} checked={true} className={"ms-2"}>{label}</Checkbox>
                <Checkbox indeterminate={true} checked={false} className={"ms-2"}>{label}</Checkbox>
                <Toggle value={true} inline className={"ms-2"}>{label}</Toggle>
                <Toggle value={false} inline className={"ms-2"}>{label}</Toggle>
                <hr/>
                <Row>
                    <Col md={12}>
                        <BasicDropdown selected={'label-1'} options={options}/>
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    )
};

export default FormsUseCase