import React, {Component, PropTypes} from 'react';
import Icon from '../shared/Icon.jsx';
import { bindAll } from 'underscore';
import StarredProvider from '../StarredProvider';

class Star extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleClick');
    this.state = {
      starred: StarredProvider.hasStar({
        repo: props.repo,
        branch: props.branch
      }) !== -1 };
  }

  handleClick(event) {
    event.stopPropagation();
    this.setState({
      starred: !this.state.starred
    });

    StarredProvider.starChange(this.state.starred, this.props.repo, this.props.branch);
  }

  render() {
    let className, iconName = '';
    if (this.state.starred) {
      className = 'sidebar__star selected';
      iconName = 'star';
    } else {
      className = 'sidebar__star unselected';
      iconName = 'star-o';
    }

    return (
      <span onClick={this.handleClick} className={ className }>
          <Icon name={ iconName }></Icon>
      </span>
    );
  }

}

Star.propTypes = {
  repo: PropTypes.string.isRequired,
  branch: PropTypes.string.isRequired
};

export default Star;
