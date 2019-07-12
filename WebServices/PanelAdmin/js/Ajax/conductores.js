// Este script es todo lo vinculado a los conductores, ACTUALIZA la lista de la derecha y los geolocaliza cargandolo en el mapa




// GeoLocalizar e Insertar en el mapa
var div = "ajax_operacionPasajeros";
var urloperacionPasajeros = "BBDD/Ajax/ajax_operacionPasajeros.php";


    function divAjaxPasajeros(nombrePasajero, accion, tipo){
            var xmlHttp;
            try{ xmlHttp=new XMLHttpRequest();}catch (e){try{ xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");}catch (e){try{xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");}catch (e){alert("Tu explorador no soporta AJAX.");return false;}}}
            var timestamp = parseInt(new Date().getTime().toString().substring(0, 10));
            var nocacheurl = urloperacionPasajeros+"?t="+timestamp;

            // Lo enviamos
            xmlHttp.open("POST",nocacheurl,true);
            xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
            xmlHttp.onreadystatechange=function(){
                if(xmlHttp.readyState== 4 && xmlHttp.readyState != null){
                    document.getElementById(divoperacionPasajeros).innerHTML=xmlHttp.responseText;
                }
            }
            xmlHttp.send("cantidad="+cant_pasajeros+"&nombresPasajeros="+JSON.stringify(arrayPasajero));  
    }