import React, { PropTypes } from 'react';
import Icon from '../../shared/Icon.jsx';

const SingularityLink = ({taskId}) => {
  // to do: surface singularity env
  const singularityPath = `https://tools.hubteamqa.com/singularity/task/${taskId}`;

  return (
    <a
      className="singularity-link"
      href={singularityPath}
      target="_blank"
      onClick={(e) => e.stopPropagation()}
      title="View in Singularity"
    >
      <Icon name="server" />
    </a>
  );
};

SingularityLink.propTypes = {
  taskId: PropTypes.string.isRequired
};

export default SingularityLink;
