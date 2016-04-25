const
RESULTS_EACH_PAGE = 20;

function Search(start, limit) {
  var form = $('.search_form');
  var query = _ParseQuery(form, start, limit);
  console.log('Query', query);
  _CleanResults();
  $.getJSON(form.attr('action'), query).done(function(top_docs) {
    _RenderTopDocs(top_docs);
    _RenderPaging(start, limit, top_docs.total_hits);
  }).fail(function(msg) {
    console.log(msg.statusText);
  });
}

function _CleanResults() {
  $('.results_count').hide();
  $('.result:not(.template)').remove();
  $('.paging').hide();
}

function _RenderPaging(start, limit, total_hits) {
  /*
  var num_pages_before = (start + limit - 1) / limit;
  var num_pages_after = (total_hits - start - 1) / limit;
  if (num_pages_before == 0) {
    $('.page_previous').hide();
  } else {
    $('.page_previous').show().onclick(function() {
      var new_start = Math.max(start - limit, 0);
      Search(new_start, limit);
    });
  }
  if (num_pages_after == 0) {
    $('.page_after').hide();
  } else {
    $('.page_after').show().onclick(function() {
      Search(start + limit, limit);
    });
  }
  var current_page = start / limit;
  var left = current_page, right = current_page;
  */
}

function _RenderTopDocs(top_docs) {
  console.log('Result', top_docs);
  $('.results_count>span').text(top_docs.total_hits).show();

  $.each(top_docs.docs, function(i, doc) {
    var doc_node = $('.result.template').clone().removeClass('template');
    doc_node.find('.result_title>a').text(doc.title);
    doc_node.find('.result_title>a').attr('href',
        '/big-data-box/content/' + doc.doc_id);
    doc_node.find('.result_date').text(new Date(doc.date).toISOString());
    doc_node.find('.result_url').text(doc.url);
    doc_node.appendTo('.results').show();
  });
}

function _ParseQuery(form, start, limit) {
  var text = form.find('[name=text]').val();
  var title = form.find('[name=title]').val();
  var content = form.find('[name=content]').val();
  var start_date = form.find('[name=start_date]').val();
  var end_date = form.find('[name=end_date]').val();
  var url = form.find('[name=url]').val();
  var host = form.find('[name=host]').val();
  var query = {};
  if (text) {
    query.text = text;
  }
  if (title) {
    query.title = title;
  }
  if (content) {
    query.content = content;
  }
  if (start_date) {
    query.start_date = start_date;
  }
  if (end_date) {
    query.end_date = end_date;
  }
  if (url) {
    query.url = url;
  }
  if (host) {
    query.host = host;
  }
  query.start = start;
  query.limit = limit;
  return query;
}

function ToggleOptions() {
  $('.advanced_search_options').slideToggle('slow');
}