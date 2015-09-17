import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

class PageHeader extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleScroll', 'headerClassForState');
    this.state = {
      isSmall: false
    };
  }

  componentDidMount() {
    window.onscroll = this.handleScroll;
  }

  componentWillUnmount() {
    window.onscroll = null;
  }

  handleScroll(event) {
    let position = window.pageYOffset;
    if (position > 10 && !this.state.isSmall) {
      this.setState({
        isSmall: true
      });
    } else if (position < 10 && this.state.isSmall) {
      this.setState({
        isSmall: false
      });
    }
  }

  headerClassForState() {
    return this.state.isSmall ? 'page-header-small' : '';
  }

  render() {
    return (
      <div className={`page-header ${this.headerClassForState()}`}>
        {this.props.children}
      </div>
    );
  }
}

PageHeader.propTypes = {
  children: PropTypes.node
};

export default PageHeader;
