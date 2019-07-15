<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Iniciar Sesion - CiberTaxi</title>
	<link type="text/css" media="all" href="css/main.css" rel="stylesheet" />


    <!-- Notificaciones -->
	<script type="text/javascript" src="js/notifIt.js"></script>
	<link rel="stylesheet" type="text/css" href="css/notifIt.css">

	<style type="text/css">
		html, body {
	        height: 100%;
	        margin: 0;
	        padding: 0;
	    }
	</style>
</head>
<body>
	<div class="container-lgn">
		<center>
			<div class="form-lgn">
				<p>Acceso:</p>
				<input type="password" placeholder="" id="pass"/>
				<button onClick="login();">Ingresar</button>
			</div>
		</center>
	</div>
	<script src="https://code.jquery.com/jquery-1.9.1.js"></script>
	<script type="text/javascript">
	function login(){
		if($("#pass").val() != ''){
			$.ajax({
	        type: "GET",
	        url: "BBDD/login.php?pass="+$("#pass").val(),             
	        dataType: 'json',
	        success: function(response){
	        	if(response[0]=="true"){
	        		window.location.href="index.php";
	        	}else{
	        		notif({
						msg: "Error: Contraseña inválida.",
						type: "error",
						timeout: 2000
					});
	        	}
	        }})
		}
	}
	</script>
</body>
</html>