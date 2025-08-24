import React from 'react';

const getMinecraftColorClass = (colorLetter, previousColor) => {
    if (!colorLetter) return "";
    switch (colorLetter.toUpperCase()) {
        case "0":
            return "black";
        case "1":
            return "darkblue";
        case "2":
            return "darkgreen";
        case "3":
            return "darkaqua";
        case "4":
            return "darkred";
        case "5":
            return "darkpurple";
        case "6":
            return "gold";
        case "7":
            return "gray";
        case "8":
            return "darkgray";
        case "9":
            return "blue";
        case "A":
            return "green";
        case "B":
            return "aqua";
        case "C":
            return "red";
        case "D":
            return "pink";
        case "E":
            return "yellow";
        case "F":
            return "white";
        case "L":
            return previousColor ? "bold " + previousColor : "bold";
        case "M":
            return previousColor ? "strikethrough " + previousColor : "strikethrough";
        case "N":
            return previousColor ? "underline " + previousColor : "underline";
        case "O":
            return previousColor ? "italic " + previousColor : "italic";
        case "R":
            return "";
        default:
            return previousColor || "";
    }
}

const ColoredText = ({text}) => {
    if (!text) return <></>;
    if (typeof text !== 'string') return text;

    const parts = text.split(text.includes('&sect;') ? '&sect;' : '§');
    const htmlElements = [];
    let previousColor = undefined;
    let i = 0;
    for (const part of parts) {
        // Don't take away letters if text doesn't start with a color.
        // Also appends whole text if there is no colors in entire text.
        if (i === 0 && !(text.startsWith('§') || text.startsWith('&sect;'))) {
            htmlElements.push(part);
        } else {
            const minecraftColorClass = getMinecraftColorClass(part[0], previousColor);
            previousColor = minecraftColorClass;
            htmlElements.push(<span key={'part-' + i}
                                    className={minecraftColorClass}>{part.substring(1)}</span>);
        }
        i++;
    }

    return (
        <>
            {htmlElements}
        </>
    )
};

export default ColoredText