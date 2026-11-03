import React from 'react';

type Props = React.PropsWithChildren<{
    fallbackFunction?: (
        error: Error,
        errorInfo?: React.ErrorInfo,
    ) => React.ReactNode;
}>;

type State = {
    error?: Error;
    errorInfo?: React.ErrorInfo;
};

class ErrorBoundary extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            error: undefined,
            errorInfo: undefined,
        };
    }

    componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
        this.setState({ error, errorInfo });
    }

    render() {
        if (this.state.error && this.props.fallbackFunction) {
            return this.props.fallbackFunction(
                this.state.error,
                this.state.errorInfo,
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
