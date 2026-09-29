package com.example.demo.controladores;

import com.example.demo.excepciones.EntidadNoEncontradaException;
import com.example.demo.excepciones.ValidacionException;
import com.example.demo.modelo.Estadia;
import com.example.demo.servicios.EstadiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/estadias")
@CrossOrigin(origins = "*")
public class EstadiaControlador {

    @Autowired
    private EstadiaService estadiaService;

    // --- CU15: OCUPAR (CHECK-IN) ---
    @PostMapping("/masivo")
    public ResponseEntity<?> crearEstadiasMasivas(@RequestBody List<CrearEstadiaRequest> requests) {
        try {
            List<Estadia> nuevas = estadiaService.crearEstadiasMasivas(requests);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Se registraron " + nuevas.size() + " ocupaciones correctamente."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- CHECK-IN AUTOSERVICIO (tótem del hotel, sin pasar por recepción) ---
    @PostMapping("/check-in-autoservicio")
    public ResponseEntity<?> checkInAutoservicio(@RequestBody CheckInAutoservicioRequest request) {
        try {
            Estadia estadia = estadiaService.checkInAutoservicio(
                request.getIdReserva(), request.getTipoDocumento(), request.getNumeroDocumento());

            String habitacion = String.valueOf(estadia.getHabitacion().getNumero());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "¡Bienvenido/a " + estadia.getHuesped().getNombre() + "! Su habitación es la " + habitacion + ".",
                "habitacion", habitacion
            ));
        } catch (EntidadNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage() + " Por favor, acérquese a recepción."
            ));
        } catch (ValidacionException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() + " Por favor, acérquese a recepción."
            ));
        }
    }

    public static class CheckInAutoservicioRequest {
        private Integer idReserva;
        private String tipoDocumento;
        private String numeroDocumento;

        public Integer getIdReserva() { return idReserva; }
        public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

        public String getTipoDocumento() { return tipoDocumento; }
        public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

        public String getNumeroDocumento() { return numeroDocumento; }
        public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    }

    // --- CU11: Verificar Historial ---
    @GetMapping("/huesped/{id}/alojado")
    public ResponseEntity<?> verificarAlojado(@PathVariable Integer id) {
        boolean seHaAlojado = estadiaService.huespedSeHaAlojado(id);
        return ResponseEntity.ok(Map.of("seHaAlojado", seHaAlojado));
    }

    // --- CLASE INTERNA PARA EL REQUEST ---
    public static class CrearEstadiaRequest {
        private Integer idHabitacion;
        private Integer idHuespedTitular;
        private List<Integer> idHuespedesAcompanantes;
        private Integer cantidadHuespedes;
        private Integer cantidadDias;
        private Integer idReserva;

        // Constructores
        public CrearEstadiaRequest() {
        }

        // Getters y Setters
        public Integer getIdHabitacion() { return idHabitacion; }
        public void setIdHabitacion(Integer idHabitacion) { this.idHabitacion = idHabitacion; }

        public Integer getIdHuespedTitular() { return idHuespedTitular; }
        public void setIdHuespedTitular(Integer idHuespedTitular) { this.idHuespedTitular = idHuespedTitular; }

        public List<Integer> getIdHuespedesAcompanantes() { return idHuespedesAcompanantes; }
        public void setIdHuespedesAcompanantes(List<Integer> idHuespedesAcompanantes) { this.idHuespedesAcompanantes = idHuespedesAcompanantes; }

        public Integer getCantidadHuespedes() { return cantidadHuespedes; }
        public void setCantidadHuespedes(Integer cantidadHuespedes) { this.cantidadHuespedes = cantidadHuespedes; }

        public Integer getCantidadDias() { return cantidadDias; }
        public void setCantidadDias(Integer cantidadDias) { this.cantidadDias = cantidadDias; }

        public Integer getIdReserva() { return idReserva; }
        public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    }

    // --- ENDPOINT DE PRUEBA ---
    @GetMapping("/status")
    public String status() {
        return "Servicio de estadías activo";
    }

    // --- BUSCAR OCUPANTES DE UNA HABITACIÓN (Para CU07 Facturación) ---
    @GetMapping("/habitacion/{nro}/ocupantes")
    public ResponseEntity<?> obtenerOcupantesPorHabitacion(@PathVariable Integer nro) {
        try {
            // 1. Llamar al servicio para buscar la estadía ACTIVA en esa habitación
            // Nota: Debes asegurarte de tener este método en tu servicio (ver abajo)
            Optional<Estadia> estadiaOpt = estadiaService.buscarEstadiaActivaPorHabitacion(nro);

            // 2. Si no hay estadía activa, devolvemos 404 (Esto activa el modal rojo en el Front)
            if (estadiaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                            "success", false,
                            "message", "La habitación " + nro + " no se encuentra ocupada."
                        ));
            }

            Estadia estadia = estadiaOpt.get();

            // 3. Preparamos la lista de personas para la tabla del Frontend
            List<Map<String, Object>> listaPersonas = new ArrayList<>();

            // A. Agregamos al Huésped Titular (Responsable)
            listaPersonas.add(Map.of(
                "id", estadia.getHuesped().getId(),
                "apellido", estadia.getHuesped().getApellido(),
                "nombre", estadia.getHuesped().getNombre(),
                "tipoDoc", estadia.getHuesped().getTipoDocumento(),
                "documento", estadia.getHuesped().getNumeroDocumento()
            ));

            return ResponseEntity.ok(listaPersonas);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false, 
                "message", "Error interno: " + e.getMessage()
            ));
        }
    }
}