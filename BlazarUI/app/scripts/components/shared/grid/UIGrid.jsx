import React from 'react';

class UIGrid extends React.Component {

  constructor(props){
    super(props);
  }

  getClassName(){
    return `row ${this.props.className}`;
  }

  getContainerClassName(){
    return `container-fluid ui-grid ${this.props.containerClassName}`;
  }

  render() {
    return (
      <div className={this.getContainerClassName()}>
        <div {...this.props} className={this.getClassName()}>
          {this.props.children}
        </div>
      </div>
    );
  }
}

UIGrid.defaultProps = {className: '', containerClassName: ''};

export default UIGrid;