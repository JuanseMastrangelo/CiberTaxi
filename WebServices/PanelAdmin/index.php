<?
	// Conexión a la base de Datos
	include("BBDD/config.php"); // Incluimos los datos de conexion
	session_start(); // Variable sesion
	$conn= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO"); // Creamos la conexion
	mysql_select_db($db_name); // Seleccionamos la base de datos MYSQL°

	if(!$_SESSION){
		header("location:login.php");
	}
?>


<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Panel de Administración - CiberTaxi</title>


    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet"  type="text/css" />
    <link href="https://fonts.googleapis.com/css?family=PT+Sans&display=swap" rel="stylesheet">

    <link rel="stylesheet" type="text/css" href="css/main.css">
 	<!-- Notificaciones -->
	<script type="text/javascript" src="js/notifIt.js"></script>
	<link rel="stylesheet" type="text/css" href="css/notifIt.css">


    <style type="text/css">
		#map {
		    height: 100%;
		  }
		html, body {
	        height: 100%;
	        margin: 0;
	        padding: 0;
	    }
    </style>
</head>
<body>
    <div id="map"></div>
	<div class="rightMenu">
		<div class="listaConductores" id="scrollbarEstilo">
			<p><i class="fa fa-circle"></i> Lista Conductores: 
			<label class="switch">
				<input type="checkbox" id="btn_switch" <?$estadoSwitch = mysql_query("SELECT * FROM extras WHERE dato='manejoAgencia' ORDER BY id ASC");while ($switch = mysql_fetch_array($estadoSwitch)) {if ($switch['valor'] == "true"){?>checked<?}}?>>
				<span class="slider round"></span>
			</label>
			</p>
			<div id="listaConductores"></div>
		</div>


		<div class="informacionViaje">
			<div id="asignarConductor"></div>
		</div>
	</div>





	<script src="https://code.jquery.com/jquery-1.9.1.js"></script>
	<script type="text/javascript">
	var manejoAgencia = <?$estadoSwitch = mysql_query("SELECT * FROM extras WHERE dato='manejoAgencia' ORDER BY id ASC");while ($switch = mysql_fetch_array($estadoSwitch)) {if ($switch['valor'] == "true"){?>true<?}else{?>false<?}}?>;
	$("#btn_switch").click(function (){
		if(manejoAgencia == true){

			notif({
				msg: "Automático",
				type: "info",
				timeout: 2000
			});
			manejoAgencia = false;

		}else{

			notif({
				msg: "Manual",
				type: "info",
				timeout: 2000
			});
			manejoAgencia = true;

		}
		switchM();
	});

	</script>

    <script>
      	var map; // Inicializamos el objeto mapa
      	var markersArray = []; // Aca insertaremos todos los marcadores para tener mayor control
      	var position;
      	function tomarPosicionAgencia() {
      		/* Comprobamos que tengamos permisos para ver la ubicación actual de la agencia */
			if (navigator.geolocation) {
				navigator.geolocation.getCurrentPosition(crearMapa);
				//crearMapa();
		  	} else {
		    	x.innerHTML = "Geolocation is not supported by this browser.";
		  	}
		}
		function crearMapa(posicion) {
		//function crearMapa() {
			/* Crea el mapa y lo situa en la agencia */
	        position = posicion;
			map = new google.maps.Map(document.getElementById('map'), {
	          center: {lat: position.coords.latitude, lng: position.coords.longitude},
	          //center: {lat: -38.9568149, lng: -67.999895},
	          zoom: 15
	        });
	        reloj(); // Funcion encargada de actualizar el mapa

		}
		//function marcadorAgencia(position){
		function marcadorAgencia(){
			/* Agrega el marcador en la agencia */

			agregarMarcador(position.coords.latitude, position.coords.longitude, "imagenes/support.png");
			//agregarMarcador(-38.9568149, -67.999895, "imagenes/support.png", "Agencia", -1);
		}	

		function agregarMarcador(latitud, longitud, icono, titulo, idOperacion) {
			marker = new google.maps.Marker({
		    	position: {lat: latitud, lng: longitud},
				icon: icono,
				draggable: false,
		    	map: map
		  	});
		  	markersArray.push(marker); // Agrega el marcador al array de marcadores

		  	var infowindow = new google.maps.InfoWindow({ // Crea un adaptador para los infoWindows
                content: '<div id="content"><b>'+titulo+'</b></div>'
            });

            i = 0;
		  	google.maps.event.addListener(marker, 'click', (function(marker, i) { // Listener de InfoWindows
		        return function() {
		        	if(idOperacion != -1){
		        		asignarConductor(idOperacion)
		        	}
		            infowindow.open(map, marker);
		        }
		    })(marker, i));
		  	
		}	
		function limpiarMapa() {
			// Borra todos los marcadores del mapa
		  	if (markersArray) {
		    	for (i in markersArray) {
		      		markersArray[i].setMap(null);
		    	}
		  	}
		}
    </script>


	<script type="text/javascript">
	// GeoLocalizar e Insertar en el mapa a los conductores

	function switchM(){
		// Cambiamos la forma de asignar los viajes
		$.ajax({
        type: "GET",
        url: "BBDD/switch.php",             
        dataType: 'json',
        success: function(response){}})
	}


	function actualizarMapaConductores(){
		// Llamamos a todos los conductores disponibles y su localización actual, lo insertamos en el mapa con un marcador
		$.ajax({
        type: "GET",
        url: "BBDD/locConductores.php",             
        dataType: 'json',
        success: function(response){
        	for (var i = 0; i < response.length; i++) {
        		var splitArray = response[i].split("|");
        		agregarMarcador(parseFloat(splitArray[0]), parseFloat(splitArray[1]), splitArray[2], splitArray[3], -1);
        	};
        }})
	}

	function actualizarMapaClientes(){
		// Llamamos a todos los conductores disponibles y su localización actual, lo insertamos en el mapa con un marcador
		$.ajax({
        type: "GET",
        url: "BBDD/locClientes.php",             
        dataType: 'json',
        success: function(response){
        	for (var i = 0; i < response.length; i++) {
        		var splitArray = response[i].split("|");
        		agregarMarcador(parseFloat(splitArray[0]), parseFloat(splitArray[1]), splitArray[2], splitArray[3], splitArray[4]);
        	};
        }})
	}

	function reloj(){
		limpiarMapa();
		marcadorAgencia(position);
	    //marcadorAgencia(); // Crea el marcador de la agencia
		actualizarMapaConductores(); // Agregamos los conductores
		actualizarMapaClientes(); // Agregamos los clientes
		listaConductores(); // Actualiza la lista de conductores y su estado
		setTimeout("reloj()",10*1000); // En 10 segundos repetimos la funcion
	}



	function asignarConductor(id){
		// Actualiza la lista de la derecha al clickear un cliente para asignar viaje
        var divAsignarConductor = "asignarConductor";
        var urlAsignarConductor = "BBDD/Ajax/asignarConductor.php";
        var xmlHttp;
        try{ xmlHttp=new XMLHttpRequest();}catch (e){try{ xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");}catch (e){try{xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");}catch (e){alert("Tu explorador no soporta AJAX.");return false;}}}
        var timestamp = parseInt(new Date().getTime().toString().substring(0, 10));
        var nocacheurl = urlAsignarConductor+"?t="+timestamp;
        xmlHttp.onreadystatechange=function(){if(xmlHttp.readyState== 4 && xmlHttp.readyState != null){document.getElementById(divAsignarConductor).innerHTML=xmlHttp.responseText;}}
        xmlHttp.open("POST",nocacheurl,true);
        xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xmlHttp.send("id="+id); // Enviamos el id de la operación
	}

	function accionAsignarConductor(idOperacion){
		// Se ejecuta cuando el usuario clickea el boton "Asignar Viaje"
		if($("#idconductor").val() != ''){ // Si el campo 'conductor' no esta vacio
			$.ajax({
	        type: "GET",
	        url: "BBDD/asignarConductor.php?idConductor="+$("#idconductor").val()+"&idOperacion="+idOperacion,             
	        dataType: 'json',
	        success: function(response){
	        	notif({
					msg: "Viaje asignado correctamente",
					type: "info",
					timeout: 2000
				});
	        	asignarConductor(-1); // Borramos la info de la derecha enviando un id '-1' que no existe
	        	reloj(); // Actualizamos el mapa y lista de conductores
	        }})
		}
	}

	function listaConductores(){
		// Lista de conductores actualizada
        var divConductores = "listaConductores";
        var urlConductores = "BBDD/Ajax/listaConductores.php";
        var xmlHttp;
        try{ xmlHttp=new XMLHttpRequest();}catch (e){try{ xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");}catch (e){try{xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");}catch (e){alert("Tu explorador no soporta AJAX.");return false;}}}
        var timestamp = parseInt(new Date().getTime().toString().substring(0, 10));
        var nocacheurl = urlConductores+"?t="+timestamp;
        xmlHttp.onreadystatechange=function(){if(xmlHttp.readyState== 4 && xmlHttp.readyState != null){document.getElementById(divConductores).innerHTML=xmlHttp.responseText;}}
        xmlHttp.open("POST",nocacheurl,true);
        xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xmlHttp.send("id="); // Enviamos vacio
	}

    </script>








    <script src="https://maps.googleapis.com/maps/api/js?key=KEY_AQUÍ&callback=tomarPosicionAgencia"
    async defer></script>
</body>
</html>