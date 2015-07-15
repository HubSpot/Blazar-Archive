import React from 'react';
import UIGridConstants from './UIGridConstants.jsx';

const OFFSET_CLASSES = {
  0: '',
  1: 'col-sm-offset-1',
  2: 'col-sm-offset-2',
  3: 'col-sm-offset-3',
  4: 'col-sm-offset-4',
  5: 'col-sm-offset-5',
  6: 'col-sm-offset-6',
  7: 'col-sm-offset-7',
  8: 'col-sm-offset-8',
  9: 'col-sm-offset-9',
  10: 'col-sm-offset-10',
  11: 'col-sm-offset-11',
  12: 'col-sm-offset-12'
};

const SIZE_CLASSES = {
  1: 'col-sm-1',
  2: 'col-sm-2',
  3: 'col-sm-3',
  4: 'col-sm-4',
  5: 'col-sm-5',
  6: 'col-sm-6',
  7: 'col-sm-7',
  8: 'col-sm-8',
  9: 'col-sm-9',
  10: 'col-sm-10',
  11: 'col-sm-11',
  12: 'col-sm-12'
};

class UIGridItem extends React.Component {

  getClassName() {
    return `${SIZE_CLASSES[this.props.size]} ${OFFSET_CLASSES[this.props.offset]} ${this.props.className}`;
  }

  render() {
    return (
      <div {...this.props} className={this.getClassName()}>
        {this.props.children}
      </div>
    );
  }
}

UIGridItem.defaultProps = {
  className: ''
};

UIGridItem.propTypes = {
  offset: React.PropTypes.oneOf(UIGridConstants.OFFSET_RANGE),
  size: React.PropTypes.oneOf(UIGridConstants.SIZE_RANGE).isRequired,
  className: React.PropTypes.string,
  children: React.PropTypes.node
};

export default UIGridItem;
