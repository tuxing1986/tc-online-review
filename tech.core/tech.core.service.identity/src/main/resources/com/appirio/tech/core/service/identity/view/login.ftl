<#-- @ftlvariable name="" type="com.appirio.tech.core.service.identity.view.LoginView" -->
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<title>API v3 static web application</title>
<script src="https://cdn.auth0.com/js/lock-7.0.min.js"></script>
<script type="text/javascript">

  var lock = null;
  document.addEventListener( "DOMContentLoaded", function(){
    lock = new Auth0Lock('${clientId}', '${clientDomain}');
  });
  
  function signin() {
    lock.show({
        callbackURL: '${callbackUrl}'
      , responseType: 'code'
      , connections: ['LDAP']
      , authParams: {
        scope: 'openid profile offline_access',
        state: '${auth0_state}'
      }
      , usernameStyle: 'username'
    });
  }
</script>
</head>
<body>
<button onclick="signin()">Show Login</button>
</body>
</html>