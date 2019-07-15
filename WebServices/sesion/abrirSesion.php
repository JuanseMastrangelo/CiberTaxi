<?php

    // Conexion con la base de datos
	include ("../BBDD/config.php");

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}

    // variables GET
	$email = $_GET['email'];
	$pass = $_GET['pass'];
    



	$existeUsuario=mysqli_query($con, "SELECT * FROM usuarios WHERE mail='$email'");
    // Existe un usuario con ese email?
	if ($existeUsuario->num_rows > 0)
	{

        // Existe email
        $sql_usuario = "SELECT * FROM usuarios WHERE mail='$email' LIMIT 1";
	    $res_usuario = mysqli_query($con,$sql_usuario) or die (mysqli_error());
        $usuario = mysqli_fetch_array($res_usuario);
            // Des- encriptamos la contraseña para saber si es correcta
            if(password_verify($pass, $usuario['pass'])){
                // Correcta
                $sesion_array=array(
                    "status" => true,
                    "idusuario" => $usuario['id'],
                    "nombre" => $usuario['usuario'],
                    "email" => $usuario['mail'],
                    "reputacion" => $usuario['reputacion'],
                    "puesto" => $usuario['puesto']
                );
            }else{
                // Incorrecta
                $sesion_array=array(
                    "status" => false,
                    "idusuario" => "La contraseña no coincide"
                );
            }
            
        

	} 
	else {
            // El email y contraseña no coincide con ninguna cuenta en la base de datos
            $sesion_array=array(
                "status" => false,
                "mensaje" => "El email no existe"
            );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($sesion_array));
?>