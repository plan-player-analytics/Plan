import {useTranslation} from "react-i18next";
import AsNumbersTable, {TableRow} from "./AsNumbersTable";
import {faUsers} from "@fortawesome/free-solid-svg-icons";
import React from "react";

const OnlineActivityAsNumbersTable = () => {
    const {t} = useTranslation();
    return (
        <AsNumbersTable
            headers={[t('html.label.last30days'), t('html.label.last7days'), t('html.label.last24hours')]}
        >
            <TableRow icon={faUsers} color="light-blue" text={t('html.label.uniquePlayers')}
                      values={["TODO"]}/>
        </AsNumbersTable>
    )
}

export default OnlineActivityAsNumbersTable;