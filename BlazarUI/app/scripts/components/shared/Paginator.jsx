let React = require('react');

let segmentize = require('../../utils/segmentize');

function noop() {}

let Paginator = React.createClass({
    displayName: 'Paginator',

    propTypes: {
        hasData: React.PropTypes.bool,
        onSelect: React.PropTypes.func,
        page: React.PropTypes.number,
        beginPages: React.PropTypes.number,
        endPages: React.PropTypes.number,
        showPrevNext: React.PropTypes.bool,
        className: React.PropTypes.string,
        ellipsesClassName: React.PropTypes.string,
        selectedClassName: React.PropTypes.string,
        prevClassName: React.PropTypes.string,
        nextClassName: React.PropTypes.string
    },
    getDefaultProps() {
        return {
            onSelect: noop,
            showPrevNext: false,
            className: 'pagify-pagination',
            ellipsesClassName: '',
            selectedClassName: 'selected',
            prevClassName: '',
            nextClassName: ''
        };
    },
    render() {
        let {
            hasData,
            onSelect,
            page,
            ellipsesClassName,
            selectedClassName,
            className,
            showPrevNext,
            prevClassName,
            nextClassName
        } = this.props;

        if (!hasData) {
            return <div></div>;
        }

        let segments = segmentize(this.props);
        segments = segments.reduce(function(a, b) {
            return a.concat(-1).concat(b);
        });

        let items = segments.map((num, i) => {
            if (num >= 0) {
                return (
                    <li
                        key={'pagination-' + i}
                        onClick={onSelect.bind(null, num)}
                        className={num === page ? selectedClassName : ''}
                    >
                        <a href='#' onClick={this.preventDefault}>
                            {num + 1}
                        </a>
                    </li>
                );
            }

            return (
                <li
                    key={'pagination-' + i}
                    className={ellipsesClassName}
                >
                    <a>&hellip;</a>
                </li>
            );
        });

        let isFirstPage = page === 0;
        let isLastPage = page === segments[segments.length - 1];

        let prevButton = (
            <li
                onClick={!isFirstPage ? onSelect.bind(null, page - 1) : noop}
                className={prevClassName + (isFirstPage ? ' disabled' : '')}
            >
                <a href='#' onClick={this.preventDefault}>
                    <span aria-hidden="true">«</span>
                </a>
            </li>
        );

        let nextButton = (
            <li
                onClick={!isLastPage ? onSelect.bind(null, page + 1) : noop}
                className={nextClassName + (isLastPage ? ' disabled' : '')}
            >
                <a href='#' onClick={this.preventDefault}>
                    <span aria-hidden="true">»</span>
                </a>
            </li>
        );

        return (
            <ul className={className}>
                {showPrevNext && prevButton}
                {items}
                {showPrevNext && nextButton}
            </ul>
        );
    },

    preventDefault(e) {
        e.preventDefault();
    }
});

function paginate(data, o) {
    data = data || [];

    let page = o.page || 0;
    let perPage = o.perPage;

    let amountOfPages = Math.ceil(data.length / perPage);
    let startPage = page < amountOfPages ? page : 0;

    return {
        amount: amountOfPages,
        data: data.slice(startPage * perPage, startPage * perPage + perPage),
        page: startPage
    };
}

Paginator.paginate = paginate;

module.exports = Paginator;
