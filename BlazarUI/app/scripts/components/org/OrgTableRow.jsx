import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';

class OrgTableRow extends Component {

  render() {
    return (
      <tr>
        <td>
        </td>
        <td className='repo-name'>
          <Link to={this.props.data.get('blazarRepositoryPath')}>
            {this.props.data.get('repository')}
          </Link>
        </td>
        <td className='active-branch-count'>
          {this.props.data.get('branchCount')}
        </td>
      </tr>
    );
  }

}

OrgTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default OrgTableRow;
