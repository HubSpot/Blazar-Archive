import React, { PropTypes } from 'react';
import Icon from '../shared/Icon.jsx';

const ALERT_TYPES = ['danger', 'info'];

const getIcon = (type, iconName) => {
  const classNames = `new-alert__icon new-alert__icon--${type}`;
  return <Icon name={iconName} classNames={classNames} />;
};

const Alert = ({children, className, titleText, type, iconName}) => {
  const classNames = `new-alert new-alert--${type} ${className}`;
  return (
    <div className={classNames} >
      {iconName && (
        <div className="new-alert__icon-container">
          {getIcon(type, iconName)}
        </div>
      )}
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
  type: PropTypes.oneOf(ALERT_TYPES),
  iconName: PropTypes.string
};

Alert.defaultProps = {
  type: 'info'
};

export default Alert;
