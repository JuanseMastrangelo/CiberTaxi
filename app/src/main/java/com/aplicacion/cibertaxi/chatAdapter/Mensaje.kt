package com.aplicacion.cibertaxi.chatAdapter


class Mensaje {
    var origen: String = ""
    var destino: String = ""
    var mensaje: String = ""
    var idusuario: String = ""

    constructor() {}

    constructor(origen: String, destino: String, mensaje: String, idusuario: String) {
        this.origen = origen
        this.destino = destino
        this.mensaje = mensaje
        this.idusuario = idusuario
    }
}