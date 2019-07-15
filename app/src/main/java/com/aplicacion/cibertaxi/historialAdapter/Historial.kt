package com.aplicacion.cibertaxi.historialAdapter


class Historial {
    var tipo: String = ""
    var conductor_usuario: String = ""
    var origen: String = ""
    var destino: String = ""
    var fecha: String = ""
    var estado: String = ""

    constructor() {}

    constructor(tipo: String, conductor_usuario: String, origen: String, destino: String, fecha: String, estado: String) {
        this.tipo = tipo
        this.conductor_usuario = conductor_usuario
        this.origen = origen
        this.destino = destino
        this.fecha = fecha
        this.estado = estado
    }
}