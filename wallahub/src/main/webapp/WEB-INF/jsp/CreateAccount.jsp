<%@page import="walla.datatypes.auto.*" pageEncoding="UTF-8"%>
<%@page import="walla.datatypes.java.*"%>
<%@page import="walla.business.*"%>
<%@page import="java.util.*"%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

    <title>FotoWalla - Viewer</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="content-script-type" content="text/javascript" />
    <meta name='description' content='FotoWalla - your trusted online foto gallery.' />
    <meta name='apple-mobile-web-app-title' content='FotoWalla' />

    <!-- Ensure that on IE the page is rendered to the maximum IE spec. -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />

    <!-- Used for viewing on mobile devices.  Ensuring the width is correctly infered -->

    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <!-- Adding javascript to enable CSS style for semantic tags pre IE9 -->
    <!--[if lt IE 9]>
         <script src="../../static/scripts/html5-format-ie8.js"></script>
    <![endif]-->

    <!-- Browser bar icon (16x16) -->
    <link href="../../../../static/images/fotowallabrowser.png" rel="shortcut icon" type="image/vnd.microsoft.icon" />

</head>



<body>

        
	<nav id="pageNavigations">
		<header id="pageHeader" class="HeaderStyle"><h1>Create Account</h1>
			<span class="HeaderStyle">Set up mandatory fields</span>
		</header>
		
		<div style="clear: both;"></div>
	</nav>
        
<input type="text" id="txtPassword" onkeyup="passwordcheck()"/>
<span id="tdPwdStrength">Password Strength</span>
             <table width="100px" >
                           <tr style="height:25px">
                           <td id="tdBad" ></td>
                           <td id="tdWeak"></td>
                           <td id="tdStrong"></td>
                           <td id="tdBest"></td>
                           </tr>
                    </table>
        
        
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

<script type="text/javascript">
function passwordcheck()
    {
       var password=document.getElementById('txtPassword').value;
 
 
        var noofchar=/^.*(?=.{6,}).*$/;
   		var checkspace=/\s/;
        var best=/^.*(?=.{6,})(?=.*[A-Z])(?=.*[\d])(?=.*[\W]).*$/;
        var strong=/^[a-zA-Z\d\W_]*(?=[a-zA-Z\d\W_]{6,})(((?=[a-zA-Z\d\W_]*[A-Z])(?=[a-zA-Z\d\W_]*[\d]))|((?=[a-zA-Z\d\W_]*[A-Z])(?=[a-zA-Z\d\W_]*[\W_]))|((?=[a-zA-Z\d\W_]*[\d])(?=[a-zA-Z\d\W_]*[\W_])))[a-zA-Z\d\W_]*$/;
        var weak=/^[a-zA-Z\d\W_]*(?=[a-zA-Z\d\W_]{6,})(?=[a-zA-Z\d\W_]*[A-Z]|[a-zA-Z\d\W_]*[\d]|[a-zA-Z\d\W_]*[\W_])[a-zA-Z\d\W_]*$/;
        var bad=/^((^[a-z]{6,}$)|(^[A-Z]{6,}$)|(^[\d]{6,}$)|(^[\W_]{6,}$))$/;
 
       if (true==checkspace.test(password))
           tdPwdStrength.innerHTML="spaces are not allowed";        
       else if (false==noofchar.test(password))
            {
            tdWeak.bgColor="transparent";
            tdStrong.bgColor="transparent";
            tdBest.bgColor="transparent";
            tdBad.bgColor="transparent"
            tdPwdStrength.innerHTML="must be 6 char";
             }
       else if(best.test(password))
            {
            tdBad.bgColor="green";
            tdWeak.bgColor="green";
            tdStrong.bgColor="green";
            tdBest.bgColor="green";
            tdPwdStrength.innerHTML="best";
            }
       else if(strong.test(password))
            {
            tdBad.bgColor="yellow";
            tdWeak.bgColor="yellow";
            tdStrong.bgColor="yellow";
            tdBest.bgColor="transparent"
            tdPwdStrength.innerHTML="Strong";
            }     
       else if(weak.test(password)==true && bad.test(password)==false)
            {   
            tdBad.bgColor="orange";
            tdWeak.bgColor="orange";
            tdStrong.bgColor="transparent";
            tdBest.bgColor="transparent";
            tdPwdStrength.innerHTML="weak";
            }
        else if(bad.test(password))
            {
            tdWeak.bgColor="transparent";
            tdStrong.bgColor="transparent";
            tdBest.bgColor="transparent";
            tdBad.bgColor="red";
            tdPwdStrength.innerHTML="Bad";
            }
                 }
</script>


</body>
   

</html>
