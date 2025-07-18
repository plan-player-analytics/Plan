import {createContext, useContext, useState} from "react";
import {hsvToHex, randomHSVColor} from "../../util/colors.js";

const ColorEditContext = createContext({});

export const ColorEditContextProvider = ({colors, saveFunction, deleteFunction, children}) => {
    const [previous, setPrevious] = useState(undefined);
    const [name, setName] = useState(undefined);
    const [color, setColor] = useState("");
    const [deleting, setDeleting] = useState(false);
    const open = name !== undefined;

    const onNameChange = (value) => {
        setName(value.toLowerCase().replace(/[^a-z0-9-]/g, "-"));
    }

    const onColorChange = (value) => {
        setColor(value.toLowerCase());
    }

    const editColor = (name, color) => {
        setName(name);
        setColor(color);
        setPrevious(name);
    }

    const discardEdit = () => {
        setName(undefined);
        setColor(undefined);
        setPrevious(undefined);
    }

    const finishEdit = () => {
        if (name.length) {
            saveFunction(name, color, previous);
        } else {
            saveFunction('new-color-' + Math.floor(Math.random() * 1000), color, previous);
        }
        discardEdit();
    }


    const deleteColor = (name) => {
        deleteFunction(name);
    }

    const editNewColor = () => {
        setName("");
        setColor(hsvToHex(randomHSVColor(Math.floor(Math.random() * 100))));
    }

    const alreadyExists = previous !== name && !!colors[name];

    return (<ColorEditContext.Provider value={{
            alreadyExists,
            name,
            color,
            deleting,
            setDeleting,
            onNameChange,
            onColorChange,
            open,
            editColor,
            finishEdit,
            discardEdit,
            editNewColor,
            deleteColor
        }}>
            {children}
        </ColorEditContext.Provider>
    )
}

export const useColorEditContext = () => {
    return useContext(ColorEditContext);
}