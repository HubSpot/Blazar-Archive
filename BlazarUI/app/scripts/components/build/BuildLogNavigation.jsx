import React, {Component, PropTypes} from 'react';
import {dataTagValue, buildIsOnDeck} from '../Helpers';
import BuildStates from '../../constants/BuildStates';
import JSONModal from '../shared/JSONModal.jsx';

class BuildLogNavigation extends Component {

  constructor(props) {
    super(props);
    this.handleNavClick = this.handleNavClick.bind(this);
    
    this.state = {
      topDisabled: this.props.loading,
      bottomDisabled: this.props.loading
    }
  }
  
  componentWillReceiveProps(nextProps) {
    this.setState({
      topDisabled: nextProps.loading,
      bottomDisabled: nextProps.loading
    })
  }

  handleNavClick(e) {
    const position = dataTagValue(e, 'position');

    this.setState({
      [position + 'Disabled']: true
    })

    this.props.requestNavigationChange(position);
  }

  render() {
    if (this.props.loading || buildIsOnDeck(this.props.build.state)) {
      return null;
    }

    return (
      <nav className='text-right'>
        <JSONModal
          classname='log-nav-btn log-nav-btn--json hidden-sm hidden-xs'
          json={this.props.build}
        />
        <button id='module-build-to-top-button' data-position='top' disabled={this.state.topDisabled} onClick={this.handleNavClick} className='log-nav-btn btn btn-default'>To Top</button>
        <button id='module-build-to-bottom-button' data-position='bottom' disabled={this.state.bottomDisabled} onClick={this.handleNavClick} className='log-nav-btn btn btn-default'>To Bottom</button>
      </nav>
    );
  }
}
// 
BuildLogNavigation.propTypes = {
  requestNavigationChange: PropTypes.func.isRequired,
  build: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired
};

export default BuildLogNavigation;
