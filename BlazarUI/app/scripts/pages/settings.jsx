import React, {PropTypes} from 'react';
import SettingsContainer from '../components/settings/SettingsContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

const Settings = ({params}) => (
  <div>
    <HeaderContainer
      params={params}
    />
    <SettingsContainer
      params={params}
    />
  </div>
);

Settings.propTypes = {
  params: PropTypes.object.isRequired
};

export default Settings;
