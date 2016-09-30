import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

class Card extends Component {
  getClassNames() {
    const {expanded, className} = this.props;
    return classNames(
      'card', {
        'card--expanded': expanded
      },
      className
    );
  }

  getDetailsClassNames() {
    return classNames(
      'card__details', {
        'card__details--collapsed': !this.props.expanded
      }
    );
  }

  renderDetails() {
    return (
      <div className={this.getDetailsClassNames()}>
        {this.props.details}
      </div>
    );
  }

  renderSummary() {
    return (
      <div className="card__summary" onClick={this.props.onClick}>
        {this.props.summary}
      </div>
    );
  }

  render() {
    return (
      <div className={this.getClassNames()}>
        {this.renderSummary()}
        {this.renderDetails()}
      </div>
    );
  }
}

Card.propTypes = {
  summary: PropTypes.node.isRequired,
  details: PropTypes.node,
  expanded: PropTypes.bool,
  onClick: PropTypes.func,
  className: PropTypes.string
};

export default Card;
