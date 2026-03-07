import React from 'react';
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import Scrollable from "../Scrollable";

const GroupRow = ({group, color}) => {
    const {t} = useTranslation();
    return (
        <tr>
            <td style={{color}}>{t(group.name)}</td>
            <td>{group.y}</td>
        </tr>
    )
}

const GroupTable = ({groups, colors}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    function getColor(i) {
        if (groups[i].color) {
            return groups[i].color;
        }
        const index = i % colors.length;
        return nightModeEnabled ? withReducedSaturation(colors[index]) : colors[index];
    }

    return (
        <Scrollable>
            <table className={"table mb-0" + (nightModeEnabled ? " table-dark" : '')}>
                <tbody>
                {groups.length ? groups.map((group, i) =>
                        <GroupRow key={group.name}
                                  group={group}
                                  color={getColor(i)}/>) :
                    <tr>
                        <td>{t('generic.noData')}</td>
                        <td>-</td>
                        <td>-</td>
                        <td>-</td>
                    </tr>}
                </tbody>
            </table>
        </Scrollable>
    )
};

export default GroupTable