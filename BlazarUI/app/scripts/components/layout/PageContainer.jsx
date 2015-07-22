import React from 'react';

class PageContainer extends React.Component {

  constructor(props) {
    super(props);
  }

  getClassNames() {
    return 'page-content ' + this.props.classNames;
  }

  getHeadline() {
    let headline;
    if (this.props.headline) {
      headline = <h2 className='PageContainer__headline'>{this.props.headline}</h2>;
    }
    return headline;
  }

  render() {
    return (
      <div className={this.getClassNames()}>
        {this.getHeadline()}
        {this.props.children}
      </div>
    );
  }
}

PageContainer.propTypes = {
  headline: React.PropTypes.string,
  classNames: React.PropTypes.string,
  children: React.PropTypes.node
};

export default PageContainer;
