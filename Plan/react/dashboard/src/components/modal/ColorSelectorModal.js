import React from "react";
import {useTheme} from "../../hooks/themeHook";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCloudMoon, faPalette} from "@fortawesome/free-solid-svg-icons";
import {colorEnumToBgClass} from "../../util/colors";
import {Modal} from "react-bootstrap-v5";

const ColorSelectorButton = ({color, setColor, disabled}) =>
    <button className={"btn color-chooser " + colorEnumToBgClass(color)}
            id={"choose-" + color}
            disabled={disabled}
            onClick={() => setColor(color)}
    >
        <Fa icon={faPalette}/>
    </button>

const ColorSelectorModal = () => {
    const theme = useTheme();

    return (
        <Modal id="colorChooserModal"
               aria-labelledby="colorChooserModalLabel"
               show={theme.colorChooserOpen}
               onHide={theme.toggleColorChooser}>
            <Modal.Header>
                <Modal.Title id="colorChooserModalLabel">
                    <Fa icon={faPalette}/> Theme Select
                </Modal.Title>
                <button aria-label="Close" className="btn-close" onClick={theme.toggleColorChooser}/>
            </Modal.Header>
            <Modal.Body style={{padding: "0.5rem 0 0.5rem 0.5rem"}}>
                {theme.themeColors.map((color, i) =>
                    <ColorSelectorButton
                        key={i}
                        color={color.name}
                        setColor={theme.setColor}
                        disabled={theme.nightModeEnabled}
                    />)}
            </Modal.Body>
            <Modal.Footer>
                <button className="btn" id="night-mode-toggle" type="button" onClick={theme.toggleNightMode}>
                    <Fa icon={faCloudMoon}/> Night Mode
                </button>
                <button className="btn bg-plan" type="button" onClick={theme.toggleColorChooser}>OK</button>
            </Modal.Footer>
        </Modal>
    )
}

export default ColorSelectorModal;