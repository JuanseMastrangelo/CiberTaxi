<?php

	// Conexion a la base de datos
	include ("../BBDD/config.php"); // Importamos los datos de conexion
	date_default_timezone_set('America/Argentina/Buenos_Aires'); // Setamos la zona horaria

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	mysql_select_db($db_name);
	// Fin Conexion

	

	// El mensaje lo envia un usuario
	$cantPublicidad = mysql_query("SELECT * FROM publicidad");
	$cantFilas = mysql_num_rows($cantPublicidad); // Contamos la cantidad de filas = publicidad


	$idPublicidad = rand(1,$cantFilas); // Seleccionamos una publicidad al azar

	$peticion = mysql_query("SELECT * FROM publicidad WHERE id='$idPublicidad'");
	while ($array_res = mysql_fetch_array($peticion)) {
		// Insertamos el mensaje en la base de datos
		$array_de_respuesta=array(
			"url" => $array_res['url']
		);
	}
	
	

	
	
 
	mysqli_close($con);
	print_r(json_encode($array_de_respuesta));
?>