import React, { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import DateInputField from '../../../input/DateInputField.tsx';
import TimeInputField from '../../../input/TimeInputField.tsx';
import { Col, Row } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrashAlt } from '@fortawesome/free-regular-svg-icons';
import OutlineButton from '../../../input/button/OutlineButton.tsx';
import Checkbox from '../../../input/Checkbox.tsx';
import { InlinedRow } from '../../../layout/InlinedRow.tsx';
import {
    DATE_RANGE_PRESETS,
    toDateTimeRange,
} from '../../../../util/queryDatePresets.ts';
import { useTimeframePresets } from '../../../../hooks/timeframePresetsHook.ts';
import PresetSelect from '../../../input/PresetSelect.tsx';

const BetweenDatesFilter = ({
    index,
    fieldId,
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

    const presetRange = useCallback(
        (preset) => {
            if (preset.name === 'allData') {
                return { after: options.after, before: options.before };
            }
            const computed = preset.compute();
            return computed && toDateTimeRange(computed, options);
        },
        [options],
    );

    const applyPreset = useCallback(
        (preset) => {
            const range = presetRange(preset);
            if (!range) return;

            setFromDateEnabled(true);
            setToDateEnabled(true);
            setFromDate(range.after[0]);
            setFromTime(range.after[1]);
            setToDate(range.before[0]);
            setToTime(range.before[1]);
            ['-from-date', '-from-time', '-to-date', '-to-time'].forEach(
                (suffix) => setAsValid(fieldId + suffix),
            );
            setInputResetCount((count) => count + 1);
        },
        [presetRange, setAsValid, fieldId],
    );

    const presets = useTimeframePresets({
        defaultPreset:
            filter.parameters && Object.keys(filter.parameters).length
                ? 'custom'
                : 'allData',
        options: DATE_RANGE_PRESETS,
        zone: options,
        onSelect: applyPreset,
    });
    const { resetToCustom } = presets;
    const preset = presets.currentPreset.name;

    const toggleFromDate = () => {
        if (!toDateEnabled && fromDateEnabled) {
            setToDateEnabled(true);
        }
        setFromDateEnabled(!fromDateEnabled);
        resetToCustom();
    };
    const toggleToDate = () => {
        if (!fromDateEnabled && toDateEnabled) {
            setFromDateEnabled(true);
        }
        setToDateEnabled(!toDateEnabled);
        resetToCustom();
    };

    const editedBy = (setter) => (value) => {
        setter(value);
        resetToCustom();
    };

    useEffect(() => {
        if (preset === 'allData') {
            setFilterOptions({ ...filter, parameters: {} });
            return;
        }
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
        preset,
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
                <PresetSelect
                    id={fieldId + '-preset'}
                    className='ms-2'
                    presets={presets}
                />
            </InlinedRow>
            <Row className={'my-2 justify-content-start align-items-center'}>
                <Col md={3} sm={6}>
                    <InlinedRow style={{ alignItems: 'center' }}>
                        <Checkbox
                            checked={fromDateEnabled}
                            onChange={toggleFromDate}
                            center
                        />
                        <DateInputField
                            key={inputResetCount}
                            id={fieldId + '-from-date'}
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
                        id={fieldId + '-from-time'}
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
                            center
                        />
                        <DateInputField
                            key={inputResetCount}
                            id={fieldId + '-to-date'}
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
                        id={fieldId + '-to-time'}
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
