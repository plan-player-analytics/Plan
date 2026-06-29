import {PropsWithChildren} from "react";

type Props = {
    justifyContent?: string;
} & PropsWithChildren;

export const InlinedRow = ({children, justifyContent}: Props) => {
    return (
        <div style={{display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent}}>
            {children}
        </div>
    )
}