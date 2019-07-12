<?php
	//ALGORITMO PARA TOMAR IDUSUARIO Y DATOS DE USUARIO DEL MARCADOR QUE EL CONDUCTOR SELECCIONÓ

	// CONEXIÓN A LA BASE DE DATOS
	include ("../BBDD/config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');
	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	mysql_select_db($db_name);
	// FIN CONEXIÓN

	// VARIABLES GET DE URL
	$idconductor = $_GET['idconductor'];
	$lat = $_GET['lat'];
	$lon = $_GET['lon'];
    


	// SQL A BASE DE DATO PARA AVERIGUAR SI EXISTEN LOS PARAMETROS INDICADOS
	$resultadoViajes=mysqli_query($con, "SELECT * FROM localizacionConductor WHERE idconductor='$idconductor'");

	// SI EXISTE =>
	if ($resultadoViajes->num_rows > 0)
	{
		$sql = mysql_query("UPDATE localizacionConductor SET lat='$lat',lon='$lon' WHERE idconductor='$idconductor'"); 
		
	} 
	// SI NO EXISTEN =>
	else {
		$insert_value = mysql_query('INSERT INTO `' . $db_name . '`.`localizacionConductor` (`idconductor`, `lat`, `lon`) VALUES ("' . $idconductor . '", "' . $lat . '", "' . $lon . '")');
	}


	$resultadoMov=mysqli_query($con, "SELECT * FROM movimientosConductor WHERE idconductor='$idconductor'");
	if($resultadoMov->num_rows > 0){
		// Si ya existen coordenadas
		$peticion = mysql_query("SELECT * FROM movimientosConductor WHERE idconductor='$idconductor' ORDER BY id DESC LIMIT 1");
		while ($obj_mov = mysql_fetch_array($peticion)) {

			// Este algoritmo toma los decimales de cada localizacion y los resta para tomar la diferencia que hay entre los dos. Así sabemos si el vehículo se mueve o no; respetando el margen de error del gps
			$restaLat = abs($obj_mov['lat']) - abs($lat);
			$restaLon =  $obj_mov['lon'] - $lon;
			$latitudActual=abs(($obj_mov['lat']-intval($obj_mov['lat']))*10000000);
			$longitudActual=abs(($obj_mov['lon']-intval($obj_mov['lon']))*10000000);
			$latitudGET=abs(($lat-intval($lat))*10000000);
			$longitudGET=abs(($lon-intval($lon))*10000000);
			// Resta
			$restaLat = $latitudActual - $latitudGET;
			$restaLon = $longitudActual - $longitudGET;


			if(round(abs($restaLat)) > 200 OR round(abs($restaLon)) > 200){ // 200 margen de error del gps
				// Insertamos los movimientos del conductor
				$insertarMovimientos = mysql_query('INSERT INTO `' . $db_name . '`.`movimientosConductor` (`idconductor`, `lat`, `lon`) VALUES ("' . $idconductor . '", "' . $lat . '", "' . $lon . '")');
				$sql = mysql_query("UPDATE usuarios SET estado='movimiento' WHERE id='$idconductor'"); 
			}else{
				// El auto está parado
				$sql = mysql_query("UPDATE usuarios SET estado='parado' WHERE id='$idconductor'"); 
			}
	    }

    }else{
    	// Creamos una nueva fila para el conductor
    	$insertarMovimientos = mysql_query('INSERT INTO `' . $db_name . '`.`movimientosConductor` (`idconductor`, `lat`, `lon`) VALUES ("' . $idconductor . '", "' . $lat . '", "' . $lon . '")');
    }

	$geolocalizacion_arr=array("status" => true);
	// CERRAMOS CONEXIÓN CON LA BASE DE DATOS
	mysqli_close($con);
	// ENVIAMOS JSON DE RESPUESTA
	print_r(json_encode($geolocalizacion_arr));
?>