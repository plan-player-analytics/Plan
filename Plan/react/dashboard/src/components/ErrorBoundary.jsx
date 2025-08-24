import React from "react";

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            error: undefined,
            errorInfo: undefined
        };
    }

    componentDidCatch(error, errorInfo) {
        this.setState({error, errorInfo})
    }

    render() {
        if (this.state.error && this.props.fallbackFunction) {
            return this.props.fallbackFunction(this.state.error, this.state.errorInfo);
        }

        return this.props.children;
    }
}

export default ErrorBoundary;