import React, {useState} from "react";
import {Col, Row} from "react-bootstrap";


const SliceHeader = ({i, open, onClick, slice, alignment}) => {
    return (
        <li id={"slice_h_" + i} aria-controls={"slice_t_" + i}
            className={"clickable btn nav-item mb-1 " + (open ? "bg-plan" : "bg-grey-outline")}
            data-bs-target={"#slice_t_" + i} data-bs-toggle="collapse"
            onClick={onClick}
            style={{textAlign: alignment}}
        >
            {slice.header}
        </li>
    )
}

const SliceBody = ({i, open, slice}) => {
    return (
        <div className={"collapse" + (open ? ' open show' : 'closed')}
             data-bs-parent="#tableAccordion"
             id={"slice_t_" + i}
             style={open ? {} : {display: 'none'}}
        >
            {slice.body}
        </div>
    )
}

const SideNavTabs = ({slices, open, alignment}) => {
    const [openSlice, setOpenSlice] = useState(open ? 0 : -1);

    return (
        <>
            <Row>
                <Col md={2}>
                    <ul className={"nav flex-column"}>
                        {slices.length ? slices.map((slice, i) => (
                            <SliceHeader key={'slice-' + i}
                                         i={i}
                                         slice={slice}
                                         open={openSlice === i}
                                         onClick={() => setOpenSlice(i)}
                                         alignment={alignment}
                            />
                        )) : <li className={"nav-item"}>No Data</li>}
                    </ul>
                </Col>
                <Col md={10}>
                    {slices.length ? slices.map((slice, i) => (
                        <SliceBody key={'slice-' + i}
                                   i={i}
                                   slice={slice}
                                   open={openSlice === i}
                        />
                    )) : <p>No Data</p>}
                </Col>
            </Row>
        </>
    )
}

export default SideNavTabs;