<? 
	// Conexión a la base de Datos
	include("config.php"); // Incluimos los datos de conexion
	$conn= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO"); // Creamos la conexion
	mysql_select_db($db_name); // Seleccionamos la base de datos MYSQL°


	$array_respuesta = array(); // Inicializamos el array[] que va a responder
	$sql_localizacionConductores = mysql_query("SELECT * FROM localizacionConductor");
	while ($localizacionConductores = mysql_fetch_array($sql_localizacionConductores)) {
		$sql_conductor = mysql_query("SELECT * FROM usuarios WHERE id='$localizacionConductores[idconductor]' LIMIT 1");
		while ($Conductor = mysql_fetch_array($sql_conductor)) {

			$imgEstado="imagenes/taxi.png";
			// Vemos el estado del vehículo
			if($Conductor['estado'] == 'parado')
				$imgEstado="imagenes/taxiFrenado.png";


			//Vemos si el conductor está con un pasajero
			$sql_conductorPasajero = mysql_query("SELECT * FROM geolocalizaciones WHERE idconductor='$Conductor[id]' LIMIT 1");
			if(mysql_num_rows($sql_conductorPasajero) > 0) // Está con un pasajero
				$imgEstado="imagenes/taxiOcupado.png";




		    array_push($array_respuesta, $localizacionConductores['lat']."|".$localizacionConductores['lon']."|".$imgEstado."|".$Conductor['usuario']);
		    // ARRAY => lat|lon|icono|nombreConductor
		}
	}
	echo json_encode($array_respuesta);// Enviamos el array de respuesta en forma de json
?>