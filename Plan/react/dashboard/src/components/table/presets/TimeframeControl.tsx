import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Dropdown } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGear } from '@fortawesome/free-solid-svg-icons';
import { staticSite } from '../../../service/backendConfiguration';
import { TimeframePreset } from '../../../util/datapointTablePresets';
import { DatapointTimeframe } from './useDatapointTimeframe';
import CustomTimeframeInputs from './CustomTimeframeInputs';

type Props = {
    id: string;
    presets: TimeframePreset[];
    timeframe: DatapointTimeframe;
};

const TimeframeControl = ({ id, presets, timeframe }: Props) => {
    const { t } = useTranslation();
    const [customOpen, setCustomOpen] = useState(false);
    if (staticSite) return null;
    return (
        <div className='d-flex align-items-center gap-1'>
            <select
                id={id + '-timeframe'}
                className='form-select form-select-sm'
                style={{
                    width: 0,
                    flexGrow: 1,
                    minWidth: '8rem',
                    maxWidth: '16rem',
                }}
                aria-label={t('html.label.timeframe.label')}
                value={timeframe.preset}
                onChange={(event) => {
                    const preset = event.target.value as TimeframePreset;
                    timeframe.selectPreset(preset);
                    setCustomOpen(preset === 'custom');
                }}>
                {presets.map((preset) => (
                    <option key={preset} value={preset}>
                        {t('html.label.timeframe.' + preset)}
                    </option>
                ))}
            </select>
            {timeframe.preset === 'custom' && (
                <Dropdown
                    id={id + '-custom'}
                    show={customOpen}
                    onToggle={(next) => setCustomOpen(next)}
                    autoClose='outside'>
                    <Dropdown.Toggle
                        variant={''}
                        size='sm'
                        className={
                            timeframe.invalidCustom
                                ? 'text-warning'
                                : 'col-text'
                        }
                        style={
                            {
                                '--bs-btn-color':
                                    'var(--color-forms-input-text)',
                            } as React.CSSProperties
                        }
                        title={t('html.label.timeframe.custom')}
                        aria-label={t('html.label.timeframe.custom')}>
                        <FontAwesomeIcon icon={faGear} />
                    </Dropdown.Toggle>
                    <Dropdown.Menu>
                        <CustomTimeframeInputs id={id} timeframe={timeframe} />
                    </Dropdown.Menu>
                </Dropdown>
            )}
        </div>
    );
};

export default TimeframeControl;
