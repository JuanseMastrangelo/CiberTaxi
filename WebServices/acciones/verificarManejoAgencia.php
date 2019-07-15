<?php
	// Conexion a la base de datos
	include ("../BBDD/config.php"); // Importamos los datos de conexion
	date_default_timezone_set('America/Argentina/Buenos_Aires'); // Setamos la zona horaria

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	mysql_select_db($db_name);
	// Fin Conexion




	$peticion = mysql_query("SELECT * FROM extras WHERE dato='manejoAgencia' LIMIT 1");
	while ($array_res = mysql_fetch_array($peticion)) {
		$res_arr=array("status" => $array_res['valor']);
	}

 
	mysqli_close($con);
	print_r(json_encode($res_arr));
?>