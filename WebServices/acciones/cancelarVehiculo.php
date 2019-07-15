<?PHP
	include ("../BBDD/config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}

    
    
    if($_GET['idusuario']){
        $idusuario = $_GET['idusuario'];
        $resultadoViajes=mysqli_query($con, "SELECT * FROM geolocalizaciones WHERE idusuario='$idusuario'");
        if ($resultadoViajes->num_rows > 0){
            mysql_select_db($db_name) or die ("ERROR BASE DE DATOS");
            $consulta = "DELETE FROM geolocalizaciones WHERE idusuario='$idusuario'"; 
            $query = mysql_query($consulta) or die (mysql_error()); 

            
            $consulta_mensajes = "DELETE FROM mensajes WHERE iddestino='$idusuario' OR idorigen='$idusuario'"; 
            $query_mensajes = mysql_query($consulta_mensajes) or die (mysql_error()); 

            $geolocalizacion_arr=array(
                "status" => true,
                "mensaje" => "true"
            );
        }else {
            $geolocalizacion_arr=array(
                "status" => false,
                "mensaje" => "false"
            );
        }
    }else{
        // Si el viaje finalizó (Llamada del conductor)
        $idconductor = $_GET['idconductor'];
        $resultadoViajes=mysqli_query($con, "SELECT * FROM geolocalizaciones WHERE idconductor='$idconductor'");
        if ($resultadoViajes->num_rows > 0){
            mysql_select_db($db_name) or die ("ERROR BASE DE DATOS");
            $consulta = "DELETE FROM geolocalizaciones WHERE idconductor='$idconductor'"; 
            $query = mysql_query($consulta) or die (mysql_error()); 


            $geolocalizacion_arr=array(
                "status" => true,
                "mensaje" => "true"
            );
        }else {
            $geolocalizacion_arr=array(
                "status" => false,
                "mensaje" => "false"
            );
        }
    }
	mysql_close($con); 
	print_r(json_encode($geolocalizacion_arr));


?>
