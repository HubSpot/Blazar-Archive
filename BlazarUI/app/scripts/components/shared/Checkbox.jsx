import React, {Component} from 'react';

class Checkbox extends Component {

  constructor(props) {
    super(props);

    this.state = {isChecked: this.props.checked || false};

    this.onChange = this.onChange.bind(this);
  }

  onChange() {
    const isChecked = !this.state.isChecked;

    this.setState({isChecked: isChecked});
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

export default Checkbox;
