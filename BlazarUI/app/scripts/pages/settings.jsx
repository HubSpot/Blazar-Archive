import React, {PropTypes} from 'react';
import SettingsContainer from '../components/settings/SettingsContainer.jsx';

const Settings = ({params}) => (
  <div>
    <SettingsContainer params={params} />
  </div>
);

Settings.propTypes = {
  params: PropTypes.object.isRequired
};

export default Settings;
