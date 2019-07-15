<? 
	// Conexión a la base de Datos
	include("../config.php"); // Incluimos los datos de conexion
	$conn= mysqli_connect($db_host, $db_user, $db_password) or die ("ERROR CONECTANDO"); // Creamos la conexion
	mysql_select_db($db_name); // Seleccionamos la base de datos MYSQL°
	mysqli_select_db($con,$db_name); // Seleccionamos la base de datos MYSQLi

?>
		

		
	<table id="conductores">
		<?
		$sql_localizacionConductor = mysql_query("SELECT * FROM localizacionConductor ORDER BY id ASC");
		while ($localizacionConductor = mysql_fetch_array($sql_localizacionConductor)) {

		$sql_conductor = mysql_query("SELECT * FROM usuarios WHERE id='$localizacionConductor[idconductor]' ORDER BY id ASC");
		while ($Conductor = mysql_fetch_array($sql_conductor)) {

		// Verificamos si el conductor tiene asignado un viaje
		$estadoConductor=mysql_query("SELECT * FROM geolocalizaciones WHERE idconductor='$Conductor[id]'");
		?>
		<tr>
		    <td id="conductoresNum"><center><span <?if (mysql_num_rows($estadoConductor) > 0){?>style="background-color: #F34927;"<?}?>><?echo $Conductor['id'];?></span></td>
		    <td><?echo $Conductor['usuario'];?></td>
		</tr>
		<?}}?>
	</table>
