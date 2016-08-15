import React, {Component, PropTypes} from 'react';

class Checkbox extends Component {

  constructor(props) {
    super(props);

    this.state = {isChecked: this.props.checked || false};

    this.onChange = this.onChange.bind(this);
  }

  onChange() {
    const isChecked = !this.state.isChecked;

    this.setState({isChecked});
    this.props.onCheckboxUpdate(isChecked);
  }

  render() {
    return (
      <label>
        <input
          type="checkbox"
          name={this.props.name}
          value={this.props.value}
          checked={this.state.isChecked}
          onChange={this.onChange}
        />
        {this.props.label}
      </label>
    );
  }
}

Checkbox.propTypes = {
  checked: PropTypes.bool,
  onCheckboxUpdate: PropTypes.func,
  name: PropTypes.string.isRequired,
  value: PropTypes.string,
  label: PropTypes.string.isRequired
};

export default Checkbox;
