import {useCallback} from "react";
import {useTranslation} from "react-i18next";
import {useDecimalFormatter} from "./useDecimalFormatter";
import {useTimeAmountFormatter} from "./useTimeAmountFormatter";
import {capitalize} from "./capitalize";
import {includesAny} from "../includesAny";

export const useStatisticFormatter = () => {
    const {t} = useTranslation();
    const {formatDecimals} = useDecimalFormatter();
    const {formatTime} = useTimeAmountFormatter();
    return useCallback((name: string, value: number) => {
        if (!value) return '-';
        if (name.includes("damage_")) return t('html.label.stats.hearts', {value: value / 2});
        if (name.includes("_cm")) return t('html.label.stats.blocks', {value: formatDecimals(value / 10)});
        if (name.includes("time")) return formatTime(value * 1000 / 20);
        return value;
    }, []);
}

export const formatStatisticName = (name: string) => {
    const split = name.split(".");
    const last = split[split.length - 1];
    return capitalize(last.replaceAll("_", " ").replaceAll("one cm", ""))
}

export const getStatisticsCategory = (name: string) => {
    const split = name.split(":");
    if (includesAny(name, "damage_", "player_kills", "mob_kills", "deaths")) return "custom.damage";
    if (name.includes("_cm")) return "custom.travel";
    if (name.includes("time")) return "custom.time";
    if (includesAny(name, "inspect_", "interact", "open_", "_ring", "eat_",
        "play_", "tune_", "minecraft.pot_flower", "sleep_", "minecraft.use_cauldron", "trigger_")) {
        return "custom.block_interaction";
    }
    if (name.includes("_villager") || name.includes("raid")) return "custom.villagers";
    if (name.includes("clean_")) return "custom.cauldron_color_removal";
    if (name.includes("minecraft.custom")) return "custom.uncategorized"
    return split[0];
}