<?php
	include ("../BBDD/config.php");// Importamos los datos para realizar la conexion a la base de datos
	date_default_timezone_set('America/Argentina/Buenos_Aires'); // Seleccionamos la zona horaria

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");  // Realizamos la conexion
		
	mysqli_select_db($con,$db_name); // Seleccionamos la base de datos MYSQLI
	mysql_select_db($db_name); // Seleccionamos la base de datos MYSQL


	$idconductor = $_GET['idconductor']; // Id conductor

    



	$resultadoViajes=mysqli_query($con, "SELECT * FROM mensajes WHERE iddestino='$idconductor' AND visto='no'"); // Vemos si tenemos mensajes SIN LEER

	if ($resultadoViajes->num_rows > 0)
	{
		// Tenemos mensajes SIN LEER, lo guardamos en $array_mensajes
		$array_mensajes=array(
            "status" => true,
            "cantidad" => $resultadoViajes->num_rows
        );
	} 
	else {
            // No hay mensajes NO LEIDOS
            $geolocalizacion_arr=array(
                "status" => false,
                "mensaje" => "false"
            );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($array_mensajes));
?>