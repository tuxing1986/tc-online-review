<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script>
    //document.addEventListener( "DOMContentLoaded", function(){
        // Save the JWT token.
        localStorage.setItem('userJWTToken', "${auth0_id_token}");
        localStorage.setItem('userAuth0AccessToken', "${auth0_access_token}");
        
        //Stop at this page on debug
        if(!"${debug}") {
            location.href = "${redirectUrl}";
        }
    //});
    
    function redirect() {
        location.href = "${redirectUrl}";
    }
</script>
</head>
<body>
  <h3>Redirect page</h3>
  <p>
  <dl>
    <dt>auth0 jwt token</dt><dd>${auth0_id_token}</dd>
    <dt>auth0 access token</dt><dd>${auth0_access_token}</dd>
  </dl>
  Token has been set to localStorage 'userJWTToken' and 'userAuth0AccessToken'.<br/>
  Proceeding pages within the same domain can access these token via localStorage.getItem().
  </p>
  <button type="button" id="redirect" onclick="redirect()">Proceed with redirect</button>
</body>
</html>