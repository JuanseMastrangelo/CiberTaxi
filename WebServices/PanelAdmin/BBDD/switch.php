<?php
	//ALGORITMO PARA ASIGNAR VIAJES DE FORMA MANUAL O AUTOMÄTICA

	// CONEXIÓN A LA BASE DE DATOS
	include ("config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');
	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysql_select_db($db_name);
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}
	// FIN CONEXIÓN


	$array_respuesta = array();
	// Comprobamos el estado del switch
	$estadoSwitch = mysql_query("SELECT * FROM extras WHERE dato='manejoAgencia' ORDER BY id ASC");
	while ($switch = mysql_fetch_array($estadoSwitch)) {
		if ($switch['valor'] == "true"){ // Si lo está manejando la agencia
			$update_switch = mysql_query("UPDATE extras SET valor='false' WHERE dato='manejoAgencia'"); 
			array_push($array_respuesta, "false");
		}else{// Si lo está manejando el conductor
			$update_switch = mysql_query("UPDATE extras SET valor='true' WHERE dato='manejoAgencia'"); 
			array_push($array_respuesta, "true");
		}
	}



	mysqli_close($con);
	echo json_encode($array_respuesta);// Enviamos el array de respuesta en forma de json
?>