<?php
	//ALGORITMO PARA TOMAR IDUSUARIO Y DATOS DE USUARIO DEL MARCADOR QUE EL CONDUCTOR SELECCIONÓ

	// CONEXIÓN A LA BASE DE DATOS
	include ("config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');
	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysql_select_db($db_name);
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}
	// FIN CONEXIÓN

	// VARIABLES GET DE URL
	$idOperacion = $_GET['idOperacion'];
	$idConductor = $_GET['idConductor'];
    $array_respuesta = array();


	// SQL A BASE DE DATO PARA AVERIGUAR SI EXISTEN LOS PARAMETROS INDICADOS
	$sql_viajes = mysql_query("UPDATE geolocalizaciones SET idconductor='$idConductor' WHERE id='$idOperacion'"); 
	array_push($array_respuesta, "success");
	mysqli_close($con);
	echo json_encode($array_respuesta);// Enviamos el array de respuesta en forma de json
?>