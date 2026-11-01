import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import DateInputField from '../../../input/DateInputField.tsx';
import TimeInputField from '../../../input/TimeInputField.tsx';
import { Col, Row } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrashAlt } from '@fortawesome/free-regular-svg-icons';
import OutlineButton from '../../../input/button/OutlineButton.tsx';
import Checkbox from '../../../input/Checkbox.tsx';
import { InlinedRow } from '../../../layout/InlinedRow.tsx';
import { useMetadata } from '../../../../hooks/metadataHook.tsx';
import { usePreferences } from '../../../../hooks/preferences/preferencesHook.tsx';
import {
    calculatePresetRange,
    DATE_RANGE_PRESETS,
    getServerWallClockNow,
} from '../../../../util/queryDatePresets.ts';

const BetweenDatesFilter = ({
    index,
    label,
    filter,
    removeFilter,
    setFilterOptions,
    setAsInvalid,
    setAsValid,
}) => {
    const { t } = useTranslation();
    const select =
        index === 0
            ? t('html.query.filter.generic.start')
            : t('html.query.filter.generic.and');

    const options = filter.options;
    const { timeZoneOffsetMinutes } = useMetadata();
    const { firstDay } = usePreferences();

    const [preset, setPreset] = useState(
        filter.parameters && Object.keys(filter.parameters).length
            ? 'custom'
            : 'allData',
    );
    const [inputResetCount, setInputResetCount] = useState(0);

    const [fromDateEnabled, setFromDateEnabled] = useState(true);
    const [fromDate, setFromDate] = useState(
        filter.parameters?.afterDate || options.after[0],
    );
    const [fromTime, setFromTime] = useState(
        filter.parameters?.afterTime || options.after[1],
    );
    const [toDateEnabled, setToDateEnabled] = useState(true);
    const [toDate, setToDate] = useState(
        filter.parameters?.beforeDate || options.before[0],
    );
    const [toTime, setToTime] = useState(
        filter.parameters?.beforeTime || options.before[1],
    );

    const toggleFromDate = () => {
        if (!toDateEnabled && fromDateEnabled) {
            setToDateEnabled(true);
        }
        setFromDateEnabled(!fromDateEnabled);
        setPreset('custom');
    };
    const toggleToDate = () => {
        if (!fromDateEnabled && toDateEnabled) {
            setFromDateEnabled(true);
        }
        setToDateEnabled(!toDateEnabled);
        setPreset('custom');
    };

    const editedBy = (setter) => (value) => {
        setter(value);
        setPreset('custom');
    };

    const selectPreset = (event) => {
        const selected = event.target.value;
        setPreset(selected);
        if (selected === 'custom') return;

        const range =
            selected === 'allData'
                ? { after: options.after, before: options.before }
                : calculatePresetRange(
                      selected,
                      getServerWallClockNow(Date.now(), timeZoneOffsetMinutes),
                      firstDay ?? 1,
                  );
        if (!range) return;

        setFromDateEnabled(true);
        setToDateEnabled(true);
        setFromDate(range.after[0]);
        setFromTime(range.after[1]);
        setToDate(range.before[0]);
        setToTime(range.before[1]);
        ['-from-date', '-from-time', '-to-date', '-to-time'].forEach((suffix) =>
            setAsValid('filter-' + index + suffix),
        );
        setInputResetCount(inputResetCount + 1);
    };

    useEffect(() => {
        const after = fromDateEnabled
            ? {
                  afterDate: fromDate,
                  afterTime: fromTime,
              }
            : {};
        const before = toDateEnabled
            ? {
                  beforeDate: toDate,
                  beforeTime: toTime,
              }
            : {};
        setFilterOptions({
            ...filter,
            parameters: { ...after, ...before },
        });
    }, [
        setFilterOptions,
        fromDateEnabled,
        fromDate,
        fromTime,
        toDateEnabled,
        toDate,
        toTime,
        filter,
    ]);

    let actualLabel = label;
    if (!toDateEnabled) actualLabel = label.replace('Between', 'After');
    if (!fromDateEnabled) actualLabel = label.replace('Between', 'Before');

    return (
        <div id={'filter-' + index} className='mt-2'>
            <InlinedRow>
                <label>
                    {select} {t(actualLabel)}:
                </label>
                <select
                    id={'filter-' + index + '-preset'}
                    className='form-select form-select-sm w-auto ms-2'
                    aria-label={t('html.query.filter.preset.label')}
                    value={preset}
                    onChange={selectPreset}>
                    {DATE_RANGE_PRESETS.map((option) => (
                        <option key={option.name} value={option.name}>
                            {t(option.label)}
                        </option>
                    ))}
                </select>
            </InlinedRow>
            <Row className={'my-2 justify-content-start'}>
                <Col md={3} sm={6}>
                    <InlinedRow>
                        <Checkbox
                            checked={fromDateEnabled}
                            onChange={toggleFromDate}
                        />
                        <DateInputField
                            key={inputResetCount}
                            id={'filter-' + index + '-from-date'}
                            value={fromDate}
                            setValue={editedBy(setFromDate)}
                            placeholder={options.after[0]}
                            setAsInvalid={setAsInvalid}
                            setAsValid={setAsValid}
                            disabled={!fromDateEnabled}
                            type='string'
                        />
                    </InlinedRow>
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField
                        key={inputResetCount}
                        id={'filter-' + index + '-from-time'}
                        value={fromTime}
                        setValue={editedBy(setFromTime)}
                        placeholder={options.after[1]}
                        setAsInvalid={setAsInvalid}
                        setAsValid={setAsValid}
                        disabled={!fromDateEnabled}
                    />
                </Col>
                <Col
                    md={1}
                    sm={12}
                    className={'text-center my-1 my-md-2 flex-fill'}>
                    <label htmlFor='inlineFormCustomSelectPref'>&</label>
                </Col>
                <Col md={3} sm={6}>
                    <InlinedRow>
                        <Checkbox
                            checked={toDateEnabled}
                            onChange={toggleToDate}
                        />
                        <DateInputField
                            key={inputResetCount}
                            id={'filter-' + index + '-to-date'}
                            value={toDate}
                            setValue={editedBy(setToDate)}
                            placeholder={options.before[0]}
                            setAsInvalid={setAsInvalid}
                            setAsValid={setAsValid}
                            disabled={!toDateEnabled}
                            type='string'
                        />
                    </InlinedRow>
                </Col>
                <Col md={2} sm={6}>
                    <TimeInputField
                        key={inputResetCount}
                        id={'filter-' + index + '-to-time'}
                        value={toTime}
                        setValue={editedBy(setToTime)}
                        placeholder={options.before[1]}
                        setAsInvalid={setAsInvalid}
                        setAsValid={setAsValid}
                        disabled={!toDateEnabled}
                    />
                </Col>
                <Col md={'auto'} sm={12} className={'my-1 my-md-auto'}>
                    <OutlineButton
                        className='filter-remover float-end'
                        onClick={removeFilter}>
                        <FontAwesomeIcon icon={faTrashAlt} />
                    </OutlineButton>
                </Col>
            </Row>
        </div>
    );
};

export default BetweenDatesFilter;
