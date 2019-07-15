<?php

	// Conexion a la base de datos
	include ("../BBDD/config.php");
	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	mysql_select_db($db_name);

	// Gets
	$idconductor = $_GET['idconductor'];
    

	$resultadoViajes=mysqli_query($con, "SELECT * FROM localizacionConductor WHERE idconductor='$idconductor'");

	if ($resultadoViajes->num_rows > 0)
	{
		// Si el conductor está enviando geolocalizacion
        $sql_latLon = mysql_query("SELECT * FROM localizacionConductor WHERE idconductor='$idconductor'");
		while ($latLon = mysql_fetch_array($sql_latLon)) {
        
			// JSON CON LATITUD Y LONGITUD DEL CONDUCTOR
			$geolocalizacion_arr=array(
				"status" => true,
				"lat" => $latLon['lat'],
				"lon" => $latLon['lon']
			);
		}
	} 
	else {
            // El conductor no esta enviando la localizacion
            $geolocalizacion_arr=array(
                "status" => false
            );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($geolocalizacion_arr));
?>