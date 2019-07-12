<?php
	//ALGORITMO PARA TOMAR IDUSUARIO Y DATOS DE USUARIO DEL MARCADOR QUE EL CONDUCTOR SELECCIONÓ

	// CONEXIÓN A LA BASE DE DATOS
	include ("../BBDD/config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');
	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	// FIN CONEXIÓN

	// VARIABLES GET DE URL
	$idusuario = $_GET['id'];
	$apellido = $_GET['apellido'];
	$telefono = $_GET['telefono'];
    


	// SQL A BASE DE DATO PARA AVERIGUAR SI EXISTEN LOS PARAMETROS INDICADOS
	mysql_select_db($db_name);
	$sql = mysql_query("UPDATE usuarios SET apellido='$apellido', telefono='$telefono' WHERE id='$idusuario'"); 
	$mensajes_arr=array(
		"status" => true
	);

	// CERRAMOS CONEXIÓN CON LA BASE DE DATOS
	mysqli_close($con);
	// ENVIAMOS JSON DE RESPUESTA
	print_r(json_encode($mensajes_arr));
?>