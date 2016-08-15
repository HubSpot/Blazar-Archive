import React from 'react';

const LazyRender = React.createClass({
  propTypes: {
    children: React.PropTypes.array.isRequired,
    maxHeight: React.PropTypes.number.isRequired,
    childHeight: React.PropTypes.number,
    className: React.PropTypes.string,
    itemPadding: React.PropTypes.number,
    extraChildHeight: React.PropTypes.number
  },

  getDefaultProps() {
    return {
      itemPadding: 7,
      extraChildHeight: 0
    };
  },

  getInitialState() {
    return {
      childrenTop: 0,
      childrenToRender: 10,
      scrollTop: 0,
      height: this.props.maxHeight
    };
  },

  componentDidMount() {
    this.onMount();
  },

  componentWillReceiveProps(nextProps) {
    const childrenTop = Math.floor(this.state.scrollTop / this.state.childHeight);
    let childrenBottom = (nextProps.children.length - childrenTop -
                          this.state.childrenToRender);

    if (childrenBottom < 0) {
      childrenBottom = 0;
    }

    const height = this.getHeight(
      nextProps.children.length,
      this.state.childHeight,
      nextProps.maxHeight,
      nextProps.extraChildHeight
    );

    let numberOfItems = Math.ceil(height / this.state.childHeight);

    if (height === this.props.maxHeight) {
      numberOfItems += this.props.itemPadding;
    }

    this.setState({
      childrenTop,
      childrenBottom,
      childrenToRender: numberOfItems,
      height
    });
  },

  componentDidUpdate() {
    this.onUpdate();
  },

  onUpdate() {
    if (this.state.childHeight !== this.getChildHeight()) {
      this.setState({childHeight: this.getChildHeight()});
    }
  },

  onMount() {
    const childHeight = this.getChildHeight();

    const height = this.getHeight(
      this.props.children.length,
      childHeight,
      this.props.maxHeight,
      this.props.extraChildHeight
    );

    let numberOfItems = Math.ceil(height / childHeight);

    if (height === this.props.maxHeight) {
      numberOfItems += this.props.itemPadding;
    }

    this.setState({
      childHeight,
      childrenToRender: numberOfItems,
      childrenTop: 0,
      childrenBottom: this.props.children.length - numberOfItems,
      height
    });
  },

  onScroll() {
    const container = this.refs.container;
    const scrollTop = container.scrollTop;

    const childrenTop = Math.floor(scrollTop / this.state.childHeight);
    let childrenBottom = (this.props.children.length - childrenTop -
                          this.state.childrenToRender);

    if (childrenBottom < 0) {
      childrenBottom = 0;
    }

    this.setState({
      childrenTop,
      childrenBottom,
      scrollTop
    });
  },

  getHeight(numChildren, childHeight, maxHeight, extraChildHeight = 0) {
    const fullHeight = (numChildren * childHeight) + extraChildHeight;
    return fullHeight < maxHeight ? fullHeight : maxHeight;
  },

  getElementHeight(element) {
    let elmHeight;
    let elmMargin;
    const elm = element;

    if (document.all) { // IE
      elmHeight = elm.currentStyle.height;
      elmMargin = parseInt(elm.currentStyle.marginTop, 10) + parseInt(elm.currentStyle.marginBottom, 10);
    } else { // Mozilla
      elmHeight = parseInt(document.defaultView.getComputedStyle(elm, '').getPropertyValue('height'), 10);
      elmMargin = parseInt(document.defaultView.getComputedStyle(elm, '').getPropertyValue('margin-top'), 10) + parseInt(document.defaultView.getComputedStyle(elm, '').getPropertyValue('margin-bottom'), 10);
    }
    return (elmHeight + elmMargin);
  },

  getChildHeight() {
    if (this.props.childHeight) {
      return this.props.childHeight;
    }

    if (this.props.children.length === 0) {
      return 0;
    }

    const firstChild = this.refs['child-0'];

    if (firstChild === null) {
      return this.props.maxHeight;
    }

    return this.getElementHeight(firstChild);
  },

  render() {
    if (!this.props.children.length) {
      return <div />;
    }

    const end = this.state.childrenTop + this.state.childrenToRender;

    const childrenToRender = this.props.children.slice(0, end);
    const children = childrenToRender.map((child, index) => {
      if (index === 0) {
        return React.cloneElement(child, {ref: `child-${index}`, key: index});
      }
      return child;
    });

    let childHeightTop = this.state.childrenTop * this.state.childHeight;
    let childHeightBottom = this.state.childrenBottom * this.state.childHeight;

    if (isNaN(childHeightTop)) {
      childHeightTop = null;
    }

    if (isNaN(childHeightBottom)) {
      childHeightBottom = null;
    }

    children.push(
      <div style={
        { height: childHeightBottom }
      } key="bottom"></div>
    );

    const height = isNaN(this.state.height) ? null : this.state.height;

    return (
      <div
        style={{ height, overflowY: 'auto' }}
        className={this.props.className}
        ref="container"
        onScroll={this.onScroll}>
        {children}
      </div>
    );
  }
});

module.exports = LazyRender;
