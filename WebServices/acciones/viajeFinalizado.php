<?php
	include ("../BBDD/config.php"); // Importamos los datos para realizamos la conexión a la base de datos
	date_default_timezone_set('America/Argentina/Buenos_Aires'); // Seleccionamos la zona horaria

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO"); // Realizamos la conexión
	mysql_select_db($db_name);
	

	$idconductor = $_GET['idconductor']; // Id conductor enviado con get
	$latF = $_GET['lat']; // Latitud Final
	$lonF = $_GET['lon']; // Longitud Final
	$mensaje = $_GET['mensaje']; // Mensaje de la razon de la finalizacion
    $fecha = date('Y-m-d H:i:s'); // fecha
	
	
	$peticiones = mysql_query("SELECT * FROM geolocalizaciones WHERE idconductor='$idconductor'"); // Tomamos los valores de inicio de la table geolocalizacion
	while ($viaje = mysql_fetch_array($peticiones)) {
		// Insertamos los datos del conductor y fecha para saber cuanto tardó
		$posInicial = $viaje['lat']."|".$viaje['lon'];
		$posFinal = $latF."|".$lonF;
		$partida = $viaje['fecha'];
		$idusuario = $viaje['idusuario'];
		echo $idconductor;

		$insertar_dato = 'INSERT INTO `' . $db_name . '`.`controlViajes` (`idconductor`, `llegada`, `partida`, `idusuario`, `posInicial`, `posFinal`, `mensaje`) VALUES ("' . $idconductor . '","'.$fecha.'","'.$partida.'","'.$idusuario.'","'.$posInicial.'","'.$posFinal.'","'.$mensaje.'")'; // query para insertar datos en la base de datos `controlViajes`
	

		mysqli_select_db($con, $db_name); // Seleccionamos la base de datos
		mysql_query($insertar_dato); // Ejecutamos la query
	}
	$consulta = "DELETE FROM mensajes WHERE iddestino='$idconductor' OR idorigen='$idconductor'"; 
    $query = mysql_query($consulta) or die (mysql_error()); 

	


	// Enviamos un json para que no de error
	$array_c=array(
		"status" => true
	);
 
	mysqli_close($con); // Finalizamos conexion
	print_r(json_encode($array_c)); // Mostramos json de respuesta
?>