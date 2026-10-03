import React from "react";
import {useTranslation} from "react-i18next";
import styles from './TextWithProgressBar.module.scss';

type Props = {
    positiveCount: number;
    negativeCount?: number;
    total: number;
    titleKey: string;
    textKey: string;
    otherTranslationParams?: { [key: string]: any }
    small?: boolean;
}

export const TextWithProgressBar = ({
                                        positiveCount,
                                        negativeCount,
                                        total,
                                        titleKey,
                                        textKey,
                                        otherTranslationParams,
                                        small
                                    }: Props) => {
    const {t} = useTranslation();

    const positiveFraction = positiveCount / (total || 1);
    const positivePercentage = (positiveFraction * 100).toFixed(1);
    const negativeFraction = negativeCount ? negativeCount / total : undefined;
    const negativePercentage = negativeFraction ? (negativeFraction * 100).toFixed(1) : undefined;

    const translationParams = {
        positivePercentage,
        negativePercentage,
        positiveCount,
        negativeCount,
        total, ...otherTranslationParams
    };
    const title = t(titleKey, translationParams);
    const text = t(textKey, translationParams);

    return (
        <div className={"float-end flex-column"} title={title}>
            {small && <small>{text}</small>}
            {!small && <span>{text}</span>}
            <ProgressBar progress={positiveFraction} noProgress={negativeFraction}/>
        </div>
    )
}

type ProgressBarProps = {
    progress: number;
    noProgress?: number;
};

function ProgressBar({progress, noProgress}: Readonly<ProgressBarProps>) {
    const value = Math.min(1, Math.max(0, progress));
    const noValue = noProgress ? Math.min(1, Math.max(0, noProgress)) : undefined;

    return (
        <div className={styles.progressBarContainer}>
            <div className={styles.positiveBar} style={{
                width: `${value * 100}%`
            }}/>
            {!!noValue && <div className={styles.negativeBar} style={{
                left: `${value * 100}%`,
                width: `${noValue * 100}%`
            }}/>}
        </div>
    );
}