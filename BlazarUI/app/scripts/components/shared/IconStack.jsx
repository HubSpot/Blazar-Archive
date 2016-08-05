import React, {Component, PropTypes} from 'react';
import Icon from './Icon.jsx';
import Immutable from 'immutable';
import classNames from 'classnames';

class IconStack extends Component {

  getBaseClassNames() {
    return classNames([
      'fa-stack-2x',
      this.props.classNames
    ]);
  }

  renderIconStackBase() {
    return (
      <Icon type="fa" classNames={this.getBaseClassNames()} name={this.props.iconStackBase} />
    );
  }

  renderIcon(iconName, i) {
    return (
      <Icon type="fa" key={i} classNames="fa-stack-1x" name={iconName} />
    );
  }

  render() {
    return (
      <span className="fa-icon-stack">
        {this.renderIconStackBase()}
        {this.props.iconNames.map((iconName, i) => this.renderIcon(iconName, i))}
      </span>
    );
  }
}

IconStack.defaultProps = {
  iconStackBase: '',
  iconNames: Immutable.List.of(),
  classNames: ''
};

IconStack.propTypes = {
  iconStackBase: PropTypes.string,
  iconNames: PropTypes.instanceOf(Immutable.List),
  classNames: PropTypes.string
};

export default IconStack;
