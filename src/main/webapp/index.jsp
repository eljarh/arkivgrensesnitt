<%@page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>ePhorte adapter</title>
  </head>
  <body>
    <script>
function escapeXml(s) {
    return (s + "").replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function getUrl(form) {
    var resourceIds = form.resource.value.split(" ");
    if (form.resource.value === "") {
        resourceIds = getSubjects(form);
        form.resource.value = resourceIds.join(" ");
    }

    var action = form.action + "?resource=" + encodeURIComponent(resourceIds[0]);
    for (var i = 1; i < resourceIds.length; i++) {
        action = action + "&resource=" + encodeURIComponent(resourceIds[i]);
    }
    return action;
}

function getSubjects(form) {
    var parts = form.body.value.split("\n");
    var subjects = {};
    for (var i = 0; i < parts.length; i++) {
        var match = parts[i].match(/<(.*?)>/);
        subjects[match[1]] = 1;
    }

    var keys = [];
    for(var k in subjects) keys.push(k);
    
    return keys;
}

function doSubmit(form) {
    var resultDiv = document.getElementById("result");
    resultDiv.innerHTML = "Loading ...";

    var url = getUrl(form);
    var body = form.body.value;

    var req = new XMLHttpRequest();
    req.open("POST", url, true);
    req.setRequestHeader("Content-Type", "application/ntriples");
    req.onreadystatechange = function() {
        if (req.readyState != 4) return;
        var result = '<span class="status">' + escapeXml(req.status) + ": " + req.statusText + '</span><br />' +
                '<div class="text">' + escapeXml(req.responseText) + '</div>';


        resultDiv.innerHTML = result;
    };

    req.send(body);

    return false;
}
    </script>
    <h1>ePhorte adapter</h1>
    <form method="post" action="webapi/fragment" accept-charset="UTF-8" onsubmit="return doSubmit(this);"> <br/> <br/>
      <label for="resource">ResourceIds separated by space (autodetected if blank)</label> <br/>
      <input type="text" id="resource" name="resource" size="100" /> <br/>
      <label for="body">Fragment</label> <br/>
      <textarea rows="25" cols="80" name="body" id="body" style="width:100%">Insert fragment</textarea> <br/><br/>
      <input type="submit" value="Submit"/>
      <div id="result">
        Go ahead, post whatever you like.
      </div>
    </form>
  </body>
</html>
