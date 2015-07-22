import React from 'react';
import { Link } from 'react-router';

class BranchModule extends React.Component{

  render() {
    let moduleDetail = this.props.moduleDetail;
    let name = moduleDetail.module.name;
    let modulePath = moduleDetail.modulePath;

    return (
      <div className='branch-module'>
        <Link className='branch-module__link' to={modulePath}>
          {name}
        </Link>
      </div>
    );
  }
}

BranchModule.propTypes = {
  moduleDetail: React.PropTypes.object.isRequired
};

export default BranchModule;
