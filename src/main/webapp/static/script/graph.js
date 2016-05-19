var dic = {};
var group = 0;

function FetchNode() {
  var form = $('.graph_form');
  var query = {};
  query.url = form.find('[name=url]').val();
  //console.log('Query', query);
  _CleanResults();
  $.getJSON(form.attr('action'), query).done(function(top_docs) {
    //console.log('Result', top_docs);
    var graphjson = new Object();
    var centreNode = top_docs.node.normalized_url; 
    var nodes = new Array();
    var links = new Array();
    var nodeGroup = _AddDict(top_docs.node.url);
    
    nodes[0] = {"name":top_docs.node.normalized_url,"group":nodeGroup};
    var k = 0;
    for(var i = 0; i < top_docs.in.length; i++)
    {
    	var g = _AddDict(top_docs.in[i].url);
    	nodes.push({"name":top_docs.in[i].normalized_url,"group":g});
    	k++;
    	links.push({"source":k,"target":0,"value":5});
    }
    for(var i = 0; i < top_docs.out.length; i++)
    {
    	var g = _AddDict(top_docs.out[i].url);
    	nodes.push({"name":top_docs.out[i].normalized_url,"group":g});
    	k++;
    	links.push({"source":0,"target":k,"value":10});
    }
    graphjson.nodes = nodes;
    graphjson.links = links;
    console.log('graph', graphjson);
    _DrawGraph(graphjson);
    
  }).fail(function(msg) {
    console.log(msg.statusText);
  });
}

function _CleanResults() {
	 dic = {};
	 group = 0;
}

function _AddDict(herf){
	var a = document.createElement('a');
    a.href = herf;
    var host = a.hostname;
    if(dic.hasOwnProperty(host)){ // if (host in dic) {
    	console.log('Node',herf,host,dic[host]);
    	return dic[host];
    }
    else{
    	group++;
    	dic[host] = group;
    	console.log('newnode',herf,host,group);
    	return group;
    }
}

function _DrawGraph(graph){
	var width = 960,
    height = 500;
	console.log('Draws', graph);

	var color = d3.scale.category20();

	var force = d3.layout.force()
    	.charge(-150)  //相互之间力的作用
    	.linkDistance(150)  //制定连线长度
    	.size([width, height]);  //作用域范围

	var svg = d3.select("body").append("svg")
		.attr("width", width)
		.attr("height", height);
  
	force
      	.nodes(graph.nodes)
      	.links(graph.links)
      	.start();

	var link = svg.selectAll(".link")
      	.data(graph.links)
      	.enter()
      	.append("line")
      	.attr("class", "link")
      	.style("stroke-width", function(d) { return Math.sqrt(d.value); });

	var node = svg.selectAll(".node")
      	.data(graph.nodes)
      	.enter()
      	.append("circle")
      	.attr("class", "node")
      	.attr("r", 10)
      	.style("fill", function(d) { return color(d.group); })
      	.call(force.drag);
	
	/*var text = svg.selectAll(".text")
  		.data(graph.nodes)
  		.enter()
  		.append("text")
  		.attr("dx", 20)
  		.attr("dy", 8)
  		.style("fill", "black")
  		.text(function(d){return d.name; });*/

	node.append("title")
		.text(function(d) { return d.name; });

	force.on("tick", function() {
		link.attr("x1", function(d) { return d.source.x; })
			.attr("y1", function(d) { return d.source.y; })
			.attr("x2", function(d) { return d.target.x; })
			.attr("y2", function(d) { return d.target.y; });

    node.attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; });
    
    /*text.attr("x", function(d) { return d.x; })
    	.attr("y", function(d) { return d.y; });*/
  });
}