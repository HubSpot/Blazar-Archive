import React from 'react';

class PageContainer extends React.Component {

  constructor(props){
    super(props);
  }

  getClassNames() {
    return 'page-content ' + this.props.classNames;
  }

  render() {
    return (
      <div className={this.getClassNames()}>
        <h2 className='PageContainer__headline'>{this.props.headline}</h2>
        {this.props.children}
      </div>
    );
  }
}

PageContainer.propTypes = {
  headline: React.PropTypes.string,
  classNames: React.PropTypes.string
}

export default PageContainer;