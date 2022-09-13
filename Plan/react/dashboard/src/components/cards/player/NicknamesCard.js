import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import {Card} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faServer, faSignature} from "@fortawesome/free-solid-svg-icons";
import Scrollable from "../../Scrollable";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import React from "react";

const NicknamesCard = ({player}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faSignature}/> {t('html.label.seenNicknames')}
                </h6>
            </Card.Header>
            <Scrollable>
                <table className={"table table-striped mb-0" + (nightModeEnabled ? " table-dark" : '')}>
                    <thead className="bg-purple">
                    <tr>
                        <th><Fa icon={faSignature}/> {t('html.label.nickname')}</th>
                        <th><Fa icon={faServer}/> {t('html.label.server')}</th>
                        <th><Fa icon={faClock}/> {t('html.label.lastSeen')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {player.nicknames.map((nickname, i) => (<tr key={'nick-' + i}>
                        <td dangerouslySetInnerHTML={{__html: nickname.nickname}}/>
                        <td>{nickname.server}</td>
                        <td>{nickname.date}</td>
                    </tr>))}
                    </tbody>
                </table>
            </Scrollable>
        </Card>
    );
}

export default NicknamesCard;