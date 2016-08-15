import React, {PropTypes} from 'react';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import { Link } from 'react-router';

const NoBuildHistory = ({moduleName, modulePath}) => {
  const repo = modulePath.split('/')[4];
  const branch = modulePath.split('/')[5];

  return (
    <EmptyMessage simple={true}>
      <p>
        <strong>No build history for:</strong>
      </p>
      <span className="crumb">
        {repo}
      </span>
      <span className="crumb">
        {branch}
      </span>
      <Link to={modulePath}>
        {moduleName}
      </Link>
    </EmptyMessage>
  );
};

NoBuildHistory.propTypes = {
  moduleName: PropTypes.string.isRequired,
  modulePath: PropTypes.string.isRequired
};

export default NoBuildHistory;
