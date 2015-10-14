import React, {Component, PropTypes} from 'react';
import PaginationLink from './PaginationLink.jsx';

function getPagination(data, state, changePage) {
  const linksTotal = 5;
  const paginationLinks = [];
  const totalPages = Math.ceil(data.length / state.rowsPerPage);
  const blockFloor = Math.floor(state.page / linksTotal) * linksTotal;

  let start;
  let end;

  // no pagination necessary
  if (totalPages == 1) {
    return <div/>;
  }

  // have less than linksTotal to show
  if (totalPages < linksTotal) {
    start = blockFloor;
    end = totalPages;
  }
  // at the first block with more to show
  else if (blockFloor === 0) {
    start = 0;
    end = linksTotal;
  }
  // at the last block
  else if (blockFloor + linksTotal > totalPages) {
    start = blockFloor;
    end = blockFloor + (totalPages - blockFloor);
  }  
  // have one or more blocks to show
  else {
    start = blockFloor;
    end = blockFloor + linksTotal;
  }
  
  // Link to beginning
  if (state.page > linksTotal - 1) {
    paginationLinks.push( 
      <PaginationLink
        key='first'
        label='« First'
        page={0}
        changePage={changePage}
      /> 
    );
  }        

  // Back one block
  if (state.page > linksTotal - 1) {
    const backPages = 0;
    paginationLinks.push( 
      <PaginationLink
        key='back'
        label='‹'
        page={blockFloor - linksTotal}
        changePage={changePage}
      /> 
    );          
  }
  
  //build the inner page links
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
  
  // Not at a beginning or ending pagination block
  if (state.page < totalPages && end !== totalPages) {
    const backPages = 0;
    paginationLinks.push(
      <PaginationLink
        key='next'
        label='›'
        page={start + linksTotal}
        changePage={changePage}
      /> 
    );

    paginationLinks.push( 
      <PaginationLink
        key='last'
        label='Last »'
        page={totalPages - 1}
        changePage={changePage}
      />  
    );
  }

  return (
    <div className='pagination-wrapper'>
      <ul className='pagination'>
        {paginationLinks}
      </ul>
    </div>
  );
}

export default getPagination;
