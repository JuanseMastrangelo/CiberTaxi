<?php

    // Conexion con la base de datos
	include ("../BBDD/config.php");

	$con= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO");
		mysqli_select_db($con,$db_name) or die ("ERROR BASE DE DATOS");
	if (!$con) {
		die('No se ha podido conectar a la base de datos');
	}

    // variables GET
	$nombreGet = $_GET['nombre'];
	$nombre = str_replace("-", " ", $nombreGet);
	$email = $_GET['email'];
    $pass = $_GET['pass'];
    $fecha = date('Y-m-d H:i:s');
	$passHash = password_hash($pass, PASSWORD_BCRYPT);
    



	$existeUsuario=mysqli_query($con, "SELECT * FROM usuarios WHERE mail='$email'");
    // Existe un usuario con ese email?
	if ($existeUsuario->num_rows > 0)
	{
        // El email y contraseña no coincide con ninguna cuenta en la base de datos
        $sesion_array=array(
            "status" => false,
            "mensaje" => "El email ya existe"
        );
	} 
	else {
        // Creamos el usuario
        $sql_usuario = 'INSERT INTO `' . $db_name . '`.`usuarios` (`usuario`, `mail`, `pass`, `fecha`, `reputacion`, `puesto`) VALUES ("' . $nombre . '", "' . $email . '", "' . $passHash . '", "'.$fecha.'", "5", "usuario")';
        mysqli_select_db($con, $db_name);
        
        $retry_value = mysql_query($sql_usuario);

        // El usuario fué creado, enviamos los datos de este mediante ajax para iniciar sesion en la aplicación

        $cons_usuario = "SELECT * FROM usuarios WHERE mail='$mail'";
        $resultado2 = mysqli_query($con,$cons_usuario) or die (mysqli_error());
        $usuario = mysqli_fetch_array($resultado2);
        $sesion_array=array(
            "status" => true,
            "mensaje" => "Usuario se ha creado con éxito",
            "idusuario" => $usuario['id'],
            "nombre" => $usuario['usuario'],
            "reputacion" => $usuario['reputacion'],
            "email" => $usuario['mail'],
            "puesto" => $usuario['puesto']
        );
            
		
	}
 
	mysqli_close($con);
	print_r(json_encode($sesion_array));
?>