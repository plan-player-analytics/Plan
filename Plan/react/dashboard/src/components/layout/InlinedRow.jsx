export const InlinedRow = ({children}) => {
    return (
        <div style={{display: 'flex', flexDirection: 'row', alignItems: 'center'}}>
            {children}
        </div>
    )
}