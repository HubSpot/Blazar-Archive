import React, {Component, PropTypes} from 'react';
import Logo from '../shared/Logo.jsx';


class Breadcrumbs extends Component {

  render() {

    return (
      <div className='breadcrumbs'>
        <Logo crumb={false} />
      </div>
    );

  }
}


Breadcrumbs.propTypes = {
  params: PropTypes.object.isRequired,
  appRoot: PropTypes.string.isRequired,
  navigationIsActive: PropTypes.bool
};


export default Breadcrumbs;
