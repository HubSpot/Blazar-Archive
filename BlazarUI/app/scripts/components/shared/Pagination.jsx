import React from 'react';
import PaginationLink from './PaginationLink.jsx';

function getPagination(data, state, changePage) {
  const linksTotal = 5;
  const paginationLinks = [];
  const totalPages = Math.ceil(data.length / state.rowsPerPage);
  const blockFloor = Math.floor(state.page / linksTotal) * linksTotal;

  let start;
  let end;

  // no pagination necessary
  if (totalPages === 1) {
    return null;
  }

  // have less than linksTotal to show
  if (totalPages < linksTotal) {
    start = blockFloor;
    end = totalPages;
  } else if (!blockFloor) {
    // at the first block with more to show
    start = 0;
    end = linksTotal;
  } else if (blockFloor + linksTotal > totalPages) {
    // at the last block
    start = blockFloor;
    end = blockFloor + (totalPages - blockFloor);
  } else {
    // have one or more blocks to show
    start = blockFloor;
    end = blockFloor + linksTotal;
  }
  // To beginning
  paginationLinks.push(
    <PaginationLink
      key="first"
      label="« First"
      disabled={state.page === 0}
      page={0}
      changePage={changePage}
    />
  );
  // Back one
  paginationLinks.push(
    <PaginationLink
      key="backBlock"
      label="‹ Previous"
      disabled={state.page === 0}
      page={state.page - 1}
      changePage={changePage}
    />
  );
  // Inner page links
  for (let i = start; i < end; i++) {
    paginationLinks.push(
      <PaginationLink
        key={i}
        page={i}
        changePage={changePage}
        activePage={state.page}
      />
    );
  }
  // Forward one
  paginationLinks.push(
    <PaginationLink
      key="next"
      label="Next ›"
      disabled={state.page + 1 === totalPages}
      page={state.page + 1}
      changePage={changePage}
    />
  );
  // Last
  paginationLinks.push(
    <PaginationLink
      key="last"
      label="Last »"
      page={totalPages - 1}
      disabled={state.page + 1 === totalPages}
      changePage={changePage}
    />
  );

  return (
    <div className="pagination-wrapper">
      <ul className="pagination">
        {paginationLinks}
      </ul>
      <p className="pagination-footer">
        Page {state.page + 1} of {totalPages}
      </p>
    </div>
  );
}

export default getPagination;
