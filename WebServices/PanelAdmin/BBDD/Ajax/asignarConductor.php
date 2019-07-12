<? 
	// Conexión a la base de Datos
	include("../config.php"); // Incluimos los datos de conexion
	$conn= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO"); // Creamos la conexion
	mysql_select_db($db_name); // Seleccionamos la base de datos MYSQL°


	$idOperacion = $_POST['id']; // Tomamos la id de Operacion enviada por AJAX
	$sql_viajes = mysql_query("SELECT * FROM geolocalizaciones WHERE id='$idOperacion' LIMIT 1");
	while ($Viaje = mysql_fetch_array($sql_viajes)) {
	$sql_usuario = mysql_query("SELECT * FROM usuarios WHERE id='$Viaje[idusuario]' LIMIT 1");
	while ($Usuario = mysql_fetch_array($sql_usuario)) {

?>
	<script src="https://code.jquery.com/jquery-1.9.1.js"></script>
	<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyATswYLlviN5LWh1a7dC68BfOVD_BAXHZU&callback=initMap"
    async defer></script>
	<script type="text/javascript">
		function initMap() {
		    codeLatLng();
		}
		function codeLatLng() {
			// Pasa de Coordenadas a Dirección fisica
		   var lat = parseFloat(<?echo $Viaje['lat'];?>);
		   var lng = parseFloat(<?echo $Viaje['lon'];?>);
		   var latlng = new google.maps.LatLng(lat, lng);
		   var geocoder = new google.maps.Geocoder();
		   geocoder.geocode({'latLng': latlng}, function(results, status) {
		      if (status == google.maps.GeocoderStatus.OK) {
		         if (results[0]) {
		         	$("#origen").html(results[0].formatted_address);
		         } else {
		            alert('No se han encontrado resultados');
		         }
		      } else {
		         alert('Geocoder falló: ' + status);
		      }
		   });
		}
	</script>
		

			<li>
				Fecha:<span> <?echo $Viaje['fecha'];?></span>
			</li>
			<li>
				Nombre:<span> <?echo $Usuario['usuario'];?></span>
			</li>
			<li id="origen">
				Origen:<span> Av. 25 de Mayo 1840, Buenos Aires, Argentina</span></i>
			</li>
			<li>
				Conductor:<span> <input type="number" id="idconductor" /></span></i>
			</li>
			<li style="text-align:right;">
				<button onClick="accionAsignarConductor(<?echo $idOperacion;?>);">Asignar viaje</button>
			</li>

<?}}?>