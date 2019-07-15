<?php

	// Conexion a la base de datos
	include ("../BBDD/config.php");
	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
		mysql_select_db($db_name);
	// Fin Conexion

	$idusuario = $_GET['idusuario'];
	$estado = 0;
    // EL ID DE USUARIO '0' ESTA RESERVADO Y NO PUEDE USARSE!!!!



	$resultadoViajes=mysqli_query($con, "SELECT * FROM geolocalizaciones WHERE idusuario='$idusuario'");
	// Existe un viaje en `geolocalizaciones`
	if ($resultadoViajes->num_rows > 0)
	{

		// Vemos si el usuario tiene un auto asignado, si lo tiene entonces tomamos el id del conductor para saber su locacalizacion
		$resultadoViajes=mysqli_query($con, "SELECT * FROM geolocalizaciones WHERE idusuario='$idusuario' AND idconductor!='' LIMIT 1");
		if ($resultadoViajes->num_rows > 0)
		{
			// El conductor aceptó el viaje
			$sql_conductor = mysql_query("SELECT * FROM geolocalizaciones WHERE idusuario='$idusuario' AND idconductor!='' LIMIT 1");
			while ($conductor = mysql_fetch_array($sql_conductor)) {
				$estado = $conductor['idconductor'];
			}
			
		}else{
		}


		/* El usuario ya pidió un viaje */
		$geolocalizacion_arr=array(
            "status" => true,
            "mensaje" => $estado."" // Si mensaje = 0 > NO HAY AUTO ASIGNADO  |  >>  | Si mensaje != 0 > CONDUCTOR ACEPTÓ EL VIAJE Y EL ID DEL CONDUCTOR SE ENVIA EN MENSAJE
        );
	} 
	else {
		// El usuario no pidió un auto todavía
            $geolocalizacion_arr=array(
                "status" => false
            );
	}
 
	mysqli_close($con); // Cerramos conexion
	print_r(json_encode($geolocalizacion_arr)); // Enviamos json de respuesta
?>