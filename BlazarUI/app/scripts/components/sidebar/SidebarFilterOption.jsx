import React from 'react';


class SidebarFilterOption extends React.Component {

  render() {
    return (
      <div>
        {this.renderOption()}
      </div>
    );
  }

  renderOption() {
    let optionData = this.props.data.module;
    let inputValue = this.props.userInputValue;

    if (optionData.indexOf(inputValue) === 0) {
      return (
        <span>
          {inputValue}
          <strong>
            {optionData.slice(inputValue.length)}
            </strong>
        </span>
      );
    }
    return optionData;
  }

}

SidebarFilterOption.propTypes = {
  data: React.PropTypes.any,
  inputValue: React.PropTypes.string,
  isSelected: React.PropTypes.bool,
  userInputValue: React.PropTypes.string
};
