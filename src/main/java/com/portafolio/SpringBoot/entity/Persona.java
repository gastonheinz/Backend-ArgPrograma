/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.portafolio.SpringBoot.entity;


import jakarta.validation.constraints.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private int id;
    
    @NotNull
    @Size(min = 1, max= 50, message = "no cumple con la longitud")
     private String nombre;
    
    
    @NotNull
    @Size(min = 1, max= 50, message = "no cumple con la longitud")
     private String apellido;
     
    @NotNull
    private String descripcion;
    
    @NotNull
     private String img;

    public Persona() {
    }

    public Persona(String nombre, String apellido, String descripcion, String img) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.descripcion = descripcion;
        this.img = img;
    }
    
    
    
}
