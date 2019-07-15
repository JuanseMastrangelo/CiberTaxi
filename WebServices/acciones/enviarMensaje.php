<?php

	// Conexion a la base de datos
	include ("../BBDD/config.php"); // Importamos los datos de conexion
	date_default_timezone_set('America/Argentina/Buenos_Aires'); // Setamos la zona horaria

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	mysql_select_db($db_name);
	// Fin Conexion

	// Variables
	$idusuario = $_GET['idusuario'];
	$tipo = $_GET['tipo'];
    $mensaje = $_GET['mensaje'];
	$fecha = date('Y-m-d H:i:s'); // Fecha actual
	
	$array_de_respuesta=array(
		"status" => false
	);

	if($tipo == "usuario"){
		// El mensaje lo envia un usuario
		$peticion = mysql_query("SELECT * FROM geolocalizaciones WHERE idusuario='$idusuario' LIMIT 1");
		while ($array_res = mysql_fetch_array($peticion)) {
			// Insertamos el mensaje en la base de datos
			$insert_value = mysql_query('INSERT INTO `' . $db_name . '`.`mensajes` (`iddestino`, `idorigen`, `fecha`, `mensaje`,`visto`) VALUES ("' . $array_res['idconductor'] . '", "' . $idusuario . '", "' . $fecha . '", "' . $mensaje . '", "no")');
			$array_de_respuesta=array(
				"status" => true
			);
		}
	}else{
		// El mensaje lo envia un conductor
		$peticion = mysql_query("SELECT * FROM geolocalizaciones WHERE idconductor='$idusuario' LIMIT 1");
		while ($array_res = mysql_fetch_array($peticion)) {
			// Insertamos el mensaje en la base de datos
			$insert_value = mysql_query('INSERT INTO `' . $db_name . '`.`mensajes` (`iddestino`, `idorigen`, `fecha`, `mensaje`,`visto`) VALUES ("' . $array_res['idusuario'] . '", "' . $idusuario . '", "' . $fecha . '", "' . $mensaje . '", "no")');

			$array_de_respuesta=array(
				"status" => true
			);
			
		}
	}
	
	

	
	
 
	mysqli_close($con);
	print_r(json_encode($array_de_respuesta));
?>