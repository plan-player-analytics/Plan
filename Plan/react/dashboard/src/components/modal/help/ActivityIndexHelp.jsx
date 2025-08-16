import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {tooltip} from "../../../util/graphs";
import {useTheme} from "../../../hooks/themeHook";
import FunctionPlotGraph from "../../graphs/FunctionPlotGraph";
import {useTranslation} from "react-i18next";
import {Form, InputGroup} from "react-bootstrap";

const indexValue = x => {
    return 5 - 5 / ((Math.PI * x / 2) + 1);
}

const inverseIndex = y => {
    return -2 * y / (Math.PI * (y - 5));
}

const activityIndexPlot = (maxValue) => {
    const data = []
    let x;

    for (x = 0; x <= Math.max(3.5, maxValue); x += 0.01) {
        data.push([x, indexValue(x)]);
    }
    return data;
}

const ActivityIndexHelp = () => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    const yPlotLines = useMemo(() => [{
        color: 'var(--color-data-players-inactive)',
        value: 0,
        width: 1,
        label: {text: t('html.label.inactive')}
    }, {
        color: 'var(--color-data-players-very-active)',
        value: 3.75,
        width: 1.5,
        label: {text: t('html.label.veryActive')}
    }, {
        color: 'var(--color-data-players-active)',
        value: 3,
        width: 1.5,
        label: {text: t('html.label.active')}
    }, {
        color: 'var(--color-data-players-regular)',
        value: 2,
        width: 1.5,
        label: {text: t('html.label.regular')}
    }, {
        color: 'var(--color-data-players-irregular)',
        value: 1,
        width: 1.5,
        label: {text: t('html.label.irregular')}
    }], [t]);
    const xPlotLines = useMemo(() => [{
        color: 'black',
        value: 0,
        width: 1
    }], []);

    const [threshold, setThreshold] = useState(2);
    const [week1, setWeek1] = useState(0.5);
    const [week2, setWeek2] = useState(0.75);
    const [week3, setWeek3] = useState(0.9)
    const [result, setResult] = useState(0);

    const series = useMemo(() => {
        const inverse = inverseIndex(result);
        const data = activityIndexPlot(inverse);
        return [{
            name: t('html.label.activityIndex') + ' y=5-5/(πx/2)+1',
            data: data,
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            color: 'var(--color-data-players-online)'
        }, {
            name: t('html.label.help.testResult'),
            type: 'scatter',
            data: [{x: inverse, y: result, marker: {radius: 10}}],
            pointPlacement: 0,
            width: 5,
            tooltip: tooltip.twoDecimals,
            color: 'var(--color-data-players-very-active)'
        }]
    }, [nightModeEnabled, t, result]);

    useEffect(() => {
        setResult((indexValue(week1 / threshold) + indexValue(week2 / threshold) + indexValue(week3 / threshold)) / 3);
    }, [threshold, week1, week2, week3, setResult]);

    const onThresholdSet = useCallback((event) => setThreshold(event.target.value), [setThreshold]);
    const onWeek1Set = useCallback((event) => setWeek1(event.target.value), [setWeek1]);
    const onWeek2Set = useCallback((event) => setWeek2(event.target.value), [setWeek2]);
    const onWeek3Set = useCallback((event) => setWeek3(event.target.value), [setWeek3]);

    return (
        <>
            <p>{t('html.label.help.activityIndexBasis')}</p>
            <p>{t('html.label.help.activityIndexVisual')}</p>
            <FunctionPlotGraph id={'activity-index-graph'}
                               series={series}
                               yPlotLines={yPlotLines} xPlotLines={xPlotLines}
                               legendEnabled/>
            <ul>
                <li>{t('html.label.help.activityIndexExample1')}</li>
                <li>{t('html.label.help.activityIndexExample2')}</li>
                <li>{t('html.label.help.activityIndexExample3')}</li>
            </ul>
            <hr/>
            <p>{t('html.label.help.testPrompt')}</p>
            <InputGroup className={'mb-2'}>
                <InputGroup.Text>{t('html.label.help.threshold')}</InputGroup.Text>
                <Form.Control value={threshold} onChange={onThresholdSet} isInvalid={isNaN(threshold)}/>
                <InputGroup.Text>{t('html.label.help.thresholdUnit')}</InputGroup.Text>
            </InputGroup>
            <p>{t('html.label.playtime')}</p>
            <InputGroup className={'mb-1'}>
                <InputGroup.Text>{t('html.label.help.activityIndexWeek', {number: 1})}</InputGroup.Text>
                <Form.Control value={week1} onChange={onWeek1Set} isInvalid={isNaN(week1)}/>
                <InputGroup.Text>{t('html.label.help.playtimeUnit')}</InputGroup.Text>
            </InputGroup>
            <InputGroup className={'mb-1'}>
                <InputGroup.Text>{t('html.label.help.activityIndexWeek', {number: 2})}</InputGroup.Text>
                <Form.Control value={week2} onChange={onWeek2Set} isInvalid={isNaN(week2)}/>
                <InputGroup.Text>{t('html.label.help.playtimeUnit')}</InputGroup.Text>
            </InputGroup>
            <InputGroup className={'mb-2'}>
                <InputGroup.Text>{t('html.label.help.activityIndexWeek', {number: 3})}</InputGroup.Text>
                <Form.Control value={week3} onChange={onWeek3Set} isInvalid={isNaN(week3)}/>
                <InputGroup.Text>{t('html.label.help.playtimeUnit')}</InputGroup.Text>
            </InputGroup>
            <p>{t('html.label.help.testResult')} {result.toFixed(2)}</p>
        </>
    )
};

export default ActivityIndexHelp