<?php
	include ("../BBDD/config.php"); // Importamos los datos para realizamos la conexión a la base de datos
	date_default_timezone_set('America/Argentina/Buenos_Aires'); // Seleccionamos la zona horaria

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO"); // Realizamos la conexión
	mysql_select_db($db_name); // Seleccionamos la base de datos
	

	$idconductor = $_GET['idconductor']; // Id conductor enviado con get
	
	
	$consulta = "DELETE FROM localizacionConductor WHERE idconductor='$idconductor'"; 
    $query = mysql_query($consulta) or die (mysql_error()); 

	


	// Enviamos un json para que no de error
	$array_c=array(
		"status" => true
	);
 
	mysqli_close($con); // Finalizamos conexion
	print_r(json_encode($array_c)); // Mostramos json de respuesta
?>