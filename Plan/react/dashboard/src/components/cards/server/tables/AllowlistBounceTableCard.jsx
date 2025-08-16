import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faFilterCircleXmark} from "@fortawesome/free-solid-svg-icons";
import {Card} from "react-bootstrap";
import AllowlistBounceTable from "../../../table/AllowlistBounceTable.jsx";
import {useTranslation} from "react-i18next";

const AllowlistBounceTableCard = ({bounces, lastSeen}) => {
    const {t} = useTranslation();
    return (
        <Card id={'allowlist-table'}>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faFilterCircleXmark} className="col-allow-list"/> {t('html.label.allowlistBounces')}
                </h6>
            </Card.Header>
            <AllowlistBounceTable bounces={bounces} lastSeen={lastSeen}/>
        </Card>
    )
};

export default AllowlistBounceTableCard;