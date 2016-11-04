import React, { Component, PropTypes } from 'react';

import Nav from 'react-bootstrap/lib/Nav';
import NavItem from 'react-bootstrap/lib/NavItem';
import Tab from 'react-bootstrap/lib/Tab';

class Tabs extends Component {
  constructor(props) {
    super(props);
    this.handleSelect = this.handleSelect.bind(this);
    this.state = {key: props.defaultActiveKey};
  }

  handleSelect(key) {
    this.setState({key});
  }

  renderTab(child) {
    if (!React.isValidElement(child) || !child.props.title) {
      return null;
    }

    const {title, eventKey, disabled} = child.props;
    return (
      <NavItem
        key={eventKey}
        eventKey={eventKey}
        disabled={disabled}
        className="private-tab"
      >
        {title}
        <span className="private-tab__indicator" />
      </NavItem>
    );
  }

  render() {
    const {id, className, children} = this.props;
    return (
      <Tab.Container activeKey={this.state.key} onSelect={this.handleSelect} className={className} id={id}>
        <div>
          <Nav bsClass="private-tabs">
            {children.map(this.renderTab)}
          </Nav>
          <Tab.Content animation={false}>
            {children}
          </Tab.Content>
        </div>
      </Tab.Container>
    );
  }
}

Tabs.propTypes = {
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  defaultActiveKey: PropTypes.string.isRequired,
  className: PropTypes.string,
  children: PropTypes.node
};

export default Tabs;
