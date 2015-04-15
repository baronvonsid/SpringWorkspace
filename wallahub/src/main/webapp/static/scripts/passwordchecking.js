/**
 * 
 */



    


    $("#mainForm").submit(function( event ) 
    {
    	var best=/^.*(?=.{6,})(?=.*[A-Z])(?=.*[\d])(?=.*[\W]).*$/;
    	var strong=/^[a-zA-Z\d\W_]*(?=[a-zA-Z\d\W_]{6,})(((?=[a-zA-Z\d\W_]*[A-Z])(?=[a-zA-Z\d\W_]*[\d]))|((?=[a-zA-Z\d\W_]*[A-Z])(?=[a-zA-Z\d\W_]*[\W_]))|((?=[a-zA-Z\d\W_]*[\d])(?=[a-zA-Z\d\W_]*[\W_])))[a-zA-Z\d\W_]*$/;
    	
    	if ( $("#newPassword").val() !=  $("#confirmPassword").val())
    	{
    		alert( "Your passwords don't match, please try re-entering them.");
    		event.preventDefault();
    		return;
    	}
    	
    	if (!best.test($("#newPassword").val()) && !strong.test($("#newPassword").val()) )
    	{
    		alert( "Password does not meet minimum complexity rules." );
    		event.preventDefault();
    		return;
    	}
    	
    });

    $("#newPassword").keyup(
    function(event)
    {
    	var noofchar=/^.*(?=.{6,}).*$/;
    	var checkspace=/\s/;
    	var best=/^.*(?=.{6,})(?=.*[A-Z])(?=.*[\d])(?=.*[\W]).*$/;
    	var strong=/^[a-zA-Z\d\W_]*(?=[a-zA-Z\d\W_]{6,})(((?=[a-zA-Z\d\W_]*[A-Z])(?=[a-zA-Z\d\W_]*[\d]))|((?=[a-zA-Z\d\W_]*[A-Z])(?=[a-zA-Z\d\W_]*[\W_]))|((?=[a-zA-Z\d\W_]*[\d])(?=[a-zA-Z\d\W_]*[\W_])))[a-zA-Z\d\W_]*$/;
    	var weak=/^[a-zA-Z\d\W_]*(?=[a-zA-Z\d\W_]{6,})(?=[a-zA-Z\d\W_]*[A-Z]|[a-zA-Z\d\W_]*[\d]|[a-zA-Z\d\W_]*[\W_])[a-zA-Z\d\W_]*$/;
    	var bad=/^((^[a-z]{6,}$)|(^[A-Z]{6,}$)|(^[\d]{6,}$)|(^[\W_]{6,}$))$/;
    	
		if ($("#newPassword").val().length < 1)
		{
			tdWeak.bgColor="transparent";
			tdStrong.bgColor="transparent";
			tdBest.bgColor="transparent";
			tdBad.bgColor="transparent";
			$("#passwordStrengthText").hide();
			return;
		}
		else
		{
			$("#passwordStrengthText").show();
		}
		
		if (true==checkspace.test($("#newPassword").val()))
		{
			passwordStrengthText.innerHTML="Spaces are not allowed";
		}
        else if ($("#newPassword").val().length > 0 && false==noofchar.test($("#newPassword").val()))
		{
			tdWeak.bgColor="transparent";
			tdStrong.bgColor="transparent";
			tdBest.bgColor="transparent";
			tdBad.bgColor="transparent";
			
			passwordStrengthText.innerHTML="Too short";
		}
		else if(best.test($("#newPassword").val()))
		{
			tdBad.bgColor="green";
			tdWeak.bgColor="green";
			tdStrong.bgColor="green";
			tdBest.bgColor="green";
			
			passwordStrengthText.innerHTML="Best password";
		}
		else if(strong.test($("#newPassword").val()))
		{
			tdBad.bgColor="yellow";
			tdWeak.bgColor="yellow";
			tdStrong.bgColor="yellow";
			tdBest.bgColor="transparent";
				
			passwordStrengthText.innerHTML="Strong password";
		}
		else if(bad.test($("#newPassword").val()))
		{
			tdWeak.bgColor="transparent";
			tdStrong.bgColor="transparent";
			tdBest.bgColor="transparent";
			tdBad.bgColor="red";
			
			passwordStrengthText.innerHTML="Poor password";
		}
		else if(weak.test($("#newPassword").val())) //
		{   
			tdBad.bgColor="orange";
			tdWeak.bgColor="orange";
			tdStrong.bgColor="transparent";
			tdBest.bgColor="transparent";
			
			passwordStrengthText.innerHTML="Weak password";
		}
	});
    
	function CloseAccountSubmit()
	{
		var currentPassword = $("#CurrentPassword").val();
		if (currentPassword.length < 6)
		{
			alert("You must enter your password to continue closing this fotowalla account.");
			return;
		}
		
		$("#mainAccountForm").submit();
	}