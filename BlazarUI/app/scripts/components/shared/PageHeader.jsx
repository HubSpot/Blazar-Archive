import React, {Component, PropTypes} from 'react';

class PageHeader extends Component {

  constructor(props) {
    super(props);
    this.state = {
      headerClass: ''
    };
  }

  componentDidMount() {
    window.onscroll = this.handleScroll.bind(this);
  }

  componentWillUnmount() {
    window.onscroll = null;
  }

  handleScroll(event) {
    let position = window.pageYOffset;
    if (position > 10) {
      this.setState({
        headerClass: 'page-header-small'
      });
    } else {
      this.setState({
        headerClass: ''
      });
    }
  }

  render() {
    return (
      <div className={`page-header ${this.state.headerClass}`}>
        {this.props.children}
      </div>
    );
  }
}

PageHeader.propTypes = {
  children: PropTypes.node
};

export default PageHeader;
