
package com.hms.prescription.dto;

import org.springframework.stereotype.Component;

import com.hms.prescription.Prescription;

@Component
public class PrescriptionResponseMapper {

    public PrescriptionResponse toResponse(Prescription prescription) {

        PrescriptionResponse response = new PrescriptionResponse();

        response.setPrescriptionId(prescription.getPrescriptionId());
        response.setPrescriptionNumber(prescription.getPrescriptionNumber());

        response.setPatientId(
                prescription.getPatient().getPatientId());

        response.setPatientName(
                prescription.getPatient().getUser().getFullName());

        response.setDoctorId(
                prescription.getDoctor().getDoctorId());

        response.setDoctorName(
                prescription.getDoctor().getUser().getFullName());

        if (prescription.getAppointment() != null) {
            response.setAppointmentId(
                    prescription.getAppointment().getAppointmentId());
        }

        response.setDiagnosis(prescription.getDiagnosis());
        response.setInstructions(prescription.getInstructions());
        response.setPrescriptionDate(prescription.getPrescriptionDate());
        response.setStatus(prescription.getStatus());

        return response;
    }
}
