<?php
	// Este archivo crea un nuevo viaje pero con usuario falso

	include ("../BBDD/config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}

	$idconductor = $_GET['idconductor'];
    $lat = $_GET['lat'];
    $lon = $_GET['lon'];
    $fecha = date('Y-m-d H:i:s');
    

    $idusuario = rand().$idconductor; // Crea un usuario random y único

	$resultadoViajes=mysqli_query($con, "SELECT * FROM geolocalizaciones WHERE idconductor='$idconductor'");

	if ($resultadoViajes->num_rows > 0)
	{ // Verificamos que el conductor no tenga viajes

		$geolocalizacion_arr=array(
            "estado" => false
        );
		
	} 
	else {
            // Creamos el viaje
			$insert_value = 'INSERT INTO `' . $db_name . '`.`geolocalizaciones` (`idusuario`, `lat`, `lon`, `idconductor`,`fecha`) VALUES ("' . $idusuario . '", "' . $lat . '", "' . $lon . '", "' . $idconductor . '", "'.$fecha.'")';
            mysqli_select_db($con, $db_name);
            $retry_value = mysql_query($insert_value);

            $geolocalizacion_arr=array(
                "estado" => true
            );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($geolocalizacion_arr));
?>