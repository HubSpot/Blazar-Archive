import React, {Component, PropTypes} from 'react';
import SettingsContainer from '../components/settings/SettingsContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class Settings extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <HeaderContainer 
          params={this.props.params}
        />
        <SettingsContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Settings.propTypes = {
  params: PropTypes.object.isRequired
};

export default Settings;
