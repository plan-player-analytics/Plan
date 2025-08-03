import React from 'react';
import OutlineButton from "../input/OutlineButton.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import {Col} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import {useTheme} from "../../hooks/themeHook.jsx";

const AddThemeButton = () => {
    const navigate = useNavigate();
    const theme = useTheme();

    const onClick = () => {
        theme.toggleColorChooser();
        navigate("/theme-editor/new");
    }

    return (
        <Col xs={4}>
            <OutlineButton style={{height: '82px'}} onClick={onClick}>
                <FontAwesomeIcon icon={faPlus}/> Add Theme
            </OutlineButton>
        </Col>
    )
};

export default AddThemeButton