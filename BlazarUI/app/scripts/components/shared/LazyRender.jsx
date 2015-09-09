import React from 'react/addons';

let LazyRender = React.createClass({
  propTypes: {
    children: React.PropTypes.array.isRequired,
    maxHeight: React.PropTypes.number.isRequired,
    childHeight: React.PropTypes.number,
    className: React.PropTypes.string,
    itemPadding: React.PropTypes.number
  },

  getDefaultProps: function() {
    return {
      itemPadding: 3
    };
  },

  getInitialState: function() {
    return {
      childrenTop: 0,
      childrenToRender: 10,
      scrollTop: 0,
      height: this.props.maxHeight
    };
  },

  onScroll: function() {
    let container = this.refs.container.getDOMNode();
    let scrollTop = container.scrollTop;

    let childrenTop = Math.floor(scrollTop / this.state.childHeight);
    let childrenBottom = (this.props.children.length - childrenTop -
                          this.state.childrenToRender);

    if (childrenBottom < 0) {
      childrenBottom = 0;
    }

    this.setState({
      childrenTop: childrenTop,
      childrenBottom: childrenBottom,
      scrollTop: scrollTop
    });
  },

  getHeight: function(numChildren, childHeight, maxHeight) {
    let fullHeight = numChildren * childHeight;
    if (fullHeight < maxHeight) {
      return fullHeight;
    }
    return maxHeight;
  },

  getElementHeight: function(element) {
    let elmHeight, elmMargin, elm = element;
    if (document.all) { // IE
        elmHeight = elm.currentStyle.height;
        elmMargin = parseInt(elm.currentStyle.marginTop, 10) + parseInt(elm.currentStyle.marginBottom, 10);
    } else { // Mozilla
        elmHeight =  parseInt(document.defaultView.getComputedStyle(elm, '').getPropertyValue('height'));
        elmMargin = parseInt(document.defaultView.getComputedStyle(elm, '').getPropertyValue('margin-top')) + parseInt(document.defaultView.getComputedStyle(elm, '').getPropertyValue('margin-bottom'));
    }
    return (elmHeight + elmMargin);
  },

  componentWillReceiveProps: function(nextProps) {
    let childrenTop = Math.floor(this.state.scrollTop / this.state.childHeight);
    let childrenBottom = (nextProps.children.length - childrenTop -
                          this.state.childrenToRender);

    if (childrenBottom < 0) {
      childrenBottom = 0;
    }

    let height = this.getHeight(
      nextProps.children.length,
      this.state.childHeight,
      nextProps.maxHeight
    );

    let numberOfItems = Math.ceil(height / this.state.childHeight);

    if (height === this.props.maxHeight) {
      numberOfItems += this.props.itemPadding;
    }

    this.setState({
      childrenTop: childrenTop,
      childrenBottom: childrenBottom,
      childrenToRender: numberOfItems,
      height: height
    });
  },

  componentDidMount: function() {
    let childHeight = this.getChildHeight();

    let height = this.getHeight(
      this.props.children.length,
      childHeight,
      this.props.maxHeight
    );

    let numberOfItems = Math.ceil(height / childHeight);

    if (height === this.props.maxHeight) {
      numberOfItems += this.props.itemPadding;
    }

    this.setState({
      childHeight: childHeight,
      childrenToRender: numberOfItems,
      childrenTop: 0,
      childrenBottom: this.props.children.length - numberOfItems,
      height: height
    });
  },

  componentDidUpdate: function() {
    if (this.state.childHeight !== this.getChildHeight()) {
      this.setState({childHeight: this.getChildHeight()});
    }
  },

  getChildHeight: function() {

    if (this.props.childHeight) {
      return this.props.childHeight;
    }

    if (this.props.children.length === 0) {
      return 0;
    }

    let firstChild = this.refs['child-0'];
    let el = React.findDOMNode(firstChild);

    if (el === null) {
      return this.props.maxHeight;
    }

    return this.getElementHeight(el);
  },

  render: function() {
    if (this.props.children.length === 0) {
      return <div></div>;
    }

    let start = this.state.childrenTop;
    let end = this.state.childrenTop + this.state.childrenToRender;

    let childrenToRender = this.props.children.slice(start, end);
    let children = childrenToRender.map(function(child, index) {
      if (index === 0) {
        return React.cloneElement(child, {ref: 'child-' + index, key: index});
      }
      return child;
    });

    children.unshift(
      <div style={
        { height: this.state.childrenTop * this.state.childHeight }
      } key="top"></div>
    );

    children.push(
      <div style={
        { height: this.state.childrenBottom * this.state.childHeight }
      } key="bottom"></div>
    );

    return (
      <div
        onMouseOver={() => {
          document.body.style.overflow = 'hidden';
        }}
        onMouseOut={() => {
          document.body.style.overflow = 'auto';
        }}
        style={{ height: this.state.height, overflowY: 'auto' }}
        className={this.props.className}
        ref="container"
        onScroll={this.onScroll}>
        {children}
      </div>
    );
  }
});

module.exports = LazyRender;
