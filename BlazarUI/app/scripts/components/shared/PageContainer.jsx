import React, {Component, PropTypes} from 'react';
import classnames from 'classnames';

class PageContainer extends Component {

  constructor(props) {
    super(props);
  }

  getClassNames() {
    return classnames([
       'page-content',
       this.props.classNames
    ]);
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
  headline: PropTypes.string,
  classNames: PropTypes.string,
  children: PropTypes.node
};

export default PageContainer;
