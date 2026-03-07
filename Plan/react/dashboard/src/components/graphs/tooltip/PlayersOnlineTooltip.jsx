import {useTranslation} from "react-i18next";
import React, {useCallback, useEffect, useMemo, useState} from "react";
import {fetchPlayersOnline} from "../../../service/serverService.js";
import {Card, CardBody} from "react-bootstrap";
import FormattedDate from "../../text/FormattedDate.jsx";

export const useTooltipOptions = (showPlayersOnline, setHoveredDate) => {
    return useMemo(() => {
        return showPlayersOnline ? {
            plotOptions: {
                series: {
                    point: {
                        events: {
                            mouseOver: e => {
                                setHoveredDate(e.target.y > 0 ? e.target.x : undefined)
                            },
                            click: e => {
                                setHoveredDate(e.target.point.y > 0 ? e.target.point.x : undefined)
                            }
                        }
                    }
                }
            }
        } : {};
    }, [showPlayersOnline, setHoveredDate]);
}

export const PlayersOnlineTooltip = ({id, hoveredDate, identifier}) => {
    const {t} = useTranslation();

    const [tooltipData, setTooltipData] = useState(undefined);
    const loadTooltipData = useCallback(async () => {
        if (!hoveredDate) return;
        try {
            setTooltipData((await fetchPlayersOnline(hoveredDate, identifier)).data);
        } catch (e) {
            console.error(e);
        }
    }, [hoveredDate]);
    useEffect(() => {
        const timeout = setTimeout(() => loadTooltipData(), 500);
        return () => {
            setTooltipData([]);
            clearTimeout(timeout);
        }
    }, [loadTooltipData])

    const playerCount = tooltipData?.length || 0;
    const visibleCount = Math.min(playerCount, 63);
    const split = []
    for (let i = 0; i < visibleCount; i++) {
        const player = tooltipData[i];
        split.push(player.name);
    }
    if (visibleCount < playerCount) {
        split.push(` ... (${playerCount - visibleCount} more)`);
    }

    const boxWidth = document.getElementById(id)?.getBoundingClientRect().width;
    const spaceRemoved = window.innerWidth - boxWidth;
    const tooltip = document.querySelector(`#${id} .highcharts-tooltip .highcharts-tooltip-header`);
    const left = tooltip?.getBoundingClientRect().x

    return (
        <aside className="overlay"
               style={{
                   position: "absolute",
                   top: "calc(100% - 6rem)",
                   left: left ? left + 105 - spaceRemoved / 2 : "calc(100%-2rem)",
                   zIndex: 1,
                   fontSize: "12px",
                   lineBreak: "strict",
                   maxWidth: 380
               }}>
            {!!tooltipData?.length && <Card>
                <CardBody style={{padding: "0.5rem"}}>

                    <small className={"col-text"}><b>{t('html.label.playersOnline')} • <FormattedDate
                        date={hoveredDate}/></b><br/>{split.join(', ')}
                    </small>
                </CardBody>
            </Card>}
        </aside>
    )
}