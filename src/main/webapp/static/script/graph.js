var hostdic = {};
var group = 0;
var urldic = {};
var index = 0;
var graph;

function ClickViewButton() {
  var form = $('.search_form');
  var url = form.find('input[name=url]').val();
  location.search = $.param({'url': url});
}

$(document).ready(function() {
  var url = $.query.get('url');

  var form = $('.search_form');
  form.find('input[name=url]').val(url);

  if (url) {
    _View(url);
  }

  form.find('input[name=text]').keypress(function(event) {
    var code = (event.keyCode ? event.keyCode : event.which);
    if (code == '13') {
      ClickViewButton();
    }
  });
});

function _View(url) {
  $.getJSON('/big-data-box/graph', {'url': url}).done(function(node_graph) {
    // console.log('Result', node_graph);
    graph = new _DrawGraph();
    _ModifyGraph(node_graph);
  }).fail(function(msg) {
    console.log(msg.statusText);
  });
}

function _CleanResults() {
  var hostdic = {};
  var group = 0;
  var urldic = {};
  var index = 0;
}

function _AddHostDict(url){
  console.log("url",url);
  if (url[0] != 'h') {
    url = "http://" + url;
  }
  var a = document.createElement('a');
  a.href = url;
  var host = a.hostname;

  console.log("host",host);
  if (host in hostdic) {
    return hostdic[host];
  } else {
    hostdic[host] = group;
    group++;
    return group - 1;
  }
}

function _AddUrlDict(nurl){
  if (nurl in urldic) {
    return urldic[nurl];
  } else {
    urldic[nurl] = index;
    index++;
    return index - 1;
  }
}

function _FindNode(nurl){
  if (nurl in urldic) {
    return urldic[nurl];
  } else {
    return -1;
  }
}

function _DrawGraph(){
  this.addNode = function(nurl, url) {
    if (_FindNode(nurl) == -1) {
      var k = _AddUrlDict(nurl);
      var g = _AddHostDict(url);
      nodes[k] = {"url":url,"nurl":nurl,"group":g};
      update();
    }
  };

  this.addLink = function(source,target){
    var flag = 1;
    for(var i = 0; i < links.length;i++){
      if(links[i]['source'] == source && links[i]['target']==target){
        flag = 0;
        break;
      }
    }
    if(flag){
      links.push({"source":source,"target":target,"value":5});
      update();
    }
  };

  var width = 1400, height = 700;

  var color = d3.scale.category20();

  var force = d3.layout.force()
      .charge(-150)  // 相互之间力的作用
      .linkDistance(150)  // 制定连线长度
      .size([width, height]);  // 作用域范围

  var nodes = force.nodes(), links = force.links();

  var svg = d3.select("body")
                .append("svg")
                .attr("width", width)
                .attr("height", height);

  var defs = svg.append("defs");

  var arrowMarker =
      defs.append("marker")
          .attr("id", "arrow")
          .attr("markerUnits", "strokeWidth")  //标识大小的基准
          .attr("markerWidth", "7")            //标识大小
          .attr("markerHeight", "7")
          .attr("viewBox", "0 0 10 10")  //坐标系的区域
          .attr("refX", "6")  //在viewBox内的基准点，绘制时此点在直线端点上
          .attr("refY", "6")
          .attr("orient", "auto");  //绘制方向

  var arrow_path = "M2,2 L10,6 L2,10 L6,6 L2,2";

  arrowMarker.append("path").attr("d", arrow_path).attr("fill", "gray");

  force.start();

  var link =
      svg.selectAll(".link")
          .data(links)
          .enter()
          .append("line")
          .attr("class", "link")
          .attr("marker-end", "url(#arrow)")
          .style("stroke-width", function(d) { return Math.sqrt(d.value); });

  var node = svg.selectAll(".node")
                 .data(nodes)
                 .enter()
                 .append("circle")
                 .attr("class", "node")
                 .attr("r", 15)
                 .style("fill", function(d) { return color(d.group); })
                 .call(force.drag);

  node.append("title").text(function(d) { return d.nurl; });

  force.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; });

    node.on("dblclick", function(d) { _FindNewLink(d.url) });
  });

  var update = function() {
    link = link.data(force.links());

    link.enter()
        .append("line")
        .attr("class", "link")
        .attr("marker-end", "url(#arrow)")
        .style("stroke-width", function(d) { return Math.sqrt(d.value); });
    link.exit().remove();

    node = node.data(force.nodes());
    node.enter()
        .append("circle")
        .attr("class", "node")
        .attr("r", 15)
        .style("fill", function(d) { return color(d.group); })
        .call(force.drag);
    node.append("title").text(function(d) { return d.nurl; });
    node.exit().remove();
    force.start();
  };

}

function _FindNewLink(url){
  var query = {};
  query.url = url;
  // console.log('Query', query);
  $.getJSON('/big-data-box/graph', query).done(function(top_docs) {
    _ModifyGraph(top_docs);
  }).fail(function(msg) {
    console.log(msg.statusText);
  });
}

function _ModifyGraph(top_docs){
  graph.addNode(top_docs.node.normalized_url,top_docs.node.url);
  var k = _FindNode(top_docs.node.normalized_url);
  for(var i = 0; i < top_docs.in.length; i++) {
    graph.addNode(top_docs.in[i].normalized_url,top_docs.in[i].url);
    graph.addLink(_FindNode(top_docs.in[i].normalized_url),k);
  }
  for(var i = 0; i < top_docs.out.length; i++) {
    graph.addNode(top_docs.out[i].normalized_url,top_docs.out[i].url);
    graph.addLink(k,_FindNode(top_docs.out[i].normalized_url));
  }
}
