import React from 'react';
import BranchModule from './BranchModule.jsx';

class BranchList extends React.Component {



  getModuleComponents() {
    let modules = this.props.modules.map( (module) => {
      return (
        <BranchModule key={module.module.name} moduleDetail={module} />
      );
    });
    return modules;
  }

  render() {

    return (
      <div>
        {this.getModuleComponents()}
      </div>

    );
  }

}


BranchList.propTypes = {
  loading: React.PropTypes.bool,
  modules: React.PropTypes.array.isRequired
};

export default BranchList;
