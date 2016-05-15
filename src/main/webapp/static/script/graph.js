function FetchNode() {
  var form = $('.graph_form');
  var query = {};
  query.url = form.find('[name=url]').val();
  console.log('Query', query);
  _CleanResults();
  $.getJSON(form.attr('action'), query).done(function(top_docs) {
    console.log('Result', top_docs);
  }).fail(function(msg) {
    console.log(msg.statusText);
  });
}

function _CleanResults() {
}