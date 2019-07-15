<?php
	include ("../BBDD/config.php");
	date_default_timezone_set('America/Argentina/Buenos_Aires');

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}
	mysql_select_db($db_name);

    $idusuario = $_GET['idusuario'];
    $mensaje = $_GET['mensaje'];
    


	$resultadoViajes=mysqli_query($con, "SELECT * FROM mensajes WHERE iddestino='$idusuario' or idorigen='$idusuario'");

	if ($resultadoViajes->num_rows > 0)
	{
        $mensajes_array = array();
        $peticion = mysql_query("SELECT * FROM mensajes WHERE iddestino='$idusuario' or idorigen='$idusuario'");
		while ($obj_mensaje = mysql_fetch_array($peticion)) {
			array_push($mensajes_array, $obj_mensaje['iddestino']."|".$obj_mensaje['idorigen']."|".$obj_mensaje['fecha']."|".$obj_mensaje['mensaje']."|".$obj_mensaje['visto']);
        }
		/* Enviamos los mensajes usando array y json */
		$mensajes_arr=array(
            "status" => true,
            "mensajes_array" => $mensajes_array
        );
	} 
	else {
            // No hay un conexión cliente-conductor
            $mensajes_arr=array(
                "status" => false,
                "mensaje" => "false"
            );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($mensajes_arr));
?>