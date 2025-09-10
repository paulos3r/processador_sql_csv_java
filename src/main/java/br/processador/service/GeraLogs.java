/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.processador.service;

import br.processador.Principal;

public class GeraLogs {
    private Principal principal;
    
    public GeraLogs(Principal principal) {
        this.principal = principal;
    }

    public void logs(String log){
        principal.adicionarLog(log);
    }
}