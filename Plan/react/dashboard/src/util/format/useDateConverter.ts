import moment from 'moment';
import {useMetadata} from '../../hooks/metadataHook';
import {useI18nFriendlyLanguage} from '../../service/localeService';
import {useMemo} from 'react';

interface DateConverterBase {
    toEpochMs(): number;

    toServerEpochMs(): number;

    toUTCEpochMs(): number;

    toBrowserEpochMs(): number;

    toWallClockEpochMs(): number;

    toDate(): Date;

    toIsoString(): string;

    toFormat(format: string): string;

    toYYYYMMDD(): string;

    toDDMMYYYY(): string;

    removeBrowserOffset(): DateConverter;

    addBrowserOffset(): DateConverter;

    isValid(): boolean;
}

export type DateConverter = DateConverterBase;

export type InputOffset = 'browser' | 'server' | 'utc' | number;

class MomentDateConverter implements DateConverterBase {
    private readonly m: moment.Moment;
    private readonly offsetMs: number;

    constructor(m: moment.Moment, offsetMs: number) {
        this.m = m;
        this.offsetMs = offsetMs;
    }

    toEpochMs(): number {
        return this.m.valueOf();
    }

    toServerEpochMs(): number {
        return this.m.valueOf() + this.offsetMs;
    }

    toUTCEpochMs(): number {
        return this.m.valueOf();
    }

    toBrowserEpochMs(): number {
        const browserOffsetMs = moment().utcOffset() * 60 * 1000;
        return this.m.valueOf() - browserOffsetMs;
    }

    toWallClockEpochMs(): number {
        return this.m.valueOf();
    }

    toDate(): Date {
        const m = this.m;
        return new Date(m.year(), m.month(), m.date(), m.hours(), m.minutes(), m.seconds(), m.milliseconds());
    }

    toIsoString(): string {
        return this.m.toISOString();
    }

    toFormat(format: string): string {
        return this.m.format(format);
    }

    toYYYYMMDD(): string {
        return this.m.format('YYYY/MM/DD');
    }

    toDDMMYYYY(): string {
        return this.m.format('DD/MM/YYYY');
    }

    removeBrowserOffset(): DateConverter {
        const browserOffsetMinutes = this.m.utcOffset();
        return new MomentDateConverter(this.m.clone().subtract(browserOffsetMinutes, 'minutes'), this.offsetMs);
    }

    addBrowserOffset(): DateConverter {
        const browserOffsetMinutes = moment().utcOffset();
        return new MomentDateConverter(this.m.clone().add(browserOffsetMinutes, 'minutes'), this.offsetMs);
    }

    isValid(): boolean {
        return this.m.isValid();
    }
}

export const useDateConverter = () => {
    const {timeZoneOffsetMinutes} = useMetadata() as { timeZoneOffsetMinutes: number };
    const locale = useI18nFriendlyLanguage();
    const offsetMs = (timeZoneOffsetMinutes || 0) * 60 * 1000;

    return useMemo(() => {
        const convert = (date?: number | string | Date | moment.Moment, inputOffset?: InputOffset) => {
            let m: moment.Moment;
            if (date === undefined || date === null || date === '') {
                const invalidMoment = moment.invalid();
                return new MomentDateConverter(invalidMoment, offsetMs);
            }
            if (date instanceof Date || moment.isMoment(date)) {
                // When we get a Date object from a picker (like react-datepicker with timeZone="UTC"),
                // it represents the UTC date, but JS Date might still have local offset if not handled.
                // However, react-datepicker with timeZone="UTC" gives us a Date object where 
                // getFullYear, getMonth etc match the UTC values.
                // Using moment.utc([y, m, d...]) will treat it as UTC.
                m = moment.utc([
                    date instanceof Date ? date.getFullYear() : date.year(),
                    date instanceof Date ? date.getMonth() : date.month(),
                    date instanceof Date ? date.getDate() : date.date(),
                    date instanceof Date ? date.getHours() : date.hours(),
                    date instanceof Date ? date.getMinutes() : date.minutes(),
                    date instanceof Date ? date.getSeconds() : date.seconds(),
                    date instanceof Date ? date.getMilliseconds() : date.milliseconds()
                ]).locale(locale);

                if (typeof inputOffset === 'number') {
                    m.subtract(inputOffset, 'ms');
                } else {
                    switch (inputOffset) {
                        case 'browser':
                            m.subtract(moment().utcOffset(), 'minutes');
                            break;
                        case 'server':
                            m.subtract(offsetMs, 'ms');
                            break;
                        case 'utc':
                        default:
                            break;
                    }
                }
            } else if (typeof date === 'number') {
                m = moment.utc(date).locale(locale);
            } else if (date) {
                // Try various formats
                m = moment.utc(date, [
                    moment.ISO_8601,
                    'YYYY/MM/DD',
                    'DD/MM/YYYY',
                    'YYYY-MM-DD',
                    'DD-MM-YYYY',
                    'DDMMYYYY',
                    'YYYYMMDD',
                ], locale, true);
                if (!m.isValid()) {
                    m = moment.utc(date).locale(locale);
                }
            } else {
                m = moment.utc().locale(locale);
            }
            return new MomentDateConverter(m, offsetMs);
        };

        return {convert};
    }, [offsetMs, locale]);
};