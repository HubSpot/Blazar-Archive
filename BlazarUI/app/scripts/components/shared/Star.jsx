import React, {Component, PropTypes} from 'react';
import { connect } from 'react-redux';
import Icon from '../shared/Icon.jsx';
import classnames from 'classnames';
import { toggleStar } from '../../redux-actions/starredBranchesActions';

class Star extends Component {

  constructor(props) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick(event) {
    event.stopPropagation();
    this.props.toggleStar(this.props.branchId);
  }

  getContainerClassNames() {
    return classnames(
       'star',
       this.props.className, {
         selected: this.props.isStarred,
         unselected: !this.props.isStarred,
       }
    );
  }

  render() {
    return (
      <span onClick={this.handleClick} className={this.getContainerClassNames()}>
        <Icon name={this.props.isStarred ? 'star' : 'star-o'} />
      </span>
    );
  }

}

Star.propTypes = {
  className: PropTypes.string,
  isStarred: PropTypes.bool.isRequired,
  branchId: PropTypes.number.isRequired,
  toggleStar: PropTypes.func.isRequired
};

const mapStateToProps = (state, ownProps) => ({
  isStarred: state.starredBranches.has(ownProps.branchId)
});

export default connect(mapStateToProps, {toggleStar})(Star);
