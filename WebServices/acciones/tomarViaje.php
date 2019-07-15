<?php
	//ALGORITMO PARA TOMAR IDUSUARIO Y DATOS DE USUARIO DEL MARCADOR QUE EL CONDUCTOR SELECCIONÓ

	// CONEXIÓN A LA BASE DE DATOS
	include ("../BBDD/config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');
	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}
	// FIN CONEXIÓN

	// VARIABLES GET DE URL
	$idusuario = $_GET['idusuario'];
	$idconductor = $_GET['idconductor'];
    


	// SQL A BASE DE DATO PARA AVERIGUAR SI EXISTEN LOS PARAMETROS INDICADOS
	$resultadoViajes=mysqli_query($con, "SELECT * FROM geolocalizaciones WHERE idusuario='$idusuario' AND idconductor=''");

	// SI EXISTE =>
	if ($resultadoViajes->num_rows > 0)
	{
		
		mysql_select_db($db_name);
		$sql_viajes = "UPDATE geolocalizaciones SET idconductor='$idconductor' WHERE idusuario='$idusuario' AND idconductor=''"; 
	    $query_sql_viajes = mysql_query($sql_viajes); 
		$geolocalizacion_arr=array(
			"status" => true,
			"mensaje" => "Viaje tomado con éxito"
		);
	} 
	// SI NO EXISTEN =>
	else {
            // DEVOLVEMOS JSON CON RESULTADO `FALSE` ( ERROR )
            $geolocalizacion_arr=array(
                "status" => false,
                "mensaje" => "error"
            );
            
		
	}

	// CERRAMOS CONEXIÓN CON LA BASE DE DATOS
	mysqli_close($con);
	// ENVIAMOS JSON DE RESPUESTA
	print_r(json_encode($geolocalizacion_arr));
?>