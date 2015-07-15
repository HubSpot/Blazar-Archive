import React from 'react';
import Select from 'react-select';
import ComponentHelpers from '../ComponentHelpers';

class SidebarFilter extends React.Component {

  constructor(props) {
    super(props);
    ComponentHelpers.bindAll(this, ['getOptions']);
  }

  // user has clicked an options
  selectionMade(val) {
    console.log('user has clicked an option: ', val);
  }

  // check if we need to expand any repos
  checkFilterState(input, cb) {
    this.props.updateResults(input);
    cb();
  }

  // a slight hack to get the value the user typed,
  // (as react-select doesnt have a direct keyup event)
  // then we pass back the original options from props
  getOptions(input, cb) {
    let options = { options: this.props.modules };
    this.checkFilterState(input, () => {
      cb(null, options);
    });
  }

  render() {

    if (this.props.loading) {
      return <div></div>;
    }
    return (
      <div className="form-group">
        <Select
          name="repolist"
          value="Filter Modules..."
          asyncOptions={this.getOptions}
          onChange={this.selectionMade}
          onBlur={this.blur}
          autoload={false}
        />
      </div>
    );
  }
}

SidebarFilter.propTypes = {
  updateResults: React.PropTypes.func.isRequired,
  modules: React.PropTypes.array.isRequired,
  loading: React.PropTypes.bool.isRequired
};

export default SidebarFilter;
