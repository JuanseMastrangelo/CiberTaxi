<?php
	// Conexion a la base de datos
	include ("../BBDD/config.php"); // Importamos los datos de conexion
	date_default_timezone_set('America/Argentina/Buenos_Aires'); // Setamos la zona horaria

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
	mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	mysql_select_db($db_name);
	// Fin Conexion

	$idusuario = $_GET['id'];
    



	$resultado=mysqli_query($con, "SELECT * FROM usuarios WHERE id='$idusuario'");

	if ($resultado->num_rows > 0)
	{
            $peticion = mysql_query("SELECT * FROM usuarios WHERE id='$idusuario' LIMIT 1");
			while ($array_res = mysql_fetch_array($peticion)) {

				$usuario_arr=array(
	                "status" => true,
	                "nombre" => $array_res['usuario'],
	                "apellido" => $array_res['apellido'],
	                "telefono" => $array_res['telefono']
	            );

			}
	} 
	else {
	            
            
		// No se ha encontrado al usuario
		$ususario_arr=array(
            "status" => false
        );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($usuario_arr));
?>