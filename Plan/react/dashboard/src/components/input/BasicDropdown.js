import React from 'react';
import DropdownToggle from "react-bootstrap-v5/lib/esm/DropdownToggle";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import DropdownMenu from "react-bootstrap-v5/lib/esm/DropdownMenu";
import DropdownItem from "react-bootstrap-v5/lib/esm/DropdownItem";
import {useTranslation} from "react-i18next";

const BasicDropdown = ({selected, optionList, onChange, optionLabelMapper, icon, title}) => {
    const {t} = useTranslation();

    return (
        <BasicDropdown className="float-end" style={{position: "absolute", right: "0.5rem"}} title={t(title)}>
            <DropdownToggle variant=''>
                <Fa icon={icon}/> {t(optionLabelMapper ? optionLabelMapper(selected) : selected)}
            </DropdownToggle>

            <DropdownMenu>
                <h6 className="dropdown-header">{t(title)}</h6>
                {optionList.map((option, i) => (
                    <DropdownItem key={i} onClick={() => onChange(option)}>
                        {t(optionLabelMapper ? optionLabelMapper(option) : option)}
                    </DropdownItem>
                ))}
            </DropdownMenu>
        </BasicDropdown>
    )
};

export default BasicDropdown