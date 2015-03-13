<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>API v3 static web application</title>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<!-- auth0.com -->
<script src="//cdn.auth0.com/w2/auth0-widget-5.2.min.js"></script>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<script>
    var AUTH0_DOMAIN = "${clientDomain}";
    function reloadProfile() {
        $("#auth0_domain").text(AUTH0_DOMAIN);
        $("#jwt_token").text(localStorage.getItem('userJWTToken'));
        $("#auth0_token").text(localStorage.getItem('userAuth0AccessToken'));
    }
    
    $(document).ready(function() {
        var widget = new Auth0Widget({
            // All this properties are set on auth0-variables.js
            domain: AUTH0_DOMAIN,
            clientID: "${clientId}",
            callbackURL: "${callbackUrl}"
        });
        widget.signin({
            container: 'auth0_login',
            chrome: true,
            scope: 'openid profile',
            state: "${auth0_state}"
        });

        $('.btn-logout').click(function(e) {
            localStorage.removeItem('userJWTToken');
            localStorage.removeItem('userAuth0AccessToken');
            reloadProfile();
        });
		
        if('${debug}' == 'true') {
        	reloadProfile();
        } else {
            $("#auth0_profile").hide();
        }
    });
</script>
</head>
<body>
  <div id="auth0_profile">
  <h3>Auth0 Profile</h3>
    <dl>
      <dt>auth0 domain:</dt><dd><div id="auth0_domain"/></dd>
      <dt>auth0 access token:</dt><dd><div id="auth0_token"/></dd>
      <dt>jwt token:</dt><dd><div id="jwt_token"/></dd>
    </dl>
    <button type="button" class="btn-logout" id="btn-logout">Logout (Clear Tokens)</button>
  </div>
  <h3>Auth0 Login (make it cleaner to look like topcoder login page)</h3>
  <div id="auth0_login" style="width: 320px; margin: 40px auto; padding: 10px; border-style: dashed; border-width: 1px;">
    embeded area
  </div>
</body>
</html>