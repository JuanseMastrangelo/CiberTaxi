<? 
	$contrasena = "admin"; // Contraseña del usuario ADMIN



	$pass = $_GET['pass'];
	$array_respuesta = array(); // Inicializamos el array[] que va a responder

	if($pass == $contrasena){ // Iniciamos sesion
		session_start();
		array_push($array_respuesta, "true");
		$_SESSION['nombre'] = "admin";
	}else{
		array_push($array_respuesta, "false");
	}
	echo json_encode($array_respuesta);// Enviamos el array de respuesta en forma de json
?>