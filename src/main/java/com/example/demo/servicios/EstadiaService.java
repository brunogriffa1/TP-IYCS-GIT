package com.example.demo.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.controladores.EstadiaControlador.CrearEstadiaRequest;
import com.example.demo.excepciones.EntidadNoEncontradaException;
import com.example.demo.excepciones.ValidacionException;
import com.example.demo.modelo.Estadia;
import com.example.demo.modelo.EstadoHabitacion;
import com.example.demo.modelo.EstadoReserva;
import com.example.demo.modelo.Habitacion;
import com.example.demo.modelo.Huesped;
import com.example.demo.modelo.Reserva;
import com.example.demo.repositorios.EstadiaRepositorio;
import com.example.demo.repositorios.HabitacionRepositorio;
import com.example.demo.repositorios.HuespedRepositorio;
import com.example.demo.repositorios.ReservaRepositorio;
// El método crearEstadiasMasivas es la Fachada. 
// Unifica en un solo punto de acceso toda la lógica transaccional, las validaciones y el cambio de estado de entidades
@Service
@Transactional
public class EstadiaService {
    
    @Autowired
    private EstadiaRepositorio estadiaRepositorio;

    @Autowired
    private HabitacionRepositorio habitacionRepositorio;

    @Autowired
    private HuespedRepositorio huespedRepositorio;

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    /**
     * CREACIÓN MASIVA (TRANSACCIONAL)
     * Valida todo el paquete antes de guardar nada. Si falla algo, no se guarda nada.
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Estadia> crearEstadiasMasivas(List<CrearEstadiaRequest> requests) throws Exception {
        List<Estadia> estadiasGuardadas = new ArrayList<>();
        
        // SET para controlar que un acompañante no esté repetido en este lote
        Set<Integer> acompanantesEnProceso = new HashSet<>();

        // 1. VALIDACIÓN PREVIA DE TODO EL LOTE
        for (CrearEstadiaRequest req : requests) {
            // A. Validar Titular
            if (!huespedRepositorio.existsById(req.getIdHuespedTitular())) {
                throw new ValidacionException("El titular con ID " + req.getIdHuespedTitular() + " no existe.");
            }

            // B. Validar Acompañantes (Regla de Negocio: Solo en una habitación)
            if (req.getIdHuespedesAcompanantes() != null) {
                for (Integer idAcomp : req.getIdHuespedesAcompanantes()) {
                    // 1. Chequeo dentro del mismo lote
                    if (acompanantesEnProceso.contains(idAcomp)) {
                        Huesped h = huespedRepositorio.findById(idAcomp).orElse(new Huesped());
                        throw new ValidacionException("El acompañante " + h.getApellido() + " " + h.getNombre() + 
                            " no puede estar asignado a múltiples habitaciones a la vez.");
                    }
                    acompanantesEnProceso.add(idAcomp);

                    // 2. Chequeo contra la base de datos
                    if (huespedSeHaAlojadoActualmente(idAcomp)) {
                         Huesped h = huespedRepositorio.findById(idAcomp).get();
                         throw new ValidacionException("El acompañante " + h.getApellido() + " ya se encuentra alojado en el hotel.");
                    }
                }
            }
            
            // C. Validar Disponibilidad de Habitación
            Habitacion hab = habitacionRepositorio.findById(req.getIdHabitacion())
                .orElseThrow(() -> new ValidacionException("Habitación " + req.getIdHabitacion() + " no existe"));
            
            if (hab.getEstado() == EstadoHabitacion.OCUPADA) {
                throw new ValidacionException("La habitación " + hab.getNumero() + " ya figura OCUPADA en el sistema.");
            }
            if (hab.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new ValidacionException("La habitación " + hab.getNumero() + " está FUERA DE SERVICIO.");
        }
        }

        // 2. GUARDADO EFECTIVO (Si llegamos acá, todo es válido)
        for (CrearEstadiaRequest req : requests) {
            estadiasGuardadas.add(procesarGuardadoIndividual(req));
        }
        
        return estadiasGuardadas;
    }

    // Lógica interna de guardado
    private Estadia procesarGuardadoIndividual(CrearEstadiaRequest request) throws Exception {
        Habitacion habitacion = habitacionRepositorio.findById(request.getIdHabitacion()).get();
        Huesped titular = huespedRepositorio.findById(request.getIdHuespedTitular()).get();
        
        List<Huesped> listaAcomp = new ArrayList<>();
        if (request.getIdHuespedesAcompanantes() != null) {
            for(Integer id : request.getIdHuespedesAcompanantes()) listaAcomp.add(huespedRepositorio.findById(id).get());
        }

        Optional<Reserva> reservaOpt = reservaRepositorio.findReservaActivaParaCheckIn(
        habitacion.getId(), LocalDate.now()
        );

        Integer idReservaVinculada = null;

        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            boolean coincidencia = false;

            // Validar si el Titular coincide con la Reserva (Apellido)
            if (titular.getApellido().trim().equalsIgnoreCase(reserva.getApellidoHuesped().trim())) {
                coincidencia = true;
            }

            // Si no coincidió el titular, buscamos en los acompañantes
            if (!coincidencia) {
                for (Huesped acomp : listaAcomp) {
                    if (acomp.getApellido().trim().equalsIgnoreCase(reserva.getApellidoHuesped().trim())) {
                        coincidencia = true;
                        break;
                    }
                }
            }

            if (coincidencia) {
                idReservaVinculada = reserva.getId();
                // Actualizamos el estado de la reserva para que no quede "colgando"
                reserva.setEstado(EstadoReserva.CONFIRMADA); // Asegurate de tener este ENUM o usar otro lógico
                reservaRepositorio.save(reserva);
            } else {
                // EL PUNTO 5 IMPLICA BLOQUEO SI NO ES LA PERSONA
                throw new ValidacionException("La habitación está reservada por " + 
                    reserva.getApellidoHuesped() + " y no coincide con los huéspedes ingresados.");
            }
        }

        Estadia estadia = new Estadia();
        estadia.setHabitacion(habitacion);
        estadia.setHuesped(titular);
        estadia.setCheckIn(LocalDateTime.now());
        //estadia.setCheckOut(null);
        estadia.setCantidadDias(request.getCantidadDias());
        estadia.setCantidadHuespedes(1 + listaAcomp.size());
        estadia.setCantidadHabitaciones(1);
        estadia.setIdReserva(idReservaVinculada);

        // Cambiar estado
        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepositorio.save(habitacion);
        
        Estadia guardada = estadiaRepositorio.save(estadia);

        // Actualizar ubicación actual
        titular.setEstadia(guardada);
        huespedRepositorio.save(titular);
        
        for(Huesped a : listaAcomp) {
            a.setEstadia(guardada);
            huespedRepositorio.save(a);
        }
        return guardada;
    }

    // --- CHECK-IN AUTOSERVICIO (tótem del hotel) ---
    // El huésped ingresa el código de su reserva y su documento. Si todo coincide se ocupa la habitación
    // sin pasar por recepción; si no, se le indica el motivo para que lo resuelva en recepción (CU15).
    public Estadia checkInAutoservicio(Integer idReserva, String tipoDocumento, String numeroDocumento)
            throws EntidadNoEncontradaException, ValidacionException {
        if (idReserva == null || tipoDocumento == null || numeroDocumento == null) {
            throw new ValidacionException("Ingrese el código de reserva y su documento.");
        }

        Reserva reserva = reservaRepositorio.findById(idReserva)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe una reserva con el código " + idReserva + "."));
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ValidacionException("La reserva no está confirmada.");
        }
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(reserva.getFechaEntrada())
                || (reserva.getFechaSalida() != null && !hoy.isBefore(reserva.getFechaSalida()))) {
            throw new ValidacionException("La reserva no corresponde a la fecha de hoy.");
        }
        if (estadiaRepositorio.existsByIdReserva(reserva.getId())) {
            throw new ValidacionException("El check-in de esta reserva ya fue realizado.");
        }

        Huesped huesped = huespedRepositorio.findByDocumento(tipoDocumento.trim(), numeroDocumento.trim())
                .orElseThrow(() -> new EntidadNoEncontradaException("No hay un huésped registrado con ese documento."));
        // Mismo criterio que el check-in manual: el apellido debe coincidir con el de la reserva
        if (reserva.getApellidoHuesped() == null || huesped.getApellido() == null
                || !reserva.getApellidoHuesped().trim().equalsIgnoreCase(huesped.getApellido().trim())) {
            throw new ValidacionException("El documento ingresado no corresponde al titular de la reserva.");
        }
        if (huesped.getEstadia() != null) {
            throw new ValidacionException("El huésped ya se encuentra alojado.");
        }

        Habitacion habitacion = reserva.getHabitacion();
        if (habitacion == null
                || habitacion.getEstado() == EstadoHabitacion.OCUPADA
                || habitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new ValidacionException("La habitación reservada no está disponible.");
        }

        Estadia estadia = new Estadia();
        estadia.setHabitacion(habitacion);
        estadia.setHuesped(huesped);
        estadia.setCheckIn(LocalDateTime.now());
        long dias = reserva.getFechaSalida() != null
                ? ChronoUnit.DAYS.between(hoy, reserva.getFechaSalida())
                : 1;
        estadia.setCantidadDias((int) Math.max(1, dias));
        estadia.setCantidadHuespedes(1);
        estadia.setCantidadHabitaciones(1);
        estadia.setIdReserva(reserva.getId());

        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepositorio.save(habitacion);

        Estadia guardada = estadiaRepositorio.save(estadia);
        huesped.setEstadia(guardada);
        huespedRepositorio.save(huesped);
        return guardada;
    }

    // Método auxiliar para saber si está alojado HOY (para validaciones)
    private boolean huespedSeHaAlojadoActualmente(Integer id) {
        // Si el huesped tiene seteada una estadía, es que está dentro
        return huespedRepositorio.findById(id).map(h -> h.getEstadia() != null).orElse(false);
    }

    // --- CU11: Verificar Historial ---
    public boolean huespedSeHaAlojado(Integer id) { 
        return !estadiaRepositorio.findByHuespedID(id).isEmpty();
    }

    public Optional<Estadia> buscarEstadiaActivaPorHabitacion(Integer idHabitacion) {
        // Llama a la query personalizada del repositorio
        return estadiaRepositorio.findByHabitacionIdAndOcupada(idHabitacion);
    }
}

