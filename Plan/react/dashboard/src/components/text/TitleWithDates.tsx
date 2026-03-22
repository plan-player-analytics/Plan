import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRight} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {useDateFormatter} from "../../util/format/useDateFormatter";
import {useTranslation} from "react-i18next";

type Props = {
    label: string;
    fallback?: string;
    after: number;
    before: number;
}

export const TitleWithDates = ({label, fallback, after, before}: Props) => {
    const {t} = useTranslation();
    const {formatDate} = useDateFormatter(false, {
        pattern: "MMM dd yyyy - HH:mm:ss",
        recentDaysPattern: "MMM dd yyyy",
        noOffset: true
    });

    if (!after && !before) return t(fallback || '')

    return (<>{t(label)}: {formatDate(after)}
        <FontAwesomeIcon icon={faArrowRight} className={"ms-2 me-2"}/>
        {formatDate(before)}</>)
}