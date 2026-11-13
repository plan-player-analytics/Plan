import { useTranslation } from 'react-i18next';
import { TimeframePresets } from '../../hooks/timeframePresetsHook';
import { DateRangePreset } from '../../util/queryDatePresets';
import { classNames } from '../../util/classNames';
import { SEPARATOR } from '../../util/presetSeparator';

type Props = {
    id: string;
    presets: TimeframePresets;
    className?: string;
};

const PresetSelect = ({ id, presets, className }: Props) => {
    const { t } = useTranslation();
    return (
        <select
            id={id}
            className={classNames(
                'form-select form-select-sm w-auto',
                className,
            )}
            aria-label={t('html.query.filter.preset.label')}
            value={presets.currentPreset.name}
            onChange={(event) =>
                presets.selectPreset(event.target.value as DateRangePreset)
            }>
            {presets.presetOptions.map((option, i) =>
                option.name === SEPARATOR ? (
                    <hr key={option.name + i} />
                ) : (
                    <option key={option.name} value={option.name}>
                        {t(option.label)}
                    </option>
                ),
            )}
        </select>
    );
};

export default PresetSelect;
