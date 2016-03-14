import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import ClassNames from 'classnames';
import Icon from '../shared/Icon.jsx';


class Breadcrumb extends Component {

  constructor(props) {
    super(props);
  }

  refreshPaths() {
    const {appRoot, params} = this.props;
    const root = `/builds`;

    this.paths = {
      host: `${root}/${params.host}`,
      org: `${root}/${params.host}/${params.org}`,
      repo: `${root}/${params.host}/${params.org}/${params.repo}`,
      branch: `${root}/${params.host}/${params.org}/${params.repo}/${encodeURIComponent(params.branch)}`.replace('#', '%23'),
      buildNumber: `${root}/${params.host}/${params.org}/${params.repo}/${encodeURIComponent(params.branch)}/${params.buildNumber}`.replace('#', '%23'),
      moduleName: `${root}/${params.host}/${params.org}/${params.repo}/${encodeURIComponent(params.branch)}/${params.buildNumber}/${this.props.moduleName}`.replace('#', '%23')
    };
  }

  render() {
    this.refreshPaths();

    if (this.props.isActive || this.props.dontLink) {
      const classes = ClassNames([
        'crumb',
        {'active' : this.props.isActive}
      ]);
      
      return (
        <span className={classes}>
          <Icon classNames='breadcrumb-icon' for={this.props.param} />
          {this.props.text}
        </span>
      );
    }

    return (
      <Link className='crumb' to={this.paths[this.props.param]}>
        <Icon classNames='breadcrumb-icon' for={this.props.param} />
        {this.props.text}
      </Link>
    );
  }
}

Breadcrumb.propTypes = {
  params: PropTypes.object.isRequired,
  appRoot: PropTypes.string.isRequired,
  dontLink: PropTypes.bool,
  isActive: PropTypes.bool
};

export default Breadcrumb;
