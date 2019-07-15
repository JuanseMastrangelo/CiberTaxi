<?php
	include ("../BBDD/config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}

	$idusuario = $_GET['idusuario'];
    $lat = $_GET['lat'];
    $lon = $_GET['lon'];
    $idconductor = $_GET['idconductor'];
    $fecha = date('Y-m-d H:i:s');
    



	$resultadoViajes=mysqli_query($con, "SELECT * FROM geolocalizaciones WHERE idusuario='$idusuario'");

	if ($resultadoViajes->num_rows > 0)
	{
		/* El usuario ya pidió un viaje */
		$geolocalizacion_arr=array(
            "status" => false,
            "mensaje" => "El usuario ya pidio un automovil"
        );
	} 
	else {
            /* Guardamos un viaje en la base de datos */
			$insert_value = 'INSERT INTO `' . $db_name . '`.`geolocalizaciones` (`idusuario`, `lat`, `lon`, `idconductor`,`fecha`) VALUES ("' . $idusuario . '", "' . $lat . '", "' . $lon . '", "' . $idconductor . '", "'.$fecha.'")';
            mysqli_select_db($con, $db_name);
            
			$retry_value = mysql_query($insert_value);
			if (!$retry_value) {
			   die('Error: ' . mysql_error());
            }
            
            $geolocalizacion_arr=array(
                "status" => true,
                "mensaje" => "Automovil pedido",
                "idusuario" => $idusuario,
                "idconductor" => $idconductor
            );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($geolocalizacion_arr));
?>