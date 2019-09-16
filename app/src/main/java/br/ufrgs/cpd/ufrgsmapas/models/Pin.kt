package br.ufrgs.cpd.ufrgsmapas.models

 class Pin(val latitude: Double,
           val longitude: Double,
           var buildings: ArrayList<Building>,
           val id: Int)