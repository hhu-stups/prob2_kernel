<<<<<<< HEAD
var loglevel = "ERROR";

function switchLogLevel() {
	var level = "ERROR";
	var levela = $("#loglevel")[0];
	if (loglevel == "ERROR") {
		level = "TRACE";
	}
	$.getJSON("loglevel", {
		input : level
	}, function(data) {
		levela.innerHTML = data.output;
		loglevel = data.output;
	});

}

=======
>>>>>>> origin/feature/groovy_integration

function scrollDown(){
  window.scrollTo(0,document.body.scrollHeight);
}

function initialize() {
<<<<<<< HEAD
	var levela = $("#loglevel")[0];
	$.getJSON("loglevel", {
		input : loglevel
	}, function(data) {
		levela.innerHTML = data.output;
	});


=======
>>>>>>> origin/feature/groovy_integration
	// setup output polling
	setInterval(function() {
		$.ajax({
			url : "outputs",
			success : function(data) {
				if (data != "") {
					$("#system_out").get(0).innerHTML += data;
					scrollDown();
				}
			},
			dataType : "json"
		});
	}, 100);
}
