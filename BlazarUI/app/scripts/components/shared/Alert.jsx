import React, { PropTypes } from 'react';
import Icon from '../shared/Icon.jsx';

const ALERT_TYPES = ['danger'];

const getIcon = (type) => {
  switch (type) {
    case 'danger':
      return <Icon name="exclamation" classNames="new-alert__icon new-alert__icon--danger" />;
    default:
      return null;
  }
};

const Alert = ({children, className, titleText, type}) => {
  const classNames = `new-alert new-alert--${type} ${className}`;
  return (
    <div className={classNames} >
      {getIcon(type)}
      <div className="new-alert__content">
        {titleText && <h5 className="new-alert__title">{titleText}</h5>}
        {children}
      </div>
    </div>
  );
};

Alert.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string,
  titleText: PropTypes.string,
  type: PropTypes.oneOf(ALERT_TYPES)
};

Alert.defaultProps = {
  type: 'danger'
};

export default Alert;
