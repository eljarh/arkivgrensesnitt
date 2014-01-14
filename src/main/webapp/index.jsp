<%@page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
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
        return form.action + "?subject=" + encodeURIComponent(form.subject.value);
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
                         '<pre class="text">' + escapeXml(req.responseText) + '</pre>';


            resultDiv.innerHTML = result;
        }

        req.send(body);

        return false;
      }
    </script>
    <form method="post" action="webapi/fragment" accept-charset="UTF-8" onsubmit="return doSubmit(this);"> <br/> <br/>
      <label for="subject">Subject (PSI)</label> <br/>
      <input type="text" id="subject" name="subject" size="100" /> <br/>
      <label for="body">Fragment</label> <br/>
      <textarea rows="25" cols="80" name="body" id="body" style="width:100%">Insert fragment</textarea> <br/><br/>
      <input type="submit" value="Submit"/>
      <div id="result">
        Go ahead, post whatever you like.
      </div>
    </form>
  </body>
</html>
