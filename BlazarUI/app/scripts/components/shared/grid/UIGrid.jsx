import React from 'react';

class UIGrid extends React.Component {
  
  constructor(props){
    super(props);
  }

  getClassName(){
    return `row ${this.props.className}`
  }

  render() {
    return (
      <div className='container-fluid'>
        <div {...this.props} className={this.getClassName()}>
          {this.props.children}
        </div>
      </div>
    );
  }
}

UIGrid.defaultProps = {className: ''};

export default UIGrid;