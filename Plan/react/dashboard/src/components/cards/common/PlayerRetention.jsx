import LoadIn from "../../animation/LoadIn.tsx";
import ExtendableRow from "../../layout/extension/ExtendableRow.jsx";
import {Col} from "react-bootstrap";
import PlayerRetentionGraphCard from "./PlayerRetentionGraphCard.jsx";
import React, {useState} from "react";
import {JoinAddressListContextProvider} from "../../../hooks/context/joinAddressListContextHook.jsx";
import AddressGroupSelectorRow from "./AddressGroupSelectorRow.jsx";

const PlayerRetention = ({id, seeRetention, identifier}) => {
    const [selectedGroupBy, setSelectedGroupBy] = useState('none');
    return (
        <LoadIn>
            {seeRetention && <section id={id} className={id}>
                <JoinAddressListContextProvider identifier={identifier} isAllowed={seeRetention}
                                                loadIndividualAddresses>
                    <ExtendableRow id={`row-${id}-0`}>
                        <Col lg={12}>
                            <PlayerRetentionGraphCard identifier={identifier}
                                                      selectedGroupBy={selectedGroupBy}
                                                      setSelectedGroupBy={setSelectedGroupBy}/>
                        </Col>
                    </ExtendableRow>
                    {selectedGroupBy === 'joinAddress' && <AddressGroupSelectorRow/>}
                </JoinAddressListContextProvider>
            </section>}
        </LoadIn>
    )
};

export default PlayerRetention