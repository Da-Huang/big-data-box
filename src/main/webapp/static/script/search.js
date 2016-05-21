
function ClickSearchButton() {
  var form = $('.search_form');
  var text = form.find('[name=text]').val();
  var title = form.find('[name=title]').val();
  var content = form.find('[name=content]').val();
  var start_date = form.find('[name=start_date]').val();
  var end_date = form.find('[name=end_date]').val();
  var url = form.find('[name=url]').val();
  var host = form.find('[name=host]').val();

  var query =
      _ParseQuery(text, title, content, start_date, end_date, url, host, 0, 20);
  location.search = $.param(query);
}

$(document).ready(function() {
  var text = $.query.get('text');
  var title = $.query.get('title');
  var content = $.query.get('content');
  var start_date = $.query.get('start_date');
  var end_date = $.query.get('end_date');
  var url = $.query.get('url');
  var host = $.query.get('host');
  var start = $.query.get('start');
  var limit = $.query.get('limit');

  var form = $('.search_form');
  form.find('[name=text]').val(text);
  form.find('[name=title]').val(title);
  form.find('[name=content]').val(content);
  if (start_date) {
    try {
      start_date = new Date(start_date).toISOString().slice(0, 10);
      form.find('[name=start_date]').val(start_date);
    } catch (e) {
      console.log(e);
    }
  }
  if (end_date) {
    try {
      end_date = new Date(end_date).toISOString().slice(0, 10);
      form.find('[name=end_date]').val(end_date);
    } catch (e) {
      console.log(e);
    }
  }
  form.find('[name=url]').val(url);
  form.find('[name=host]').val(host);
  if (title || content || start_date || end_date || url || host) {
    $('.advanced_search_options').show();
  }

  if (text || title || content || content || start_date || end_date || url ||
      host) {
    var query = _ParseQuery(text, title, content, start_date, end_date, url,
                            host, start, limit);
    _Search(query);
  }
});

function _Search(query) {
  console.log('Query', query);
  $.getJSON('/big-data-box/search', query).done(function(top_docs) {
    _RenderTopDocs(top_docs);
    _RenderPaging(query.start, query.limit, top_docs.total_hits);
  }).fail(function(msg) {
    console.log(msg.statusText);
  });
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
  $('.result_list_count>span').text(top_docs.total_hits);
  $('.result_list_count').show();

  var result_template = $('.result.template');
  $.each(top_docs.docs, function(i, doc) {
    var doc_node = result_template.clone().removeClass('template');
    doc_node.find('.result_title>a').text(doc.title);
    doc_node.find('.result_title>a').attr('href', doc.url);
    doc_node.find('.result_cached')
        .attr('href', '/big-data-box/content/' + doc.doc_id);
    doc_node.find('.result_graph')
        .attr('href', '/big-data-box/graph?' + $.param({'url': doc.url}));
    doc_node.find('.result_date')
        .text(new Date(doc.date).toISOString().slice(0, 10));
    doc_node.find('.result_url').text(doc.url);
    doc_node.appendTo('.result_list').show();
  });
}

function _ParseQuery(text, title, content, start_date, end_date, url, host,
                     start, limit) {
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
    query.start_date = new Date(start_date).getTime();
  }
  if (end_date) {
    query.end_date = new Date(end_date).getTime();
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
