import React, {Component, PropTypes} from 'react';
import DocumentTitle from 'react-document-title';
import classnames from 'classnames';

class PageContainer extends Component {

  getClassNames() {
    return classnames(
       'page-content',
       this.props.classNames
    );
  }

  getHeadline() {
    let headline;
    if (this.props.headline) {
      headline = <h2 className="PageContainer__headline">{this.props.headline}</h2>;
    }
    return headline;
  }

  render() {
    const innerContent = (
      <div className={this.getClassNames()}>
        {this.getHeadline()}
        {this.props.children}
      </div>
    );

    if (this.props.documentTitle) {
      const formattedTitle = `${this.props.documentTitle} | Blazar`;
      return (
        <DocumentTitle title={formattedTitle}>
          {innerContent}
        </DocumentTitle>
      );
    }

    return innerContent;
  }
}

PageContainer.propTypes = {
  headline: PropTypes.string,
  documentTitle: PropTypes.string,
  classNames: PropTypes.string,
  children: PropTypes.node
};

export default PageContainer;
