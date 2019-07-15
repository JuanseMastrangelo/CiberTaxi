<? 
	// Conexión a la base de Datos
	include("config.php"); // Incluimos los datos de conexion
	$conn= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO"); // Creamos la conexion
	mysql_select_db($db_name); // Seleccionamos la base de datos MYSQL°


	$array_respuesta = array(); // Inicializamos el array[] que va a responder
	$sql_localizacionCliente = mysql_query("SELECT * FROM geolocalizaciones WHERE idconductor=''");
	while ($localizacionCliente = mysql_fetch_array($sql_localizacionCliente)) {
		$sql_cliente = mysql_query("SELECT * FROM usuarios WHERE id='$localizacionCliente[idusuario]' LIMIT 1");
		while ($Cliente = mysql_fetch_array($sql_cliente)) {
		    array_push($array_respuesta, $localizacionCliente['lat']."|".$localizacionCliente['lon']."|imagenes/user.png|".$Cliente['usuario']."|".$localizacionCliente['id']);
		    // ARRAY => lat|lon|icono|nombreConductor|idOperacion
		}
	}
	echo json_encode($array_respuesta);// Enviamos el array de respuesta en forma de json
?>